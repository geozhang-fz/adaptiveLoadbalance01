package com.aliware.tianchi;

import org.apache.dubbo.rpc.listener.CallbackListener;

/**
 * Gateway服务器端的监听器
 * 该类获取provider服务器端的推送信息，与 CallbackService 搭配使用
 * （可选接口）
 */
public class CallbackListenerImpl implements CallbackListener {

    /**
     * provider服务器端的CallbackServiceImpl调用，请求Gateway服务器端接收消息
     * @param msg
     */
    @Override
    public void receiveServerMsg(String msg) {

//        System.out.println("receive msg from server :" + msg);

        String[] msgs = msg.split(",");

        String quota = msgs[0];
        int index;
        if (quota.equals("small")) {
            index = 0;
        } else if (quota.equals("medium")) {
            index = 1;
        } else {
            index = 2;
        }

        /* 更新provider是否存在可用线程(Boolean) */
        int availThreadNum = Integer.valueOf(msgs[1]);
        boolean isAvailable = availThreadNum > 0 ? true : false;
        Context.AVAIL_ARR[index] = isAvailable;

        double avgTimeEachReq = Integer.valueOf(msgs[2]);

        /* 更新动态权重 */
        double updateIdx = 1;
        int upperBoundry = 800;
        int lowerBoundry = 100;
        if (avgTimeEachReq != 0) {
            updateIdx = 500 / avgTimeEachReq;
            double curWeight = Context.CUR_WEIGHT_ARR[index];
            if (lowerBoundry <= curWeight && curWeight <= upperBoundry) {
                // 限制动态权重的倍数
                // 下界缩8倍，上界扩5倍
                if (0.125 <= updateIdx && updateIdx <= 8) {
                    curWeight *= updateIdx;
                    Context.CUR_WEIGHT_ARR[index] = (int) curWeight;
                }
            } else if (curWeight < lowerBoundry) {
                Context.CUR_WEIGHT_ARR[index] = lowerBoundry;
            } else if (upperBoundry < curWeight) {
                Context.CUR_WEIGHT_ARR[index] = upperBoundry;
            }
        }

        /* 打印消息 */
        System.out.println(String.format(
            "%s级的provider，是否存在可用线程：%s，动态权重缩放：%s倍",
            quota, isAvailable, (int) updateIdx
        ));
    }

}
