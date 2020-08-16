package com.aliware.tianchi;

import org.apache.dubbo.common.Constants;
import org.apache.dubbo.config.ProtocolConfig;
import org.apache.dubbo.config.context.ConfigManager;
import org.apache.dubbo.rpc.listener.CallbackListener;
import org.apache.dubbo.rpc.service.CallbackService;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 服务端回调服务
 * 可选接口
 * 用户可以基于此服务，实现服务端向客户端动态推送的功能
 */
public class CallbackServiceImpl implements CallbackService {

    private ProviderManager providerManager = ProviderManager.getInstance();

    private Timer timer = new Timer();

    /**
     * key: listener type
     * value: callback listener
     */
    private final Map<String, CallbackListener> listeners = new ConcurrentHashMap<>();

    public CallbackServiceImpl() {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (!listeners.isEmpty()) {
                    for (Map.Entry<String, CallbackListener> entry : listeners.entrySet()) {
                        try {

                            entry.getValue().receiveServerMsg(getInfo());

                        } catch (Throwable t1) {
                            listeners.remove(entry.getKey());
                        }
                    }
                    providerManager.reset();
                }
            }
        }, 0, 1000);
    }

    private String getInfo() {

        // 获取provider的等级
        String quota = providerManager.getQuota();
        // 获取线程池大小
        long providerThreadNum = providerManager.getProviderThreadNum();
        // 获取当前活跃的线程数
        long activeThreadNum = providerManager.getActiveThreadNum();
        long totalTimeSpent = providerManager.getTotalTimeSpent();
        long totalReqCount = providerManager.getTotalReqCount();
        long totalAvgTime = 0;
        if (totalReqCount != 0) {
            totalAvgTime = totalTimeSpent / totalReqCount;
        }

        String notifyStr = String.format(
                "%s,%s,%s,%s,%s",
                quota, providerThreadNum, activeThreadNum, totalAvgTime, totalReqCount
        );

        return notifyStr;
    }

    @Override
    public void addListener(String key, CallbackListener listener) {
        listeners.put(key, listener);
        listener.receiveServerMsg(new Date().toString()); // send notification for change
    }
}
