package com.example.rainweather.utils;

/**
 * description ：资源封装类
 * toDo：封装网络请求或数据加载过程中的三种状态：加载中、成功、错误。
 * email : 3014386984@qq.com
 * date : 2/19 18:00
 */
public class Resource<T> {
    public enum Status { SUCCESS, ERROR, LOADING }
    public final Status status;
    public final T data;
    public final String message;

    public Resource(Status status, T data, String message) {
        this.status = status;
        this.data = data;
        this.message = message;
    }

    public static <T> Resource<T> success(T data) {
        return new Resource<>(Status.SUCCESS, data, null);
    }

    public static <T> Resource<T> error(String msg, T data) {
        return new Resource<>(Status.ERROR, data, msg);
    }

    public static <T> Resource<T> loading(T data) {
        return new Resource<>(Status.LOADING, data, null);
    }
}