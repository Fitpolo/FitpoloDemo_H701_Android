package com.fitpolo.support;

import android.content.Context;

import com.fitpolo.support.bluetooth.BluetoothModule;
import com.fitpolo.support.log.LogModule;

/**
 * @Date 2017/5/13 0013
 * @Author wenzheng.liu
 * @Description 初始化类
 * @ClassPath com.fitpolo.support.Fitpolo
 */
public class Fitpolo {

    public static void init(Context context) {
        LogModule.init(context);
        BluetoothModule.getInstance().createBluetoothAdapter(context);
    }
}
