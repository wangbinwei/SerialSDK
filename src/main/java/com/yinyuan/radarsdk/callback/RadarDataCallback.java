package com.yinyuan.radarsdk.callback;

/**
 * 雷达数据回调函数接口
 * @author YD_Tao
 */
@FunctionalInterface
public interface RadarDataCallback<T> {

    /**
     * 雷达数据通知函数
     * @param message
     */
    void notify(T message);
}
