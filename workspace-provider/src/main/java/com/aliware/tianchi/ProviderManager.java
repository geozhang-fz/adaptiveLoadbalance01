package com.aliware.tianchi;

import org.apache.dubbo.common.Constants;
import org.apache.dubbo.config.ProtocolConfig;
import org.apache.dubbo.config.context.ConfigManager;

import java.util.Optional;

public class ProviderManager {

    /* Data */

    private String quota = System.getProperty("quota");

    // 线程池大小
    Optional<ProtocolConfig> protocolConfig = ConfigManager.getInstance().getProtocol(Constants.DUBBO_PROTOCOL);
    private long providerThreadNum = protocolConfig.get().getThreads();

    // 当前活跃线程数
    private volatile long activeThreadNum = 0;

    // 每个发送消息的周期内，provider收到的请求数
    private volatile long reqCount = 0;

    // 每个发送消息的周期内，调用invoke方法的总处理时间
    private volatile long timeSpent = 0;

    /* Constructor */
    private static ProviderManager instance = new ProviderManager();

    public static ProviderManager getInstance() {
        return instance;
    }


    /* Getter */
    private final Object lockATN = new Object();
    private final Object lockRC = new Object();
    private final Object lockTS = new Object();

    public String getQuota() {
        return quota;
    }

    public long getProviderThreadNum() {
        return providerThreadNum;
    }

    public long getActiveThreadNum() {
        return activeThreadNum;
    }

    public long getReqCount() {
        return reqCount;
    }

    public long getTimeSpent() {
        return timeSpent;
    }


    /* Common Methods */
    public void incrementTotalReqCount() {
        synchronized (lockRC) {
            this.reqCount++;
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
        synchronized (lockTS) {
            this.timeSpent += timeSpent;
        }
    }


    /* Methods */
    public void reset() {
        synchronized (lockRC) {
            reqCount = 0;
        }
        synchronized (lockTS) {
            timeSpent = 0;
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
