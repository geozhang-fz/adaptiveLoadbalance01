package com.aliware.tianchi;

import java.util.concurrent.atomic.AtomicLong;


public class ProviderLoadInfo {

    private String quota = null;
    private volatile int weight = 0;
    private volatile int avgSpendTime;
    private int providerAllThreads = 0;
    private AtomicLong totalReqCount = new AtomicLong(0);
    private AtomicLong totalTimeSpent = new AtomicLong(0);
    private AtomicLong totalActiveThreadNum = new AtomicLong(0);

    public ProviderLoadInfo() {
    }

    public ProviderLoadInfo(String quota, int providerThread) {
        this.quota = quota;
        this.providerAllThreads = (int) (providerThread * 0.9);
        if ("large".equals(quota)) {
            this.weight = 15;
        } else if ("medium".equals(quota)) {
            this.weight = 9;
        } else if ("small".equals(quota)) {
            this.weight = 2;
        } else {
            this.weight = 1;
        }
    }

    public String getQuota() {
        return quota;
    }

    public void setQuota(String quota) {
        this.quota = quota;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public int getProviderAllThreads() {
        return providerAllThreads;
    }

    public void setProviderAllThreads(int providerAllThreads) {
        this.providerAllThreads = providerAllThreads;
    }

    public AtomicLong getTotalReqCount() {
        return totalReqCount;
    }

    public void setTotalReqCount(AtomicLong totalReqCount) {
        this.totalReqCount = totalReqCount;
    }

    public int getAvgSpendTime() {
        return avgSpendTime;
    }

    public void setAvgSpendTime(int avgSpendTime) {
        this.avgSpendTime = avgSpendTime;
    }

    public AtomicLong getTotalTimeSpent() {
        return totalTimeSpent;
    }

    public void setTotalTimeSpent(AtomicLong totalTimeSpent) {
        this.totalTimeSpent = totalTimeSpent;
    }

    public AtomicLong getTotalActiveThreadNum() {
        return totalActiveThreadNum;
    }

    public void setTotalActiveThreadNum(AtomicLong totalActiveThreadNum) {
        this.totalActiveThreadNum = totalActiveThreadNum;
    }
}
