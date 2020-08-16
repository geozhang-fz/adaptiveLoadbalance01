package com.aliware.tianchi;

import org.apache.dubbo.common.Constants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.Filter;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcException;

/**
 * 服务端过滤器
 * 在provider服务器端拦截请求和响应，捕获 rpc 调用时产生、provider服务器端返回的已知异常。
 * 即为远程调用前后包裹上代理
 * （可选接口）
 */
@Activate(group = Constants.PROVIDER)
public class TestServerFilter implements Filter {

    private ProviderManager providerManager = ProviderManager.getInstance();

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        boolean isSuccess = false;
        long startTime = System.currentTimeMillis();
        try {
            providerManager.beforeInvoker();

            Result result = invoker.invoke(invocation);
            isSuccess = true;
            return result;
        } catch (Exception e) {
            throw e;
        } finally {
            providerManager.afterInvoker(System.currentTimeMillis() - startTime, isSuccess);
        }

    }

    @Override
    public Result onResponse(Result result, Invoker<?> invoker, Invocation invocation) {
        return result;
    }

}
