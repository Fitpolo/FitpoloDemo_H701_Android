package com.fitpolo.support.callback;

import android.bluetooth.BluetoothGattCharacteristic;

/**
 * @Date 2017/5/10
 * @Author wenzheng.liu
 * @Description
 */

public interface GattCallback {
    /**
     * @Date 2017/5/10
     * @Author wenzheng.liu
     * @Description 打开服务
     */
    void onServicesDiscovered();

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
    void onConnFailure();

    /**
     * @Date 2017/5/10
     * @Author wenzheng.liu
     * @Description 连接断开
     */
    void onDisConn();

    /**
     * @Date 2017/5/10
     * @Author wenzheng.liu
     * @Description 返回数据
     */
    void onResponse(BluetoothGattCharacteristic characteristic);
}
