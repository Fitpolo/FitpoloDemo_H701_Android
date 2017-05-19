package com.fitpolo.demo.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.Toast;

import com.fitpolo.demo.DemoConstant;
import com.fitpolo.demo.R;
import com.fitpolo.demo.service.FitpoloService;
import com.fitpolo.support.FitConstant;
import com.fitpolo.support.OrderEnum;
import com.fitpolo.support.bluetooth.BluetoothModule;
import com.fitpolo.support.entity.BaseResponse;
import com.fitpolo.support.entity.DailySleep;
import com.fitpolo.support.entity.DailyStep;
import com.fitpolo.support.entity.HeartRate;
import com.fitpolo.support.entity.req.BandAlarm;
import com.fitpolo.support.entity.req.SitLongTimeAlert;
import com.fitpolo.support.entity.req.UserInfo;
import com.fitpolo.support.log.LogModule;

import java.util.ArrayList;

/**
 * @Date 2017/5/11
 * @Author wenzheng.liu
 * @Description
 */

public class SendOrderActivity extends Activity {
    private static final String TAG = "SendOrderActivity";
    private FitpoloService mService;
    private LocalBroadcastManager mBroadcastManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.send_order_layout);
        mBroadcastManager = LocalBroadcastManager.getInstance(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction(DemoConstant.ACTION_ORDER_RESULT);
        filter.addAction(DemoConstant.ACTION_ORDER_TIMEOUT);
        filter.addAction(DemoConstant.ACTION_ORDER_FINISH);
        mBroadcastManager.registerReceiver(mReceiver, filter);
        bindService(new Intent(this, FitpoloService.class), mServiceConnection, BIND_AUTO_CREATE);
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                if (DemoConstant.ACTION_ORDER_RESULT.equals(action)) {
                    OrderEnum orde = (OrderEnum) intent.getSerializableExtra("order");
                    BaseResponse response = (BaseResponse) intent.getSerializableExtra("response");
                    if (FitConstant.ORDER_CODE_SUCCESS == response.code) {
                        switch (orde) {
                            case getInnerVersion:
                                Toast.makeText(SendOrderActivity.this, "获取内部版本号成功", Toast.LENGTH_SHORT).show();
                                boolean isSupportTodayData = BluetoothModule.getInstance().isSupportTodayData();
                                LogModule.i("是否支持同步当天数据：" + isSupportTodayData);
                                boolean isSupportHeartRate = BluetoothModule.getInstance().isSupportHeartRate();
                                LogModule.i("是否支持心率功能：" + isSupportHeartRate);
                                break;
                            case setSystemTime:
                                Toast.makeText(SendOrderActivity.this, "设置手环时间成功", Toast.LENGTH_SHORT).show();
                                break;
                            case setUserInfo:
                                Toast.makeText(SendOrderActivity.this, "设置用户信息成功", Toast.LENGTH_SHORT).show();
                                break;
                            case setBandAlarm:
                                Toast.makeText(SendOrderActivity.this, "设置闹钟数据成功", Toast.LENGTH_SHORT).show();
                                break;
                            case setUnitType:
                                Toast.makeText(SendOrderActivity.this, "设置单位制式成功", Toast.LENGTH_SHORT).show();
                                break;
                            case setTimeFormat:
                                Toast.makeText(SendOrderActivity.this, "设置显示时间格式成功", Toast.LENGTH_SHORT).show();
                                break;
                            case setAutoLigten:
                                Toast.makeText(SendOrderActivity.this, "设置自动点亮屏幕成功", Toast.LENGTH_SHORT).show();
                                break;
                            case setSitLongTimeAlert:
                                Toast.makeText(SendOrderActivity.this, "设置久坐提醒成功", Toast.LENGTH_SHORT).show();
                                break;
                            case setLastShow:
                                Toast.makeText(SendOrderActivity.this, "设置上次显示成功", Toast.LENGTH_SHORT).show();
                                break;
                            case setHeartRateInterval:
                                Toast.makeText(SendOrderActivity.this, "设置心率监测间隔成功", Toast.LENGTH_SHORT).show();
                                break;
                            case setFunctionDisplay:
                                Toast.makeText(SendOrderActivity.this, "设置功能显示成功", Toast.LENGTH_SHORT).show();
                                break;
                            case getBatteryDailyStepCount:
                                Toast.makeText(SendOrderActivity.this, "获取电量和记步总数成功", Toast.LENGTH_SHORT).show();
                                LogModule.i("电池电量：" + BluetoothModule.getInstance().getBatteryQuantity());
                                break;
                            case getSleepHeartCount:
                                Toast.makeText(SendOrderActivity.this, "获取睡眠和心率总数成功", Toast.LENGTH_SHORT).show();
                                break;
                            case getDailySteps:
                                Toast.makeText(SendOrderActivity.this, "获取记步数据成功", Toast.LENGTH_SHORT).show();
                                ArrayList<DailyStep> steps = BluetoothModule.getInstance().getDailySteps();
                                for (DailyStep step : steps) {
                                    LogModule.i(step.toString());
                                }
                                break;
                            case getDailySleepIndex:
                                Toast.makeText(SendOrderActivity.this, "获取睡眠数据成功", Toast.LENGTH_SHORT).show();
                                ArrayList<DailySleep> sleeps = BluetoothModule.getInstance().getDailySleeps();
                                for (DailySleep sleep : sleeps) {
                                    LogModule.i(sleep.toString());
                                }
                                break;
                            case getHeartRate:
                                Toast.makeText(SendOrderActivity.this, "获取心率数据成功", Toast.LENGTH_SHORT).show();
                                ArrayList<HeartRate> heartRates = BluetoothModule.getInstance().getHeartRates();
                                for (HeartRate heartRate : heartRates) {
                                    LogModule.i(heartRate.toString());
                                }
                                break;
                            case getTodayData:
                                Toast.makeText(SendOrderActivity.this, "获取当天数据成功", Toast.LENGTH_SHORT).show();
                                ArrayList<DailyStep> todaySteps = BluetoothModule.getInstance().getDailySteps();
                                for (DailyStep step : todaySteps) {
                                    LogModule.i(step.toString());
                                }
                                ArrayList<DailySleep> todaySleeps = BluetoothModule.getInstance().getDailySleeps();
                                for (DailySleep sleep : todaySleeps) {
                                    LogModule.i(sleep.toString());
                                }
                                ArrayList<HeartRate> todayHeartRates = BluetoothModule.getInstance().getHeartRates();
                                for (HeartRate heartRate : todayHeartRates) {
                                    LogModule.i(heartRate.toString());
                                }
                                break;
                        }
                    }
                }
                if (DemoConstant.ACTION_ORDER_TIMEOUT.equals(action)) {

                }
                if (DemoConstant.ACTION_ORDER_FINISH.equals(action)) {

                }
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBroadcastManager.unregisterReceiver(mReceiver);
        unbindService(mServiceConnection);
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = ((FitpoloService.LocalBinder) service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }
    };


    public void getInnerVersion(View view) {
        mService.getInnerVersion();
    }

    public void setSystemTime(View view) {
        mService.setSystemTime();
    }

    public void setUserInfo(View view) {
        UserInfo userInfo = new UserInfo();
        userInfo.age = 23;
        userInfo.gender = 0;
        userInfo.height = 170;
        userInfo.weight = 80;
        mService.setUserInfo(userInfo);
    }

    public void setBandAlarm(View view) {
        mService.setBandAlarm(new ArrayList<BandAlarm>());
    }

    public void setUnitType(View view) {
        mService.setUnitType(0);
    }

    public void setTimeFormat(View view) {
        mService.setTimeFormat(0);
    }

    public void setAutoLigten(View view) {
        mService.setAutoLigten(0);
    }

    public void setSitLongTimeAlert(View view) {
        SitLongTimeAlert alert = new SitLongTimeAlert();
        alert.alertSwitch = 0;
        alert.startTime = "11:00";
        alert.endTime = "18:00";
        mService.setSitLongTimeAlert(alert);
    }

    public void setLastShow(View view) {
        mService.setLastShow(1);
    }

    public void setHeartRateInterval(View view) {
        mService.setHeartRateInterval(3);
    }

    public void setFunctionDisplay(View view) {
        boolean[] functions = new boolean[]{true, true, true, true, true};
        mService.setFunctionDisplay(functions);
    }

    public void getBatteryDailyStepCount(View view) {
        mService.getBatteryDailyStepCount();
    }

    public void getSleepHeartCount(View view) {
        mService.getSleepHeartCount();
    }

    public void getDailySteps(View view) {
        mService.getDailySteps();
    }

    public void getDailySleeps(View view) {
        mService.getDailySleeps();
    }

    public void getHeartRate(View view) {
        mService.getHeartRate();
    }

    public void getTodayData(View view) {
        mService.getTodayData();
    }
}
