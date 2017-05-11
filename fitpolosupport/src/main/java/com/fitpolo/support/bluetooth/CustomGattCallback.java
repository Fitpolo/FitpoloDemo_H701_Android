package com.fitpolo.support.bluetooth;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothProfile;

import com.fitpolo.support.callback.GattCallback;
import com.fitpolo.support.log.LogModule;

/**
 * @Date 2017/5/10
 * @Author wenzheng.liu
 * @Description 自定义蓝牙连接回调
 * @ClassPath com.fitpolo.support.bluetooth.CustomGattCallback
 */
public class CustomGattCallback extends BluetoothGattCallback {

    private GattCallback mGattCallback;

    public CustomGattCallback(GattCallback gattCallback) {
        this.mGattCallback = gattCallback;
    }

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        super.onConnectionStateChange(gatt, status, newState);
        LogModule.e("onConnectionStateChange...status:" + status + "...newState:" + newState);
        if (newState == BluetoothProfile.STATE_CONNECTED) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                mGattCallback.onConnSuccess();
                return;
            }
        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            mGattCallback.onDisConn();
            return;
        }
        mGattCallback.onConnFailure();
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        super.onServicesDiscovered(gatt, status);
        LogModule.e("onServicesDiscovered...status:" + status);
        if (status == BluetoothGatt.GATT_SUCCESS) {
            mGattCallback.onServicesDiscovered();
        } else {
            mGattCallback.onConnFailure();
        }
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        super.onCharacteristicChanged(gatt, characteristic);
        mGattCallback.onResponse(characteristic);
    }
}
