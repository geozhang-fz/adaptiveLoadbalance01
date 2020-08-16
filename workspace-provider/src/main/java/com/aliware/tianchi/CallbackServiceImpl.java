package com.aliware.tianchi;

import org.apache.dubbo.rpc.listener.CallbackListener;
import org.apache.dubbo.rpc.service.CallbackService;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 该类实现provider服务器端向Gateway服务器端动态推送消息
 * provider服务器接收Gateway服务器 CallbackListener 的注册，并执行消息推送
 * provider服务器每 5 秒向Gateway服务器端推送消息
 * （可选接口）
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

                            entry.getValue().receiveServerMsg(getMsg());

                        } catch (Throwable t1) {
                            listeners.remove(entry.getKey());
                        }
                    }
                    providerManager.reset();
                }
            }
        }, 0, 5000);
    }

    /**
     * 生成发往Gateway服务器的消息
     * @return
     */
    private String getMsg() {
        /* 获取各指标 */
        // 获取provider的等级
        String quota = providerManager.getQuota();
        // 获取线程池大小
        long providerThreadNum = providerManager.getProviderThreadNum();
        // 获取当前活跃的线程数
        long activeThreadNum = providerManager.getActiveThreadNum();
        // 计算可用线程数
        long availThreadNum = providerThreadNum - activeThreadNum;
        // 获取该发送消息周期内，provider收到的请求数
        long reqCount = providerManager.getReqCount();
        // 获取该发送消息周期内，调用invoke方法的总处理时间
        long timeSpent = providerManager.getTimeSpent();
        // 计算该发送消息周期内，每份请求的平均处理时间
        long avgTimeEachReq = 0;
        if (reqCount != 0) {
            avgTimeEachReq = timeSpent / reqCount;
        }

        /* 生成消息 */
        String msg = String.format(
            "%s,%s,%s",
            quota, availThreadNum, avgTimeEachReq
        );

        /* 打印当前状态 */
        System.out.println(String.format(
            "%s级的provider，线程池大小：%s，活跃线程数：%s，每份请求的平均处理时间：%s",
            quota, providerThreadNum, activeThreadNum, avgTimeEachReq
        ));

        return msg;
    }

    @Override
    public void addListener(String key, CallbackListener listener) {
        listeners.put(key, listener);
        listener.receiveServerMsg(new Date().toString()); // send notification for change
    }
}
