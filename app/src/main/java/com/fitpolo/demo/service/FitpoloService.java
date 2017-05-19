package com.fitpolo.demo.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import com.fitpolo.demo.DemoConstant;
import com.fitpolo.support.OrderEnum;
import com.fitpolo.support.bluetooth.BluetoothModule;
import com.fitpolo.support.callback.ConnStateCallback;
import com.fitpolo.support.callback.OrderCallback;
import com.fitpolo.support.callback.ScanDeviceCallback;
import com.fitpolo.support.entity.BaseResponse;
import com.fitpolo.support.entity.BleDevice;
import com.fitpolo.support.entity.HeartRate;
import com.fitpolo.support.entity.req.BandAlarm;
import com.fitpolo.support.entity.req.SitLongTimeAlert;
import com.fitpolo.support.entity.req.UserInfo;
import com.fitpolo.support.log.LogModule;
import com.fitpolo.support.task.AutoLightenTask;
import com.fitpolo.support.task.BandAlarmTask;
import com.fitpolo.support.task.BatteryDailyStepsCountTask;
import com.fitpolo.support.task.DailySleepIndexTask;
import com.fitpolo.support.task.DailyStepsTask;
import com.fitpolo.support.task.DailyTodayDataTask;
import com.fitpolo.support.task.FunctionDisplayTask;
import com.fitpolo.support.task.HeartRateIntervalTask;
import com.fitpolo.support.task.HeartRateTask;
import com.fitpolo.support.task.InnerVersionTask;
import com.fitpolo.support.task.LastShowTask;
import com.fitpolo.support.task.SitLongTimeAlertTask;
import com.fitpolo.support.task.SleepHeartCountTask;
import com.fitpolo.support.task.SystemTimeTask;
import com.fitpolo.support.task.TimeFormatTask;
import com.fitpolo.support.task.UnitTypeTask;
import com.fitpolo.support.task.UserInfoTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @Date 2017/5/17
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.fitpolo.demo.service.FitpoloService
 */
public class FitpoloService extends Service implements ScanDeviceCallback, ConnStateCallback, OrderCallback {
    private LocalBroadcastManager mBroadcastManager;
    private HashMap<String, BleDevice> mMap;
    private ArrayList<BleDevice> mDatas;

