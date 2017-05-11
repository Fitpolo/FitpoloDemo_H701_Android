package com.fitpolo.support;

import android.content.Context;

import com.fitpolo.support.bluetooth.BluetoothModule;
import com.fitpolo.support.log.LogModule;

/**
 * @Date 2017/5/9
 * @Author wenzheng.liu
 * @Description
 */

public class Fitpolo {

    public static void init(Context context) {
        LogModule.init(context);
        BluetoothModule.getInstance().createBluetoothAdapter(context);
    }
}
