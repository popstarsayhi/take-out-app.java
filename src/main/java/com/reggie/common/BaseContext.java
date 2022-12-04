package com.reggie.common;

/**
 * based on ThreadLocal utility, used to save and get current user's id
 * 作用于某一个线程类，每一个线程保存自己的副本
 */
public class BaseContext {

    private static ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    /**
     * set id of current user
     * @param id
     */
    public static void setCurrentId(Long id){
        threadLocal.set(id);
    }

    /**
     * get id of current user
     * @return
     */

    public static Long getCurrentId(){
        return threadLocal.get();
    }

}
