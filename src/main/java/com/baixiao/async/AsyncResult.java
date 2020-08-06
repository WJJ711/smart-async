package com.baixiao.async;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

/**
 * @author wjj
 * @version 1.0
 * @date 2020/7/7 16:55
 */
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AsyncResult<T> {
    T result;
    Exception exception;
}
