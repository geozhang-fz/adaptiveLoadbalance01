package com.aliware.tianchi;

import org.apache.dubbo.rpc.listener.CallbackListener;

/**
 * @author daofeng.xjf
 *
 * 客户端监听器
 * 可选接口
 * 用户可以基于获取获取服务端的推送信息，与 CallbackService 搭配使用
 *
 */
public class CallbackListenerImpl implements CallbackListener {

    private static Context context = Context.getInstance();

    @Override
    public void receiveServerMsg(String msg) {

//        System.out.println("receive msg from server :" + msg);

        GatewayManager.updateInfo(msg);

        String[] msgs = msg.split(",");

        String quota = msgs[0];
        int index = context.QUOTA_TO_INDEX.get(quota);
        int providerThread = Integer.valueOf(msgs[1]);
        int activeCount = Integer.valueOf(msgs[2]);
        int avgTimeSpent = Integer.valueOf(msgs[3]);

        int availThreadNum = providerThread - activeCount;
        boolean isAvailable = availThreadNum > 0 ? true : false;
        context.AVAIL_ARR[index] = isAvailable;

        context.CUR_WEIGHT_ARR[index] *= (500 / avgTimeSpent);
    }

}
