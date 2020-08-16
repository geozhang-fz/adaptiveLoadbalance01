package com.aliware.tianchi;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.RpcException;
import org.apache.dubbo.rpc.cluster.LoadBalance;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 负载均衡扩展接口
 * 必选接口，核心接口
 * 此类可以修改实现，不可以移动类或者修改包名
 * 选手需要基于此类实现自己的负载均衡算法
 */
public class UserLoadBalance implements LoadBalance {

    private static Context context = Context.getInstance();

    @Override
    public <T> Invoker<T> select(List<Invoker<T>> invokers, URL url, Invocation invocation) throws RpcException {

        return dynamicRandomWeight02(invokers);
    }


    /**
     * 根据服务端配置和平均耗时计算权重
     */
    private <T> Invoker<T> dynamicRandomWeight02(List<Invoker<T>> invokers) {

        int size = invokers.size();

        int[] weights = new int[size];
        int[] curWeights = new int[size];

        // 总权重
        int totalWeight = 0;

        /* 获取静态权重、动态权重、计算总权重 */
        for (int i = 0; i < size; i++) {

            if (context.AVAIL_ARR[i]) { // provider只有可用线程，才被赋予权重

                // 获取s、m、l当前的静态权重
                weights[i] = context.WEIGHT_ARR[i];

                // 获取s、m、l当前的动态权重
                curWeights[i] = context.CUR_WEIGHT_ARR[i];

                totalWeight = totalWeight + weights[i] + curWeights[i];
            }
        }

        int offsetWeight = ThreadLocalRandom.current().nextInt(totalWeight);

        for (int i = 0; i < size; ++i) {

            offsetWeight -= (weights[i] + curWeights[i]);

            if (offsetWeight < 0) {
                return invokers.get(i);
            }
        }

        //兜底采用随机算法
        return invokers.get(ThreadLocalRandom.current().nextInt(invokers.size()));
    }
}
