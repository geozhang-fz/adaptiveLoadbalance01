package com.aliware.tianchi;

import org.apache.dubbo.rpc.Invoker;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class GatewayManager {

    public static final Map<String, ProviderLoadInfo> LOAD_INFO = new ConcurrentHashMap<>();
    private static final Map<String, AtomicInteger> AVAIL_MAP = new ConcurrentHashMap<>();

    /**
     * 根据传入的invoker调用，返回该invoker对应的provider服务器当前的信息
     * @param invoker
     * @return
     */
    public static ProviderLoadInfo getProviderLoadInfo(Invoker<?> invoker) {

        // 获取该provider服务器的ip
        String host = invoker.getUrl().getHost();
        // 根据该ip查看provider服务器当前的信息
        ProviderLoadInfo providerLoadInfo = LOAD_INFO.get(host);
        return providerLoadInfo;
    }

    public static void updateInfo(String notiftStr) {

        String[] severLoadArr = notiftStr.split(",");

        String quota = severLoadArr[0];
        int providerThread = Integer.valueOf(severLoadArr[1]);
        int activeCount = Integer.valueOf(severLoadArr[2]);
        int avgTime = Integer.valueOf(severLoadArr[3]);
        String key = "provider-" + quota;
        ProviderLoadInfo providerLoadInfo = LOAD_INFO.get(key);
        if (providerLoadInfo == null) {
            providerLoadInfo = new ProviderLoadInfo(quota, providerThread);
            LOAD_INFO.put(key, providerLoadInfo);
        }
        providerLoadInfo.getTotalActiveThreadNum().set(activeCount);
        int availCount = providerLoadInfo.getProviderAllThreads() - activeCount;
        providerLoadInfo.setAvgSpendTime(avgTime);

        AtomicInteger avail = AVAIL_MAP.get(key);
        if (avail == null) {
            avail = new AtomicInteger(availCount);
            AVAIL_MAP.put(key, avail);
        }
    }

    public static AtomicInteger getAvailThread(Invoker<?> invoker) {
        String host = invoker.getUrl().getHost();
        return AVAIL_MAP.get(host);
    }

}