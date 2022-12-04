package com.reggie.common;

import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Generic return result, The data that the server responds to will eventually be encapsulated into this object
 * @param <T>
 */

@Data
public class R<T> implements Serializable {

    private Integer code; //1 is successful, 0 and other nums are failure

    private String msg; //error message

    private T data;

    private Map map = new HashMap(); //dynamic data

    //creating generic methods
    public static<T> R<T> success(T object){
        R<T> r = new R<T>();
        r.data = object;
        r.code = 1;
        return r;
    }

    public static<T> R<T> error(String msg){
        R<T> r = new R<T>();
        r.msg = msg;
        r.code = 0;
        return r;
    }

    public R<T> add(String key, Object value){
        this.map.put(key, value);
        return this;
    }

}
