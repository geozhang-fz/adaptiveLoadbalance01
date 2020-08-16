package com.aliware.tianchi;

public class Context {

    private static Context instance = new Context();

    public static Context getInstance() {

        return instance;

    }

    private static final int INVOKERS_SIZE = 3;

    // 静态权重
    public static final Integer[] WEIGHT_ARR = new Integer[INVOKERS_SIZE];

    // 动态权重
    public static final Integer[] CUR_WEIGHT_ARR = new Integer[INVOKERS_SIZE];

    // 表示对应的provider是否有空余的线程
    public static final Boolean[] AVAIL_ARR = new Boolean[INVOKERS_SIZE];

    // quota到index的转换
//    public static Map<String, Integer> QUOTA_TO_INDEX = Map.of(
//            "small", 0,
//            "medium", 1,
//            "large", 2
//    );

    static {

        WEIGHT_ARR[0] = 100;
        WEIGHT_ARR[1] = 400;
        WEIGHT_ARR[2] = 700;

        CUR_WEIGHT_ARR[0] = 200;
        CUR_WEIGHT_ARR[1] = 200;
        CUR_WEIGHT_ARR[2] = 200;

        for (int i = 0; i < INVOKERS_SIZE; i++) {
            AVAIL_ARR[i] = true;
        }
    }
}
