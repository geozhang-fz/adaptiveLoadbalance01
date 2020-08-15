package com.aliware.tianchi;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class Context {

    private static Context instance = new Context();

    public static Context getInstance() {

        return instance;

    }



    enum Provider {
        // Provider的name
        S,
        M,
        L,
        ;
    }

    private static final int INVOKERS_SIZE = 3;

    // 静态权重
    public static final Integer[] WEIGHT_ARR = new Integer[INVOKERS_SIZE];

    // 动态权重
    public static final Integer[] CUR_WEIGHT_ARR = new Integer[INVOKERS_SIZE];

    // 表示对应的provider是否有空余的线程
    public static final Boolean[] AVAIL_ARR = new Boolean[INVOKERS_SIZE];

    // quota到index的转换
    public static final Map<String, Integer> QUOTA_TO_INDEX = new ConcurrentHashMap<>();

    static {
        QUOTA_TO_INDEX.put("small", 0);
        QUOTA_TO_INDEX.put("medium", 1);
        QUOTA_TO_INDEX.put("large", 2);

        WEIGHT_ARR[0] = 100;
        WEIGHT_ARR[1] = 400;
        WEIGHT_ARR[2] = 700;

        CUR_WEIGHT_ARR[0] = 500;
        CUR_WEIGHT_ARR[1] = 500;
        CUR_WEIGHT_ARR[2] = 500;
    }
}
