package com.baixiao.async.core.container;

import com.baixiao.async.core.ExecutorMessage;
import com.baixiao.async.util.PropertiesUtil;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * @author wjj
 * @version 1.0
 * @date 2020/8/6 14:18
 */
public class ExecutorContainer {
    private static final Integer DEFAULT_CORESIZE=Integer.valueOf(PropertiesUtil.getProperty("async.core.size","20"));
    private static final Integer DEFAULT_MAXSIZE=Integer.valueOf(PropertiesUtil.getProperty("async.max.size","50"));
    private static final Long DEFAULT_KEEP_ALIVE_TIME=60L;
    private static ExecutorService executorService=new ThreadPoolExecutor(DEFAULT_CORESIZE,DEFAULT_MAXSIZE,DEFAULT_KEEP_ALIVE_TIME,
            TimeUnit.SECONDS,new LinkedBlockingQueue<>());
    public static <T,R> void receive(ExecutorMessage executorMessage, Function<T,R> transfor){
        executorService.submit(()->handle(executorMessage,transfor));
    }
    private static <T,R>void handle(ExecutorMessage executorMessage,Function<T,R> transfor){
        R r = transfor.apply((T) executorMessage.getParam());
        executorMessage.complete(r);
    }
    public static void exit(){
        executorService.shutdown();
    }
}
