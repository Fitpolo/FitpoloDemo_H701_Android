package com.fitpolo.support.callback;

/**
 * @Date 2017/5/10
 * @Author wenzheng.liu
 * @Description 前端展示连接回调
 * @ClassPath com.fitpolo.support.callback.ConnStateCallback
 */
public interface ConnStateCallback {
    /**
     * @Date 2017/5/10
     * @Author wenzheng.liu
     * @Description 连接成功
     */
    void onConnSuccess();

    /**
     * @Date 2017/5/10
     * @Author wenzheng.liu
     * @Description 连接失败
     */
    void onConnFailure(int errorCode);

    /**
     * @Date 2017/5/10
     * @Author wenzheng.liu
     * @Description 断开连接
     */
    void onDisconnect();

    /**
     * @Date 2017/8/29
     * @Author wenzheng.liu
     * @Description 重连超时
     */
    void onConnTimeout(int reConnCount);
}
