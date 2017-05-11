package com.fitpolo.demo.activity;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.fitpolo.demo.R;
import com.fitpolo.support.OrderEnum;
import com.fitpolo.support.bluetooth.BluetoothModule;
import com.fitpolo.support.callback.OrderCallback;
import com.fitpolo.support.entity.BaseResponse;
import com.fitpolo.support.entity.InnerVersion;
import com.fitpolo.support.task.InnerVersionTask;
import com.fitpolo.support.task.SystemTimeTask;

/**
 * @Date 2017/5/11
 * @Author wenzheng.liu
 * @Description
 */

public class SendOrderActivity extends Activity implements OrderCallback {
    private static final String TAG = "SendOrderActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.send_order_layout);
    }

    public void getInnerVersion(View view) {
        InnerVersionTask innerVersionTask = new InnerVersionTask(this, new InnerVersion());
        SystemTimeTask systemTimeTask = new SystemTimeTask(this);
        BluetoothModule.getInstance().sendOrder(innerVersionTask, systemTimeTask);
    }

    @Override
    public void onOrderResult(OrderEnum order, BaseResponse response) {
        switch (order) {
            case getInnerVersion:
                Log.i(TAG, "onOrderResult: getInnerVersion");
                InnerVersion version = (InnerVersion) response;
                boolean isSupportHeartRate = version.isSupportHeartRate;
                boolean isOldBand = version.isOldBand;
                break;
            case setSystemTime:
                Log.i(TAG, "onOrderResult: setSystemTime");
                break;
        }

    }

    @Override
    public void onOrderTimeout(OrderEnum order) {

    }

    @Override
    public void onOrderFinish() {
        Log.i(TAG, "onOrderFinish: ");
    }
}
