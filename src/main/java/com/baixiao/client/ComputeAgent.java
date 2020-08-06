package com.baixiao.client;

import com.baixiao.async.AsyncCallback;

/**
 * @author wjj
 * @version 1.0
 * @date 2020/7/10 16:02
 */
public class ComputeAgent {

    public AsyncCallback<Integer> addOne(Integer in){
        return AsyncCallback.complete(in+1);
    }
}
