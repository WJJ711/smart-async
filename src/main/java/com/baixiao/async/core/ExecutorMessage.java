package com.baixiao.async.core;

import com.baixiao.async.AsyncCallback;
import lombok.Data;

/**
 * @author wjj
 * @version 1.0
 * @date 2020/8/6 14:31
 */
@Data
public class ExecutorMessage<R,T> {
    private AsyncCallback<R> asyncMessageCallback;
    private Object result;
    private T param;

    public ExecutorMessage(AsyncCallback<R> asyncMessageCallback, T param) {
        this.asyncMessageCallback = asyncMessageCallback;
        this.param = param;
    }
    public void complete(Object result){
        this.result=result;
        this.asyncMessageCallback.completeWithResult(result);
    }
    public static <R,T>ExecutorMessage newAsyncMessage(T param){
        AsyncCallback<R> asyncMessageCallback=new AsyncCallback<>();
        return new ExecutorMessage(asyncMessageCallback,param);
    }
}
