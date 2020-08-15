package com.aliware.tianchi;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.RpcException;
import org.apache.dubbo.rpc.cluster.LoadBalance;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author daofeng.xjf
 *
 * 负载均衡扩展接口
 * 必选接口，核心接口
 * 此类可以修改实现，不可以移动类或者修改包名
 * 选手需要基于此类实现自己的负载均衡算法
 */
public class UserLoadBalance implements LoadBalance {

    @Override
    public <T> Invoker<T> select(List<Invoker<T>> invokers, URL url, Invocation invocation) throws RpcException {
        //1、所有服务器争取每个打一次请求
        Invoker invoker = doSelectInFreeInvokers(invokers);

        //2、根据服务端信息分配权重
        invoker = invoker != null ? invoker : doSelectWithWeigth(invokers);

        return invoker;
    }


    /**
     * 落实优先每个机器都有流量请求
     */
    private <T> Invoker<T> doSelectInFreeInvokers(List<Invoker<T>> invokers) {

        // 只要LOAD_INFO未满额，就表示还有provider没访问过
        if (CustomerInfoManager.LOAD_INFO.size() < invokers.size()) {
            for (Invoker invoker : invokers) {

                CustomerInfo customerInfo = CustomerInfoManager.getServerLoadInfo(invoker);

                if (customerInfo != null) break;

                // 若customerInfo为空，表示该provider还没访问过，遂选该provider
                return invoker;
            }
        }

        return null;
    }

    /**
     * 根据服务端配置和平均耗时计算权重
     */
    private <T> Invoker<T> doSelectWithWeigth(List<Invoker<T>> invokers) {
        int size = invokers.size();

        // 重新分配权重的<服务,权重>映射
        int[] serviceWeight = new int[size];

        // 总权重
        int totalWeight = 0;

        // 1、计算总权重
        for (int i = 0; i < size; ++i) {

            // 选出invokers列表中第index个invoker
            Invoker<T> invoker = invokers.get(i);

            // 从CustomerInfoManager.LOAD_INFO中取出对应的
            // 获取该invoker对应的provider服务器的信息
            CustomerInfo customerInfo = CustomerInfoManager.getServerLoadInfo(invoker);
            //
            AtomicInteger availThreadAtomic = CustomerInfoManager.getAvailThread(invoker);

            if (customerInfo != null) {

                if (availThreadAtomic.get() > 0) {
                    int weight = customerInfo.getServerWeight();
                    int clientTimeAvgSpendCurr = customerInfo.getAvgSpendTime();
                    if (clientTimeAvgSpendCurr == 0) {
                        // 耗时为0，可能性能优良，请求直接打到该机器；也有可能是性能差
                        // 采用随机
                        return invokers.get(ThreadLocalRandom.current().nextInt(invokers.size()));
                    }

                    // 更新权重
                    // 与500ms比较，如果比500ms小很多，新的weight较原weight增(500 / clientTimeAvgSpendCurr)
                    weight = weight * (500 / clientTimeAvgSpendCurr);
                    serviceWeight[i] = weight;
                    totalWeight = totalWeight + weight;
                }
                // 若availThreadAtomic.get()<0，
                // 则serviceWeight[i]对应的weight为0，这一次就不为该provider分配请求
            }
        }

        // 2、按照新的权重选择服务，权重加权随机算法
        int offsetWeight = ThreadLocalRandom.current().nextInt(totalWeight);

        for (int i = 0; i < size; ++i) {
            offsetWeight -= serviceWeight[i];
            if (offsetWeight < 0) {
                return invokers.get(i);
            }
        }

        //兜底采用随机算法
        return invokers.get(ThreadLocalRandom.current().nextInt(invokers.size()));
    }
}
