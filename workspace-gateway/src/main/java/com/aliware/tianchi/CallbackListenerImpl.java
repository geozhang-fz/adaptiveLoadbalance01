package com.aliware.tianchi;

import org.apache.dubbo.rpc.listener.CallbackListener;

import java.text.SimpleDateFormat;
import java.util.Date;

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

        /* 处理推送消息 */
        String[] msgs = msg.split(",");

        String quota = msgs[0];
        int code = Context.mapQuotaToCode(quota);

        // 更新provider是否存在可用线程(Boolean)
        int availThreadNum = Integer.valueOf(msgs[1]);
        boolean isAvailable = availThreadNum > 0 ? true : false;
        Context.AVAIL_ARR[code] = isAvailable;

        double avgTimeEachReq = Integer.valueOf(msgs[2]);

        // 获取时间
        Date now = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String nowStr = sdf.format(now);

        /* 更新动态权重 */
        double updateIdx = Context.updateCurWeight(avgTimeEachReq, code);

        /* 打印消息 */
        System.out.println(String.format(
            "【%s】%s级的provider，是否存在可用线程：%s，动态权重缩放：%s倍",
            nowStr, quota, isAvailable, (int) updateIdx
        ));
    }
}
