package com.aliware.tianchi;

import org.apache.dubbo.common.Constants;
import org.apache.dubbo.config.ProtocolConfig;
import org.apache.dubbo.config.context.ConfigManager;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public class ProviderManager {

    /* Data */
    private static ProviderManager instance = new ProviderManager();

    private String quota = System.getProperty("quota");

    // 线程池大小
    Optional<ProtocolConfig> protocolConfig = ConfigManager.getInstance().getProtocol(Constants.DUBBO_PROTOCOL);
    private long providerThreadNum = protocolConfig.get().getThreads();

    // 当前活跃线程数
    private volatile long activeThreadNum = 0;

    private volatile long totalReqCount = 0;

    private volatile long totalTimeSpent = 0;


    /* Getter */
    private final Object lockATN = new Object();
    private final Object lockTRC = new Object();
    private final Object lockTTS = new Object();


    public static ProviderManager getInstance() {
        return instance;
    }

    public String getQuota() {
        return quota;
    }

    public long getProviderThreadNum() {
        return providerThreadNum;
    }

    public long getActiveThreadNum() {
        return activeThreadNum;
    }

    public long getTotalReqCount() {
        return totalReqCount;
    }

    public long getTotalTimeSpent() {
        return totalTimeSpent;
    }


    /* Common Methods */
    public void incrementTotalReqCount() {
        synchronized (lockTRC) {
            this.totalReqCount++;
        }
    }

    public void incrementActiveThreadNum() {
        synchronized (lockATN) {
            this.activeThreadNum++;
        }
    }

    public void decrementActiveThreadNum() {
        synchronized (lockATN) {
            this.activeThreadNum--;
        }
    }

    public void addTotalTimeSpent(long timeSpent) {
        synchronized (lockTTS) {
            this.totalTimeSpent += timeSpent;
        }
    }


    /* Methods */
    public void reset() {
        synchronized (lockTRC) {
            totalReqCount = 0;
        }
        synchronized (lockTTS) {
            totalTimeSpent = 0;
        }
    }

    public void afterInvoker(long timeSpent, boolean succeeded) {
        this.decrementActiveThreadNum();
        this.incrementTotalReqCount();
        this.addTotalTimeSpent(timeSpent);
    }

    public void beforeInvoker() {
        incrementActiveThreadNum();
    }
}
