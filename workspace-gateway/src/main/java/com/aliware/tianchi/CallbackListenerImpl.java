package com.aliware.tianchi;

import org.apache.dubbo.rpc.listener.CallbackListener;

/**
 * Gateway服务器端的监听器
 * 该类获取provider服务器端的推送信息，与 CallbackService 搭配使用
 * （可选接口）
 */
public class CallbackListenerImpl implements CallbackListener {

    private static Context context = Context.getInstance();

    /**
     * provider服务器端的CallbackServiceImpl调用，请求Gateway服务器端接收消息
     * @param msg
     */
    @Override
    public void receiveServerMsg(String msg) {

//        System.out.println("receive msg from server :" + msg);

        String[] msgs = msg.split(",");

        String quota = msgs[0];
        int index = context.QUOTA_TO_INDEX.get(quota);

        /* 更新provider是否存在可用线程(Boolean) */
        int availThreadNum = Integer.valueOf(msgs[1]);
        boolean isAvailable = availThreadNum > 0 ? true : false;
        context.AVAIL_ARR[index] = isAvailable;

        int avgTimeEachReq = Integer.valueOf(msgs[2]);

        /* 更新动态权重 */
        context.CUR_WEIGHT_ARR[index] *= (500 / avgTimeEachReq);

        /* 打印消息 */
        System.out.println(String.format(
                "%s级的provider，是否存在可用线程：%s",
                quota, isAvailable
        ));
    }

}
