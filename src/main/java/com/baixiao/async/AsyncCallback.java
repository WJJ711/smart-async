package com.baixiao.async;

import com.baixiao.async.core.AgentException;
import com.baixiao.async.core.ExecutorMessage;
import com.baixiao.async.core.container.ExecutorContainer;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * @author wjj
 * @version 1.0
 * @date 2020/7/7 19:25
 */
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AsyncCallback<T> {
    AsyncHandler<T> asyncHandler;
    volatile boolean isDone;
    T result;
    Exception exception;
    final CountDownLatch countDownLatch=new CountDownLatch(1);

    public boolean isDone(){return this.isDone;}

    public static <T> AsyncCallback<T> complete(T result){
        AsyncCallback<T> callback=new AsyncCallback<>();
        callback.completeWithResult(result);
        return callback;
    }
    public static <T> AsyncCallback exception(Exception exception){
        AsyncCallback<T> callback = new AsyncCallback<>();
        callback.completeWithException(exception);
        return callback;
    }
    public static <T,R> List<R> asyncMap(Collection<T> models,Function<T,R> transform){
        List<AsyncCallback<R>> list=new ArrayList<>();
        for (T model : models) {
            ExecutorMessage executorMessage=ExecutorMessage.newAsyncMessage(model);
            ExecutorContainer.receive(executorMessage,transform);
            list.add(executorMessage.getAsyncMessageCallback());
        }
        return compose(list).get();
    }
    public static <T> AsyncCallback<List<T>> compose(List<AsyncCallback<T>> asyncCallbacks){
        Assert.isTrue(null!= asyncCallbacks && !asyncCallbacks.isEmpty(),"asyncCallbacks can not be empty.");

        AsyncCallback<List<T>> composedAsyncCallback =new AsyncCallback<>();
        List<T> result=new ArrayList<>();

        AtomicInteger counter=new AtomicInteger(asyncCallbacks.size());
        for (AsyncCallback<T> asyncCallback : asyncCallbacks) {
            asyncCallback.onCompleted(new AsyncHandler<T>() {
                @Override
                public void onCompleted(AsyncResult<T> asyncResult) {
                    if(asyncResult.getResult()!=null){
                        synchronized (result){
                            result.add(asyncResult.getResult());
                        }
                    }
                    if (counter.decrementAndGet()==0){
                        composedAsyncCallback.completeWithResult(result);
                    }
                }
            });
        }
        return composedAsyncCallback;
    }
    public <R> AsyncCallback<R> link(Function<T, AsyncCallback<R>> transformer){
        Assert.notNull(transformer,"next can not be null");
        AsyncCallback<R> asyncCallback = new AsyncCallback<>();
        this.onCompleted(new AsyncHandler<T>() {
            @Override
            public void onCompleted(AsyncResult<T> asyncResult) {
                transformer.apply(asyncResult.getResult()).onCompleted(new AsyncHandler<R>() {
                    @Override
                    public void onCompleted(AsyncResult<R> linkedAsyncResult) {
                        if (linkedAsyncResult.getException()!=null){
                            asyncCallback.completeWithException(linkedAsyncResult.getException());
                        }else {
                            asyncCallback.completeWithResult(linkedAsyncResult.getResult());
                        }
                    }
                });
            }
        });
        return asyncCallback;
    }
    public <R> AsyncCallback<R> link(AsyncCallback<R> asyncCallback){
        Assert.notNull(asyncCallback,"next can not be null");
        this.onCompleted(new AsyncHandler<T>() {
            @Override
            public void onCompleted(AsyncResult<T> asyncResult) {
                asyncCallback.onCompleted(new AsyncHandler<R>() {
                    @Override
                    public void onCompleted(AsyncResult<R> linkedAsyncResult) {
                        if (linkedAsyncResult.getException()!=null){
                            asyncCallback.completeWithException(linkedAsyncResult.getException());
                        }else {
                            asyncCallback.completeWithResult(linkedAsyncResult.getResult());
                        }
                    }
                });
            }
        });
        return asyncCallback;
    }
    /**
     * 阻塞获取结果
     * @return
     */
    public T get(){
        if (!isDone){
            while (!isDone&&!Thread.currentThread().isInterrupted()){
                try {
                    countDownLatch.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    exception=e;
                }
            }
        }
        if (exception!=null){
            throw new AgentException(AgentException.ASYNC_WITH_EXCEPTION,AgentException.ASYNC_WITH_EXCEPTION_MESSAGE,exception);

        }
        return result;
    }
    public void onCompleted(AsyncHandler<T> asyncHandler){
        this.asyncHandler=asyncHandler;
        callAsyncHandler();
    }

    public void completeWithResult(Object result){
        this.result=(T)result;
        isDone=true;
        countDownLatch.countDown();
        callAsyncHandler();
    }
    public void completeWithException(Exception exception){
        this.exception=exception;
        this.isDone=true;
        countDownLatch.countDown();
        callAsyncHandler();
    }
    //计数器,保证回调函数callAsyncHandler只能被执行一次
    private final AtomicInteger asyncCounter =new AtomicInteger(1);
    /**
     * 调用回调方法
     */
    private void callAsyncHandler(){
        if (null!=this.asyncHandler&&this.isDone&&asyncCounter.decrementAndGet()==0){
            AsyncResult<T> asyncResult = new AsyncResult<>();
            asyncResult.setException(exception);
            asyncResult.setResult(result);
            try {
                asyncHandler.onCompleted(asyncResult);
            }catch (Exception e){
                log.error("Fail to invoke the async handler",e);
            }
        }
    }
}
