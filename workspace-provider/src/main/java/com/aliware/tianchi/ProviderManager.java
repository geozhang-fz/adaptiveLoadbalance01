package com.aliware.tianchi;

public class ProviderManager {
    private static final ProviderLoadInfo SERVER_INFO = new ProviderLoadInfo();


    public static ProviderLoadInfo getServerInfo() {

        return SERVER_INFO;
    }

    public static void endTime(long expend, boolean succeeded) {
        SERVER_INFO.getTotalActiveThreadNum().decrementAndGet();
        SERVER_INFO.getTotalReqCount().incrementAndGet();
        SERVER_INFO.getTotalTimeSpent().addAndGet(expend);
    }

    public static void resetTime() {
        SERVER_INFO.getTotalTimeSpent().set(0L);
        SERVER_INFO.getTotalReqCount().set(0L);
    }

    public static void startTime() {
        SERVER_INFO.getTotalActiveThreadNum().incrementAndGet();
    }
}
