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

    public static int mapQuotaToCode(String quota) {
        int index;
        if (quota.equals("small")) {
            index = 0;
        } else if (quota.equals("medium")) {
            index = 1;
        } else {
            index = 2;
        }
        return index;
    }

    public static double updateCurWeight(double avgTimeEachReq, int providerCode) {
        double updateIdx = 1;
        int upperBoundry = 800;
        int lowerBoundry = 100;
        if (avgTimeEachReq != 0) {
            updateIdx = 500 / avgTimeEachReq;
            double curWeight = Context.CUR_WEIGHT_ARR[providerCode];
            if (lowerBoundry <= curWeight && curWeight <= upperBoundry) {
                // 限制动态权重的倍数
                // 下界缩8倍，上界扩5倍
                if (0.125 <= updateIdx && updateIdx <= 8) {
                    curWeight *= updateIdx;
                    Context.CUR_WEIGHT_ARR[providerCode] = (int) curWeight;
                }
            } else if (curWeight < lowerBoundry) {
                Context.CUR_WEIGHT_ARR[providerCode] = lowerBoundry;
            } else if (upperBoundry < curWeight) {
                Context.CUR_WEIGHT_ARR[providerCode] = upperBoundry;
            }
        }
        return updateIdx;
    }
}
