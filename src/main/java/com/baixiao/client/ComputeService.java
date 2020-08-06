package com.baixiao.client;

import com.baixiao.async.AsyncCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wjj
 * @version 1.0
 * @date 2020/7/10 20:01
 */
@Service
public class ComputeService {

    @Autowired
    ComputeAgent computeAgent;
    public List<Integer> computeBatch(List<Integer> list){
        List<AsyncCallback<Integer>> asyncCallbacks =new ArrayList<>();
        list.forEach(integer -> asyncCallbacks.add(computeAgent.addOne(integer)));
        List<Integer> result = AsyncCallback.compose(asyncCallbacks).get();
        return result;
    }
}
