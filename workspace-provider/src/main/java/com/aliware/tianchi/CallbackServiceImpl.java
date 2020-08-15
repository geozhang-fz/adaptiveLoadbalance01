package com.aliware.tianchi;

import org.apache.dubbo.common.Constants;
import org.apache.dubbo.config.ProtocolConfig;
import org.apache.dubbo.config.context.ConfigManager;
import org.apache.dubbo.rpc.listener.CallbackListener;
import org.apache.dubbo.rpc.service.CallbackService;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author daofeng.xjf
 * <p>
 * 服务端回调服务
 * 可选接口
 * 用户可以基于此服务，实现服务端向客户端动态推送的功能
 */
public class CallbackServiceImpl implements CallbackService {

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
                    ProviderManager.resetTime();
                }
            }
        }, 0, 1000);
    }

    private String getInfo() {
        ProviderLoadInfo providerLoadInfo = ProviderManager.getServerInfo();
        Optional<ProtocolConfig> protocolConfig = ConfigManager.getInstance().getProtocol(Constants.DUBBO_PROTOCOL);
        String quota = System.getProperty("quota");
        int providerThread = protocolConfig.get().getThreads();
        long allSpendTimeTotal = providerLoadInfo.getTotalTimeSpent().get();
        long allReqCount = providerLoadInfo.getTotalReqCount().get();
        long allAvgTime = 0;
        long allActiveCount = providerLoadInfo.getTotalActiveThreadNum().get();
        if (allReqCount != 0) {
            allAvgTime = allSpendTimeTotal / allReqCount;
        }
        StringBuilder info = new StringBuilder();
        info.append(quota).append(",").append(providerThread).append(",").append(allActiveCount).append(",").append(allAvgTime).append(",").append(allReqCount);

        return info.toString();
    }

    @Override
    public void addListener(String key, CallbackListener listener) {
        listeners.put(key, listener);
        listener.receiveServerMsg(new Date().toString()); // send notification for change
    }
}