    @Override
    public void onCreate() {
        super.onCreate();
        mBroadcastManager = LocalBroadcastManager.getInstance(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    public void startScanDevice() {
        LogModule.i("开始扫描...");
        mMap = new HashMap<>();
        mDatas = new ArrayList<>();
        BluetoothModule.getInstance().startScanDevice(this);
    }

    public void startConnDevice(String address) {
        LogModule.i("开始连接设备...");
        BluetoothModule.getInstance().createBluetoothGatt(this, address, this);
    }

    @Override
    public void onStartScan() {
        mBroadcastManager.sendBroadcast(new Intent(DemoConstant.ACTION_START_SCAN));
    }

    @Override
    public void onScanDevice(BleDevice device) {
        mMap.put(device.name, device);
    }

    @Override
    public void onStopScan() {
        mDatas.addAll(mMap.values());
        Intent intent = new Intent(DemoConstant.ACTION_STOP_SCAN);
        intent.putExtra("devices", mDatas);
        mBroadcastManager.sendBroadcast(intent);
    }

    @Override
    public void onConnSuccess() {
        mBroadcastManager.sendBroadcast(new Intent(DemoConstant.ACTION_CONN_SUCCESS));
    }

    @Override
    public void onConnFailure(int errorCode) {
        mBroadcastManager.sendBroadcast(new Intent(DemoConstant.ACTION_CONN_FAILURE));
    }

    @Override
    public void onDisconnect() {
        mBroadcastManager.sendBroadcast(new Intent(DemoConstant.ACTION_DISCONNECT));
    }

    @Override
    public void onOrderResult(OrderEnum order, BaseResponse response) {
        Intent intent = new Intent(new Intent(DemoConstant.ACTION_ORDER_RESULT));
        intent.putExtra("order", order);
        intent.putExtra("response", response);
        mBroadcastManager.sendBroadcast(intent);
    }

    @Override
    public void onOrderTimeout(OrderEnum order) {
        Intent intent = new Intent(new Intent(DemoConstant.ACTION_ORDER_TIMEOUT));
        intent.putExtra("order", order);
        mBroadcastManager.sendBroadcast(intent);
    }

    @Override
    public void onOrderFinish() {
        mBroadcastManager.sendBroadcast(new Intent(DemoConstant.ACTION_ORDER_FINISH));
    }


    public void getInnerVersion() {
        LogModule.i("获取内部版本号...");
        InnerVersionTask task = new InnerVersionTask(this);
        BluetoothModule.getInstance().sendOrder(task);
    }

    public void setSystemTime() {
        LogModule.i("设置系统时间...");
        SystemTimeTask task = new SystemTimeTask(this);
        BluetoothModule.getInstance().sendOrder(task);
    }

    public void setUserInfo(UserInfo userInfo) {
        LogModule.i("设置用户信息...");
        UserInfoTask task = new UserInfoTask(this, userInfo);
        BluetoothModule.getInstance().sendOrder(task);
    }

    public void setBandAlarm(List<BandAlarm> alarms) {
        LogModule.i("设置闹钟数据...");
        BandAlarmTask task = new BandAlarmTask(this, alarms);
        BluetoothModule.getInstance().sendOrder(task);

    }

    public void setUnitType(int type) {
        LogModule.i("设置单位制式...");
        UnitTypeTask task = new UnitTypeTask(this, type);
        BluetoothModule.getInstance().sendOrder(task);
    }

    public void setTimeFormat(int timeFormat) {
        LogModule.i("设置显示时间格式...");
        TimeFormatTask task = new TimeFormatTask(this, timeFormat);
        BluetoothModule.getInstance().sendOrder(task);
    }
    public void setAutoLigten(int autoLighten) {
        LogModule.i("设置自动点亮屏幕...");
        AutoLightenTask task = new AutoLightenTask(this, autoLighten);
        BluetoothModule.getInstance().sendOrder(task);
    }
    public void setSitLongTimeAlert(SitLongTimeAlert alert) {
        LogModule.i("设置久坐提醒...");
        SitLongTimeAlertTask task = new SitLongTimeAlertTask(this, alert);
        BluetoothModule.getInstance().sendOrder(task);
    }
    public void setLastShow(int lastShow) {
        LogModule.i("设置上次显示...");
        LastShowTask task = new LastShowTask(this, lastShow);
        BluetoothModule.getInstance().sendOrder(task);
    }
    public void setHeartRateInterval(int heartRateInterval) {
        LogModule.i("设置心率监测间隔...");
        HeartRateIntervalTask task = new HeartRateIntervalTask(this, heartRateInterval);
        BluetoothModule.getInstance().sendOrder(task);
    }
    public void setFunctionDisplay(boolean[] functions) {
        LogModule.i("设置功能显示...");
        FunctionDisplayTask task = new FunctionDisplayTask(this, functions);
        BluetoothModule.getInstance().sendOrder(task);
    }
    public void getBatteryDailyStepCount() {
        LogModule.i("获取电量和记步总数...");
        BatteryDailyStepsCountTask task = new BatteryDailyStepsCountTask(this);
        BluetoothModule.getInstance().sendOrder(task);
    }
    public void getSleepHeartCount() {
        LogModule.i("获取睡眠和心率总数...");
        SleepHeartCountTask task = new SleepHeartCountTask(this);
        BluetoothModule.getInstance().sendOrder(task);
    }
    public void getDailySteps() {
        LogModule.i("获取记步数据...");
        DailyStepsTask task = new DailyStepsTask(this);
        BluetoothModule.getInstance().sendOrder(task);
    }
    public void getDailySleeps() {
        LogModule.i("获取睡眠数据...");
        DailySleepIndexTask task = new DailySleepIndexTask(this);
        BluetoothModule.getInstance().sendOrder(task);
    }
    public void getHeartRate() {
        LogModule.i("获取心率...");
        HeartRateTask task = new HeartRateTask(this);
        BluetoothModule.getInstance().sendOrder(task);
    }
    public void getTodayData() {
        LogModule.i("获取今天的记步，睡眠，心率数据...");
        DailyTodayDataTask task = new DailyTodayDataTask(this);
        BluetoothModule.getInstance().sendOrder(task);
    }

    public class LocalBinder extends Binder {
        public FitpoloService getService() {
            return FitpoloService.this;
        }
    }

    private IBinder mBinder = new LocalBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
}
