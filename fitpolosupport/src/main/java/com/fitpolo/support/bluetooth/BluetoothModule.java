package com.fitpolo.support.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Message;
import android.text.TextUtils;

import com.fitpolo.support.FitConstant;
import com.fitpolo.support.OrderEnum;
import com.fitpolo.support.callback.ConnStateCallback;
import com.fitpolo.support.callback.GattCallback;
import com.fitpolo.support.callback.OrderCallback;
import com.fitpolo.support.callback.ScanDeviceCallback;
import com.fitpolo.support.entity.BaseResponse;
import com.fitpolo.support.entity.BleDevice;
import com.fitpolo.support.entity.CRCVerifyResponse;
import com.fitpolo.support.entity.DailySleep;
import com.fitpolo.support.entity.DailyStep;
import com.fitpolo.support.entity.HeartRate;
import com.fitpolo.support.log.LogModule;
import com.fitpolo.support.task.BandAlarmTask;
import com.fitpolo.support.task.CRCVerifyTask;
import com.fitpolo.support.task.DailySleepRecordTask;
import com.fitpolo.support.task.NewDailySleepIndexTask;
import com.fitpolo.support.task.NewDailySleepRecordTask;
import com.fitpolo.support.task.OrderTask;
import com.fitpolo.support.utils.BaseHandler;
import com.fitpolo.support.utils.BleConnectionCompat;
import com.fitpolo.support.utils.ComplexDataParse;
import com.fitpolo.support.utils.DigitalConver;
import com.fitpolo.support.utils.Utils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @Date 2017/5/10
 * @Author wenzheng.liu
 * @Description 蓝牙工具类
 * @ClassPath com.fitpolo.support.bluetooth.BluetoothModule
 */
public class BluetoothModule {
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGattCharacteristic mNotifyCharacteristic;
    private BluetoothGatt mBluetoothGatt;
    private BlockingQueue<OrderTask> mQueue;
    private BluetoothGattCallback mGattCallback;
    private static final UUID SERVIE_UUID =
            UUID.fromString("0000ffc0-0000-1000-8000-00805f9b34fb");
    private static final UUID CHARACTERISTIC_DESCRIPTOR_UUID =
            UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    /**
     * Write, APP send command to wristbands using this characteristic
     */
    private static final UUID CHARACTERISTIC_UUID_WRITE =
            UUID.fromString("0000ffc1-0000-1000-8000-00805f9b34fb");
    /**
     * Notify, wristbands send data to APP using this characteristic
     */
    private static final UUID CHARACTERISTIC_UUID_NOTIFY =
            UUID.fromString("0000ffc2-0000-1000-8000-00805f9b34fb");
    private static final Object LOCK = new Object();


    private static volatile BluetoothModule INSTANCE;

    private BluetoothModule() {
        mQueue = new LinkedBlockingQueue<>();
    }

    public static BluetoothModule getInstance() {
        if (INSTANCE == null) {
            synchronized (BluetoothModule.class) {
                if (INSTANCE == null) {
                    INSTANCE = new BluetoothModule();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * @Date 2017/5/10
     * @Author wenzheng.liu
     * @Description 创建蓝牙适配器
     */
    public void createBluetoothAdapter(Context context) {
        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        mHandler = new ServiceHandler(this);
        mExecutorService = Executors.newSingleThreadExecutor();

    }

    /**
     * @Date 2017/5/10
     * @Author wenzheng.liu
     * @Description 扫描设备
     */
    public void startScanDevice(final ScanDeviceCallback callback) {
        final FitLeScanCallback fitLeScanCallback = new FitLeScanCallback(callback);
        mBluetoothAdapter.startLeScan(fitLeScanCallback);
        callback.onStartScan();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mBluetoothAdapter.stopLeScan(fitLeScanCallback);
                callback.onStopScan();
            }
        }, FitConstant.SCAN_PERIOD);
    }

    /**
     * @Date 2017/5/10
     * @Author wenzheng.liu
     * @Description 连接gatt
     */
    public void createBluetoothGatt(final Context context, String address, ConnStateCallback connCallBack) {
        if (isReConnecting) {
            LogModule.i("正在重连中...");
            return;
        }
        if (TextUtils.isEmpty(address)) {
            connCallBack.onConnFailure(FitConstant.CONN_ERROR_CODE_ADDRESS_NULL);
            return;
        }
        if (!isBluetoothOpen()) {
            connCallBack.onConnFailure(FitConstant.CONN_ERROR_CODE_BLUTOOTH_CLOSE);
            return;
        }
        if (isConnDevice(context, address)) {
            connCallBack.onConnFailure(FitConstant.CONN_ERROR_CODE_CONNECTED);
            return;
        }
        mDeviceAddress = address;
        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (mGattCallback == null) {
            mGattCallback = getBluetoothGattCallback(context, connCallBack);
        }
        disConnectBle();
        reConnectCount = 4;
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mBluetoothGatt = (new BleConnectionCompat(context)).connectGatt(device, false, mGattCallback);
            }
        });
    }

    /**
     * @Date 2017/5/11
     * @Author wenzheng.liu
     * @Description 发送命令
     */
    public void sendOrder(OrderTask... orderTasks) {
        if (orderTasks.length == 0) {
            return;
        }
        if (mQueue.isEmpty()) {
            for (OrderTask task : orderTasks) {
                mQueue.offer(task);
            }
            executeOrder(null);
        } else {
            for (OrderTask task : orderTasks) {
                mQueue.offer(task);
            }
        }
    }

    /**
     * @Date 2017/5/11
     * @Author wenzheng.liu
     * @Description 发送命令（不需要等待回复）
     */
    public void sendDirectOrder(OrderTask orderTask) {
        writeCharacteristicData(mBluetoothGatt, orderTask.assemble());
    }


    private String mDeviceAddress;
    private boolean isOpenReConnect;
    private int reConnectCount = 4;
    private boolean isReConnecting;
    private boolean isSupportHeartRate;
    private boolean isSupportTodayData;
    private boolean isSupportNewData;
    private boolean isShouldUpgrade;
    private String mFirmwareHeader;
    private String mFirmwareVersion;
    private String mInnerVersion;
    private String mLastChargeTime;
    private int mBatteryQuantity;
    private int mDailyStepCount;
    private int mSleepIndexCount;
    private int mSleepRecordCount;
    private int mHeartRateCount;
    private ArrayList<DailyStep> mDailySteps;
    private ArrayList<DailySleep> mDailySleeps;
    private HashMap<Integer, DailySleep> mSleepsMap;
    private ArrayList<HeartRate> mHeartRates;
    private HashMap<Integer, Boolean> mHeartRatesMap;
    private boolean mIsNewDataSuccess;


    /**
     * @param callback
     * @Date 2017/5/11
     * @Author wenzheng.liu
     * @Description 执行命令
     */
    private void executeOrder(OrderCallback callback) {
        if (callback != null && mQueue.isEmpty()) {
            callback.onOrderFinish();
            return;
        }
        final OrderTask task = mQueue.peek();
        writeCharacteristicData(mBluetoothGatt, task.assemble());
        switch (task.getOrder()) {
            case getNewDailySteps:
            case getNewDailySleepIndex:
            case getNewHeartRate:
                mIsNewDataSuccess = false;
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (task.getResponse().code != FitConstant.ORDER_CODE_SUCCESS && !mIsNewDataSuccess) {
                            task.getResponse().code = FitConstant.ORDER_CODE_ERROR_TIMEOUT;
                            switch (task.getOrder()) {
                                case getNewDailySteps:
                                    LogModule.i("获取未同步的记步个数超时");
                                    break;
                                case getNewDailySleepIndex:
                                    LogModule.i("获取未同步的睡眠记录个数超时");
                                    break;
                                case getNewHeartRate:
                                    LogModule.i("获取未同步的心率个数超时");
                                    break;
                            }
                            mQueue.poll();
                            task.getCallback().onOrderTimeout(task.getOrder());
                            executeOrder(task.getCallback());
                        }
                    }
                }, 3000);
                return;
            case getHeartRate:
                if (mHeartRateCount == 0) {
                    return;
                }
                mHeartRatesMap.put(mHeartRateCount, false);
                orderTimeoutHandlerHeartRate(task, mHeartRateCount);
                return;
        }
        orderTimeoutHandler(task);
    }

    /**
     * @Date 2017/7/12
     * @Author wenzheng.liu
     * @Description 心率每条都有超时判断，默认3s
     */
    private void orderTimeoutHandlerHeartRate(final OrderTask task, final int heartRateCount) {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!mHeartRatesMap.get(heartRateCount)) {
                    task.getResponse().code = FitConstant.ORDER_CODE_ERROR_TIMEOUT;
                    LogModule.i("获取心率第" + heartRateCount + "个数据超时");
                    if (mHeartRates != null && mHeartRates.size() > 0) {
                        Collections.sort(mHeartRates);
                    }
                    mQueue.poll();
                    task.getCallback().onOrderTimeout(task.getOrder());
                    executeOrder(task.getCallback());
                }
            }
        }, 3000);
    }

    /**
     * @Date 2017/5/11
     * @Author wenzheng.liu
     * @Description 超时处理
     */
    private void orderTimeoutHandler(final OrderTask task) {
        long delayTime = 3000;
        switch (task.getOrder()) {
            case getDailySteps:
            case getNewDailySteps:
                delayTime = mDailyStepCount == 0 ? delayTime : 3000 + 100 * mDailyStepCount;
                break;
            case getDailySleepIndex:
            case getNewDailySleepIndex:
                delayTime = mSleepIndexCount == 0 ? delayTime : 3000 + 100 * (mSleepIndexCount + mSleepRecordCount);
                break;
//            case getHeartRate:
//            case getNewHeartRate:
//                delayTime = mHeartRateCount == 0 ? delayTime : 3000;
//                break;
            case getTodayData:
                delayTime = 5000;
                break;
            case getCRCVerifyResult:
                delayTime = 5000;
                break;
        }
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (task.getResponse().code != FitConstant.ORDER_CODE_SUCCESS) {
                    task.getResponse().code = FitConstant.ORDER_CODE_ERROR_TIMEOUT;
                    switch (task.getOrder()) {
                        case getInnerVersion:
                            LogModule.i("获取内部版本超时");
                            break;
                        case getFirmwareVersion:
                            LogModule.i("获取固件版本超时");
                            break;
                        case getFirmwareParam:
                            LogModule.i("获取硬件参数超时");
                            break;
                        case getBatteryDailyStepCount:
                            LogModule.i("获取电量和记步总数超时");
                            break;
                        case getSleepHeartCount:
                            LogModule.i("获取睡眠和心率总数超时");
                            break;
                        case getDailySteps:
                            LogModule.i("获取记步数据超时");
                            if (mDailySteps != null && mDailySteps.size() > 0)
                                Collections.sort(mDailySteps);
                            break;
                        case getDailySleepIndex:
                            LogModule.i("获取睡眠index超时");
                            break;
//                        case getHeartRate:
//                            LogModule.i("获取心率数据超时");
//                            if (mHeartRates != null && mHeartRates.size() > 0) {
//                                Collections.sort(mHeartRates);
//                            }
//                            break;
                        case getTodayData:
                            LogModule.i("获取当天数据超时");
                            if (mHeartRates != null && mHeartRates.size() > 0)
                                Collections.sort(mHeartRates);
                            if (mDailySteps != null && mDailySteps.size() > 0)
                                Collections.sort(mDailySteps);
                            break;
                        case setBandAlarm:
                            LogModule.i("设置闹钟数据超时");
                            break;
                        case setAutoLigten:
                            LogModule.i("设置翻腕自动亮屏超时");
                            break;
                        case setSystemTime:
                            LogModule.i("设置手环时间超时");
                            break;
                        case setTimeFormat:
                            LogModule.i("设置显示时间格式超时");
                            break;
                        case setUserInfo:
                            LogModule.i("设置用户信息超时");
                            break;
                        case setUnitType:
                            LogModule.i("设置单位类型超时");
                            break;
                        case setSitLongTimeAlert:
                            LogModule.i("设置久坐提醒超时");
                            break;
                        case setLastShow:
                            LogModule.i("设置最后显示超时");
                            break;
                        case setHeartRateInterval:
                            LogModule.i("设置心率时间间隔超时");
                            break;
                        case setFunctionDisplay:
                            LogModule.i("设置功能显示超时");
                            break;
                        case setShakeBand:
                            LogModule.i("设置手环震动超时");
                            break;
                        case clearBandData:
                            LogModule.i("清除手环数据超时");
                            break;
                        case getNewDailySteps:
                            LogModule.i("获取未同步的记步数据超时");
                            if (mDailySteps != null && mDailySteps.size() > 0)
                                Collections.sort(mDailySteps);
                            break;
                        case getNewDailySleepIndex:
                            LogModule.i("获取未同步的睡眠记录数据超时");
                            if (mSleepsMap != null) {
                                mSleepsMap.clear();
                            }
                            break;
                        case getCRCVerifyResult:
                            LogModule.i("CRC校验超时");
                            break;
                        case disconnectBand:
                            LogModule.i("断开手环超时");
                            break;
//                        case getNewHeartRate:
//                            LogModule.i("获取未同步的心率数据超时");
//                            if (mHeartRates != null && mHeartRates.size() > 0)
//                                Collections.sort(mHeartRates);
//                            break;
                    }
                    mQueue.poll();
                    task.getCallback().onOrderTimeout(task.getOrder());
                    executeOrder(task.getCallback());
                }
            }
        }, delayTime);
    }

    /**
     * @Date 2017/5/10
     * @Author wenzheng.liu
     * @Description 判断是否已连接手环
     */
    public boolean isConnDevice(Context context, String address) {
        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        int connState = bluetoothManager.getConnectionState(mBluetoothAdapter.getRemoteDevice(address), BluetoothProfile.GATT);
        return connState == BluetoothProfile.STATE_CONNECTED && mNotifyCharacteristic != null;
    }

    /**
     * @Date 2017/6/22
     * @Author wenzheng.liu
     * @Description 正在同步
     */
    public boolean isSyncData() {
        return mQueue != null && !mQueue.isEmpty();
    }

    /**
     * @Date 2017/5/12
     * @Author wenzheng.liu
     * @Description 是否支持心率
     */
    public boolean isSupportHeartRate() {
        return isSupportHeartRate;
    }

    /**
     * @Date 2017/5/12
     * @Author wenzheng.liu
     * @Description 是否支持同步当天数据
     */
    public boolean isSupportTodayData() {
        return isSupportTodayData;
    }

    /**
     * @Date 2017/5/12
     * @Author wenzheng.liu
     * @Description 是否支持同步未同步的数据
     */
    public boolean isSupportNewData() {
        return isSupportNewData;
    }

    /**
     * @Date 2017/8/14
     * @Author wenzheng.liu
     * @Description 是否需要升级
     */
    public boolean isShouldUpgrade() {
        return isShouldUpgrade;
    }

    /**
     * @Date 2017/9/7
     * @Author wenzheng.liu
     * @Description 获取固件版本头
     */
    public String getFirmwareHeader() {
        return mFirmwareHeader;
    }

    /**
     * @Date 2017/5/14 0014
     * @Author wenzheng.liu
     * @Description 设置重连
     */
    public void setOpenReConnect(boolean openReConnect) {
        LogModule.i(openReConnect ? "打开重连" : "关闭重连");
        isOpenReConnect = openReConnect;
    }

    /**
     * @Date 2017/8/29
     * @Author wenzheng.liu
     * @Description 获取重连次数
     */
    public int getReconnectCount() {
        return reConnectCount;
    }

    /**
     * @Date 2017/8/29
     * @Author wenzheng.liu
     * @Description 设置重连次数
     */
    public void setReconnectCount(int reConnectCount) {
        this.reConnectCount = reConnectCount;
    }

    /**
     * @Date 2017/5/15
     * @Author wenzheng.liu
     * @Description 获取固件版本号
     */
    public String getFirmwareVersion() {
        return mFirmwareVersion;
    }

    /**
     * @Date 2017/11/22
     * @Author wenzheng.liu
     * @Description 获取内部版本号
     */
    public String getInnerVersion() {
        return mInnerVersion;
    }

    /**
     * @Date 2017/5/15
     * @Author wenzheng.liu
     * @Description 获取电池电量
     */
    public int getBatteryQuantity() {
        return mBatteryQuantity;
    }

    /**
     * @Date 2017/12/6 0006
     * @Author wenzheng.liu
     * @Description 获取最后充电时间
     */
    public String getLastChargeTime() {
        return mLastChargeTime;
    }

    /**
     * @Date 2017/5/15
     * @Author wenzheng.liu
     * @Description 获取记步数据
     */
    public ArrayList<DailyStep> getDailySteps() {
        return mDailySteps;
    }

    /**
     * @Date 2017/5/15
     * @Author wenzheng.liu
     * @Description 获取睡眠数据
     */
    public ArrayList<DailySleep> getDailySleeps() {
        return mDailySleeps;
    }

    /**
     * @Date 2017/5/15
     * @Author wenzheng.liu
     * @Description 获取心率数据
     */
    public ArrayList<HeartRate> getHeartRates() {
        return mHeartRates;
    }

    /**
     * @Date 2017/5/21 0021
     * @Author wenzheng.liu
     * @Description 获取验证码
     */
    public String getVerifyCode(byte[] scanRecord) {
        String verifyCode;
        int index = 0;
        for (int i = 0; i < scanRecord.length; i++) {
            if ("0A".equals(DigitalConver.byte2HexString(scanRecord[i]).trim())
                    && "FF".equals(DigitalConver.byte2HexString(scanRecord[i + 1]).trim())) {
                index = i + 6;
                break;
            }
        }
        if (index == 0) {
            return "";
        }
        verifyCode = DigitalConver.byte2HexString(scanRecord[index]).trim() + DigitalConver.byte2HexString(scanRecord[index + 1]).trim();
        return verifyCode;
    }

    class FitLeScanCallback implements BluetoothAdapter.LeScanCallback {
        private ScanDeviceCallback mCallback;

        public FitLeScanCallback(ScanDeviceCallback callback) {
            this.mCallback = callback;
        }

        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            if (device != null) {
                if (TextUtils.isEmpty(device.getName())) {
                    return;
                }
                BleDevice bleDevice = new BleDevice();
                bleDevice.name = device.getName();
                bleDevice.address = device.getAddress();
                bleDevice.rssi = rssi;
                bleDevice.scanRecord = scanRecord;
                mCallback.onScanDevice(bleDevice);
            }
        }
    }

    /**
     * @Date 2017/5/10
     * @Author wenzheng.liu
     * @Description 蓝牙是否开启
     */
    public boolean isBluetoothOpen() {
        return mBluetoothAdapter != null && mBluetoothAdapter.isEnabled();
    }

    /**
     * @Date 2017/5/10
     * @Author wenzheng.liu
     * @Description 断开gattt
     */
    public void disConnectBle() {
        reConnectCount = 0;
        if (mBluetoothGatt != null) {
            if (refreshDeviceCache()) {
                LogModule.i("清理GATT层蓝牙缓存");
            }
            LogModule.i("断开连接");
            synchronized (LOCK) {
                mNotifyCharacteristic = null;
            }
            mBluetoothGatt.disconnect();
        }
    }

    /**
     * @param context
     * @param connCallBack
     * @Date 2017/5/10
     * @Author wenzheng.liu
     * @Description 获取蓝牙连接回调
     */
    private BluetoothGattCallback getBluetoothGattCallback(final Context context, final ConnStateCallback connCallBack) {
        BluetoothGattCallback callback = new CustomGattCallback(new GattCallback() {
            @Override
            public void onServicesDiscovered() {
                setCharacteristicNotify(mBluetoothGatt);
                connCallBack.onConnSuccess();
            }

            @Override
            public void onConnSuccess() {
                mBluetoothGatt.discoverServices();
                isReConnecting = false;
            }

            @Override
            public void onConnFailure() {
                if (mBluetoothGatt != null) {
                    mBluetoothGatt.close();
                    mBluetoothGatt = null;
                }
                if (!mQueue.isEmpty()) {
                    mQueue.clear();
                }
                connCallBack.onConnFailure(FitConstant.CONN_ERROR_CODE_FAILURE);
                startReConnect(context, connCallBack);
            }

            @Override
            public void onDisConn() {
                if (mBluetoothGatt != null) {
                    mBluetoothGatt.close();
                    mBluetoothGatt = null;
                }
                if (!mQueue.isEmpty()) {
                    mQueue.clear();
                }
                connCallBack.onDisconnect();
                startReConnect(context, connCallBack);
            }

            @Override
            public void onResponse(BluetoothGattCharacteristic characteristic) {
                byte[] data = characteristic.getValue();
                LogModule.i("接收数据：");
                String[] formatDatas = DigitalConver.formatData(data);
                OrderTask task = mQueue.peek();
                if (formatDatas != null && formatDatas.length > 0 && task != null) {
                    switch (task.getOrder()) {
                        case getInnerVersion:
                            if ("09".equals(formatDatas[1])) {
                                LogModule.i("获取内部版本成功");
                                formatInnerTask(formatDatas, task);
                            }
                            break;
                        case getFirmwareVersion:
                            if ("90".equals(formatDatas[0])) {
                                LogModule.i("获取固件版本成功");
                                formatFirmwareTask(formatDatas, task);
                            }
                            break;
                        case getFirmwareParam:
                            if ("A5".equals(formatDatas[0])) {
                                LogModule.i("获取硬件参数成功");
                                formatFirmwareParamTask(formatDatas, task);
                            }
                            break;
                        case getBatteryDailyStepCount:
                            if ("91".equals(formatDatas[0])) {
                                LogModule.i("获取电量和记步总数成功");
                                formatBatteryDailyStepCountTask(formatDatas, task);
                            }
                            break;
                        case getSleepHeartCount:
                            if ("12".equals(formatDatas[1])) {
                                LogModule.i("获取睡眠和心率总数成功");
                                formatSleepHeartCountTask(formatDatas, task);
                            }
                            break;
                        case getDailySteps:
                            if ("92".equals(formatDatas[0])) {
                                LogModule.i("获取记步数据成功");
                                formatDailyStepsTask(formatDatas, task);
                            }
                            break;
                        case getDailySleepIndex:
                            if (mSleepIndexCount == 0 && !mDailySleeps.isEmpty()) {
                                if ("94".equals(formatDatas[0])) {
                                    LogModule.i("获取睡眠record成功");
                                    formatDailySleepRecordTask(formatDatas, task);
                                }
                            } else {
                                if ("93".equals(formatDatas[0])) {
                                    LogModule.i("获取睡眠index成功");
                                    formatDailySleepIndexTask(formatDatas, task);
                                }
                            }
                            break;
                        case getDailySleepRecord:
                            if ("94".equals(formatDatas[0])) {
                                LogModule.i("获取睡眠record成功");
                                formatDailySleepRecordTask(formatDatas, task);
                            }
                            break;
                        case getHeartRate:
                            if ("18".equals(formatDatas[1]) || "A8".equals(formatDatas[0])) {
                                LogModule.i("获取心率数据成功");
                                formatHeartRateTask(formatDatas, task);
                            }
                            break;
                        case getTodayData:
                            LogModule.i("获取当天数据成功");
                            formatTodayDataTask(formatDatas, task);
                            break;
                        case getNewDailySteps:
                            if ("92".equals(formatDatas[0]) || "92".equals(formatDatas[1])) {
                                LogModule.i("获取未同步的记步数据成功");
                                formatNewDailyStepsTask(formatDatas, task);
                            }
                            break;
                        case getNewDailySleepIndex:
                            if (mSleepIndexCount == 0 && mSleepsMap != null && !mSleepsMap.isEmpty()) {
                                if ("94".equals(formatDatas[0]) || "94".equals(formatDatas[1])) {
                                    LogModule.i("获取未同步的睡眠详情数据成功");
                                    formatNewDailySleepRecordTask(formatDatas, task);
                                }
                            } else {
                                if ("93".equals(formatDatas[0]) || "93".equals(formatDatas[1])) {
                                    LogModule.i("获取未同步的睡眠记录数据成功");
                                    formatNewDailySleepIndex(formatDatas, task);
                                }
                            }
                            break;
                        case getNewDailySleepRecord:
                            if ("94".equals(formatDatas[0]) || "94".equals(formatDatas[1])) {
                                LogModule.i("获取未同步的睡眠详情数据成功");
                                formatNewDailySleepRecordTask(formatDatas, task);
                            }
                            break;
                        case getNewHeartRate:
                            if ("A8".equals(formatDatas[0]) || "A8".equals(formatDatas[1])) {
                                LogModule.i("获取未同步的心率数据成功");
                                formatNewHeartRateTask(formatDatas, task);
                            }
                            break;
                        case setBandAlarm:
                            if ("26".equals(formatDatas[1])) {
                                LogModule.i("设置闹钟数据成功");
                                formatBandAlarmTask(task);
                            }
                            break;
                        case setAutoLigten:
                            if ("25".equals(formatDatas[1])) {
                                LogModule.i("设置翻腕自动亮屏成功");
                                formatCommonOrder(task);
                            }
                            break;
                        case setSystemTime:
                            if ("11".equals(formatDatas[1])) {
                                LogModule.i("设置手环时间成功");
                                formatCommonOrder(task);
                            }
                            break;
                        case setTimeFormat:
                            if ("24".equals(formatDatas[1])) {
                                LogModule.i("设置显示时间格式成功");
                                formatCommonOrder(task);
                            }
                            break;
                        case setUserInfo:
                            if ("12".equals(formatDatas[1])) {
                                LogModule.i("设置用户信息成功");
                                formatCommonOrder(task);
                            }
                            break;
                        case setUnitType:
                            if ("23".equals(formatDatas[1])) {
                                LogModule.i("设置单位类型成功");
                                formatCommonOrder(task);
                            }
                            break;
                        case setSitLongTimeAlert:
                            if ("2A".equals(formatDatas[1])) {
                                LogModule.i("设置久坐提醒成功");
                                formatCommonOrder(task);
                            }
                            break;
                        case setLastShow:
                            if ("27".equals(formatDatas[1])) {
                                LogModule.i("设置最后显示成功");
                                formatCommonOrder(task);
                            }
                            break;
                        case setHeartRateInterval:
                            if ("17".equals(formatDatas[1])) {
                                LogModule.i("设置心率时间间隔成功");
                                formatCommonOrder(task);
                            }
                            break;
                        case setFunctionDisplay:
                            if ("19".equals(formatDatas[1])) {
                                LogModule.i("设置功能显示成功");
                                formatCommonOrder(task);
                            }
                            break;
                        case setShakeBand:
                            LogModule.i("设置手环震动成功");
                            formatCommonOrder(task);
                            break;
                        case clearBandData:
                            LogModule.i("清除手环数据成功");
                            formatCommonOrder(task);
                            break;
                        case getCRCVerifyResult:
                            if ("28".equals(formatDatas[1]) || "A6".equals(formatDatas[0]) || "A7".equals(formatDatas[0])) {
                                formatCRCResult(formatDatas, task, data);
                            }
                            break;
                        case disconnectBand:
                            if ("16".equals(formatDatas[1])) {
                                LogModule.i("断开手环成功");
                                formatCommonOrder(task);
                            }
                            break;
                    }
                }

            }
        });
        return callback;
    }

    private void formatCRCResult(String[] formatDatas, OrderTask task, byte[] data) {
        CRCVerifyTask crcVerifyTask = (CRCVerifyTask) task;
        CRCVerifyResponse response = (CRCVerifyResponse) crcVerifyTask.getResponse();
        int header = Integer.parseInt(DigitalConver.decodeToString(formatDatas[0]));
        response.header = header;
        if (header == FitConstant.RESPONSE_HEADER_ACK) {
            LogModule.i("CRC校验成功！");
        } else if (header == FitConstant.RESPONSE_HEADER_PACKAGE) {
            response.code = FitConstant.ORDER_CODE_SUCCESS;
            byte[] index = new byte[2];
            index[0] = data[1];
            index[1] = data[2];
            response.packageResult = index;
            crcVerifyTask.getCallback().onOrderResult(crcVerifyTask.getOrder(), response);
        } else if (header == FitConstant.RESPONSE_HEADER_PACKAGE_RESULT) {
            response.code = FitConstant.ORDER_CODE_SUCCESS;
            int ack = Integer.parseInt(DigitalConver.decodeToString(formatDatas[1]));
            response.ack = ack;
            mQueue.poll();
            crcVerifyTask.getCallback().onOrderResult(crcVerifyTask.getOrder(), response);
            executeOrder(crcVerifyTask.getCallback());
        }
    }

    private void formatNewHeartRateTask(String[] formatDatas, OrderTask task) {
        mIsNewDataSuccess = true;
        BaseResponse response = task.getResponse();
        int header = Integer.parseInt(DigitalConver.decodeToString(formatDatas[0]));
        if (header == FitConstant.RESPONSE_HEADER_NEW_DATA_COUNT) {
            mHeartRateCount = Integer.parseInt(DigitalConver.decodeToString(formatDatas[2]));
            LogModule.i("有" + mHeartRateCount + "条心率数据");
            if (mHeartRates != null) {
                mHeartRates.clear();
            } else {
                mHeartRates = new ArrayList<>();
            }
            if (mHeartRatesMap != null) {
                mHeartRatesMap.clear();
            } else {
                mHeartRatesMap = new HashMap<>();
            }
            if (mHeartRateCount != 0) {
                // 拿到条数后再启动超时任务
                mHeartRatesMap.put(mHeartRateCount, false);
                orderTimeoutHandlerHeartRate(task, mHeartRateCount);
            }
        }
        if (header == FitConstant.RESPONSE_HEADER_HEART_RATE) {
            if (mHeartRateCount > 0) {
                if (formatDatas.length <= 2 || task.getResponse().code == FitConstant.ORDER_CODE_ERROR_TIMEOUT)
                    return;
                mHeartRatesMap.put(mHeartRateCount, true);
                ComplexDataParse.parseHeartRate(formatDatas, mHeartRates);
                mHeartRateCount--;
                if (mHeartRateCount > 0) {
                    LogModule.i("还有" + mHeartRateCount + "条心率数据未同步");
                    mHeartRatesMap.put(mHeartRateCount, false);
                    orderTimeoutHandlerHeartRate(task, mHeartRateCount);
                    return;
                }
            }
        }
        if (mHeartRateCount != 0) {
            return;
        }
        // 对心率数据做判重处理，避免时间重复造成的数据问题
        HashMap<String, HeartRate> removeRepeatMap = new HashMap<>();
        for (HeartRate heartRate : mHeartRates) {
            removeRepeatMap.put(heartRate.time, heartRate);
        }
        mHeartRates.clear();
        mHeartRates.addAll(removeRepeatMap.values());
        Collections.sort(mHeartRates);
        response.code = FitConstant.ORDER_CODE_SUCCESS;
        mQueue.poll();
        task.getCallback().onOrderResult(task.getOrder(), response);
        executeOrder(task.getCallback());
    }

    private void formatNewDailySleepRecordTask(String[] formatDatas, OrderTask task) {
        BaseResponse response = task.getResponse();
        int header = Integer.parseInt(DigitalConver.decodeToString(formatDatas[0]));
        if (header == FitConstant.RESPONSE_HEADER_NEW_DATA_COUNT) {
            mSleepRecordCount = Integer.parseInt(DigitalConver.decodeToString(formatDatas[2]));
            LogModule.i("有" + mSleepRecordCount + "条睡眠record");
        }
        if (header == FitConstant.RESPONSE_HEADER_SLEEP_RECORD) {
            if (mSleepRecordCount > 0) {
                // 处理record
                ComplexDataParse.parseDailySleepRecord(formatDatas, mSleepsMap);
                mSleepRecordCount--;
                if (mSleepRecordCount > 0) {
                    LogModule.i("还有" + mSleepRecordCount + "条睡眠record数据未同步");
                    return;
                }
            }
        }
        if (mSleepRecordCount != 0) {
            return;
        }
        mSleepsMap.clear();
        response.code = FitConstant.ORDER_CODE_SUCCESS;
        // 重新设置回index头，方便前端调用
        task.setOrder(OrderEnum.getDailySleepIndex);
        mQueue.poll();
        task.getCallback().onOrderResult(task.getOrder(), response);
        executeOrder(task.getCallback());
    }

    private void formatNewDailySleepIndex(String[] formatDatas, OrderTask task) {
        mIsNewDataSuccess = true;
        BaseResponse response = task.getResponse();
        int header = Integer.parseInt(DigitalConver.decodeToString(formatDatas[0]));
        if (header == FitConstant.RESPONSE_HEADER_NEW_DATA_COUNT) {
            mSleepIndexCount = Integer.parseInt(DigitalConver.decodeToString(formatDatas[2]));
            LogModule.i("有" + mSleepIndexCount + "条睡眠index");
            if (mDailySleeps != null) {
                mDailySleeps.clear();
            } else {
                mDailySleeps = new ArrayList<>();
            }
            if (mSleepsMap != null) {
                mSleepsMap.clear();
            } else {
                mSleepsMap = new HashMap<>();
            }
            mSleepRecordCount = mSleepIndexCount * 2;
            // 拿到条数后再启动超时任务
            orderTimeoutHandler(task);
        }
        if (header == FitConstant.RESPONSE_HEADER_SLEEP_INDEX) {
            if (mSleepIndexCount > 0) {
                mDailySleeps.add(ComplexDataParse.parseDailySleepIndex(formatDatas, mSleepsMap));
                mSleepIndexCount--;
                if (mSleepIndexCount > 0) {
                    LogModule.i("还有" + mSleepIndexCount + "条睡眠index数据未同步");
                    return;
                }
            }
        }
        if (!mDailySleeps.isEmpty()) {
            // 请求完index后请求record
            NewDailySleepRecordTask newDailySleepRecordTask = new NewDailySleepRecordTask(task.getCallback(), ((NewDailySleepIndexTask) task).lastSyncTime);
            writeCharacteristicData(mBluetoothGatt, newDailySleepRecordTask.assemble());
        } else {
            if (mSleepIndexCount != 0) {
                return;
            }
            response.code = FitConstant.ORDER_CODE_SUCCESS;
            mQueue.poll();
            task.getCallback().onOrderResult(task.getOrder(), response);
            executeOrder(task.getCallback());
        }
    }

    private void formatNewDailyStepsTask(String[] formatDatas, OrderTask task) {
        mIsNewDataSuccess = true;
        BaseResponse response = task.getResponse();
        int header = Integer.parseInt(DigitalConver.decodeToString(formatDatas[0]));
        if (header == FitConstant.RESPONSE_HEADER_NEW_DATA_COUNT) {
            mDailyStepCount = Integer.parseInt(DigitalConver.decodeToString(formatDatas[2]));
            LogModule.i("有" + mDailyStepCount + "条记步数据");
            if (mDailySteps != null) {
                mDailySteps.clear();
            } else {
                mDailySteps = new ArrayList<>();
            }
            // 拿到条数后再启动超时任务
            orderTimeoutHandler(task);
        }
        if (header == FitConstant.RESPONSE_HEADER_STEP) {
            if (mDailyStepCount > 0) {
                mDailySteps.add(ComplexDataParse.parseDailyStep(formatDatas));
                mDailyStepCount--;
                if (mDailyStepCount > 0) {
                    LogModule.i("还有" + mDailyStepCount + "条记步数据未同步");
                    return;
                }
            }
        }
        if (mDailyStepCount != 0) {
            return;
        }
        Collections.sort(mDailySteps);
        response.code = FitConstant.ORDER_CODE_SUCCESS;
        mQueue.poll();
        task.getCallback().onOrderResult(task.getOrder(), response);
        executeOrder(task.getCallback());
    }

    private void formatTodayDataTask(String[] formatDatas, OrderTask task) {
        BaseResponse response = task.getResponse();
        int header = Integer.parseInt(DigitalConver.decodeToString(formatDatas[0]));
        if (header == FitConstant.RESPONSE_HEADER_STEP) {
            if (mDailyStepCount > 0) {
                mDailySteps.add(ComplexDataParse.parseDailyStep(formatDatas));
                mDailyStepCount--;
                if (mDailyStepCount > 0) {
                    LogModule.i("还有" + mDailyStepCount + "条记步数据未同步");
                    return;
                } else if (mSleepIndexCount != 0 || mSleepRecordCount != 0 || mHeartRateCount != 0) {
                    return;
                }
            }
        } else if (header == FitConstant.RESPONSE_HEADER_SLEEP_INDEX) {
            if (mSleepIndexCount > 0) {
                mDailySleeps.add(ComplexDataParse.parseDailySleepIndex(formatDatas, mSleepsMap));
                mSleepIndexCount--;
                if (mSleepIndexCount > 0) {
                    LogModule.i("还有" + mSleepIndexCount + "条睡眠index数据未同步");
                }
            }
            return;
        } else if (header == FitConstant.RESPONSE_HEADER_SLEEP_RECORD) {
            if (mSleepRecordCount > 0) {
                // 处理record
                ComplexDataParse.parseDailySleepRecord(formatDatas, mSleepsMap);
                mSleepRecordCount--;
                if (mSleepRecordCount > 0) {
                    LogModule.i("还有" + mSleepRecordCount + "条睡眠record数据未同步");
                }
            }
            return;
        } else if (header == FitConstant.RESPONSE_HEADER_HEART_RATE) {
            if (mHeartRateCount > 0) {
                ComplexDataParse.parseHeartRate(formatDatas, mHeartRates);
                mHeartRateCount--;
                if (mHeartRateCount > 0) {
                    LogModule.i("还有" + mHeartRateCount + "条心率数据未同步");
                    return;
                }
            }
            Collections.sort(mHeartRates);
        } else {
            mDailyStepCount = 1;
            mSleepIndexCount = Integer.parseInt(DigitalConver.decodeToString(formatDatas[2]));
            mSleepRecordCount = Integer.parseInt(DigitalConver.decodeToString(formatDatas[3]));
            mHeartRateCount = Integer.parseInt(DigitalConver.decodeToString(formatDatas[4]));
            LogModule.i("有" + mSleepIndexCount + "条睡眠index");
            LogModule.i("有" + mSleepRecordCount + "条睡眠record");
            LogModule.i("有" + mHeartRateCount + "条心率数据");
            if (mDailySteps != null) {
                mDailySteps.clear();
            } else {
                mDailySteps = new ArrayList<>();
            }
            if (mDailySleeps != null) {
                mDailySleeps.clear();
            } else {
                mDailySleeps = new ArrayList<>();
            }
            if (mSleepsMap != null) {
                mSleepsMap.clear();
            } else {
                mSleepsMap = new HashMap<>();
            }
            if (mHeartRates != null) {
                mHeartRates.clear();
            } else {
                mHeartRates = new ArrayList<>();
            }
            return;
        }
        response.code = FitConstant.ORDER_CODE_SUCCESS;
        mQueue.poll();
        task.getCallback().onOrderResult(task.getOrder(), response);
        executeOrder(task.getCallback());
    }

    private void formatDailyStepsTask(String[] formatDatas, OrderTask task) {
        BaseResponse response = task.getResponse();
        if (mDailyStepCount > 0) {
            if (mDailySteps == null) {
                mDailySteps = new ArrayList<>();
            }
            mDailySteps.add(ComplexDataParse.parseDailyStep(formatDatas));
            mDailyStepCount--;
            if (mDailyStepCount > 0) {
                LogModule.i("还有" + mDailyStepCount + "条记步数据未同步");
                return;
            }
        }
        Collections.sort(mDailySteps);
        response.code = FitConstant.ORDER_CODE_SUCCESS;
        mQueue.poll();
        task.getCallback().onOrderResult(task.getOrder(), response);
        executeOrder(task.getCallback());
    }

    private void formatHeartRateTask(String[] formatDatas, OrderTask task) {
        BaseResponse response = task.getResponse();
        if (mHeartRateCount > 0) {
            if (mHeartRates == null) {
                mHeartRates = new ArrayList<>();
            }
            if (mHeartRatesMap == null) {
                mHeartRatesMap = new HashMap<>();
            }
            if (formatDatas.length <= 2 || task.getResponse().code == FitConstant.ORDER_CODE_ERROR_TIMEOUT)
                return;
            mHeartRatesMap.put(mHeartRateCount, true);
            ComplexDataParse.parseHeartRate(formatDatas, mHeartRates);
            mHeartRateCount--;
            if (mHeartRateCount > 0) {
                LogModule.i("还有" + mHeartRateCount + "条心率数据未同步");
                mHeartRatesMap.put(mHeartRateCount, false);
                orderTimeoutHandlerHeartRate(task, mHeartRateCount);
                return;
            }
        }
        Collections.sort(mHeartRates);
        response.code = FitConstant.ORDER_CODE_SUCCESS;
        mQueue.poll();
        task.getCallback().onOrderResult(task.getOrder(), response);
        executeOrder(task.getCallback());
    }

    private void formatDailySleepIndexTask(String[] formatDatas, OrderTask task) {
        BaseResponse response = task.getResponse();
        if (mSleepIndexCount > 0) {
            if (mDailySleeps == null) {
                mDailySleeps = new ArrayList<>();
            }
            if (mSleepsMap == null) {
                mSleepsMap = new HashMap<>();
            }
            mDailySleeps.add(ComplexDataParse.parseDailySleepIndex(formatDatas, mSleepsMap));
            mSleepIndexCount--;
            if (mSleepIndexCount > 0) {
                LogModule.i("还有" + mSleepIndexCount + "条睡眠index数据未同步");
                return;
            }
        }
        if (!mDailySleeps.isEmpty()) {
            // 请求完index后请求record
            DailySleepRecordTask sleepRecordTask = new DailySleepRecordTask(task.getCallback());
            writeCharacteristicData(mBluetoothGatt, sleepRecordTask.assemble());
        } else {
            response.code = FitConstant.ORDER_CODE_SUCCESS;
            mQueue.poll();
            task.getCallback().onOrderResult(task.getOrder(), response);
            executeOrder(task.getCallback());
        }
    }

    private void formatDailySleepRecordTask(String[] formatDatas, OrderTask task) {
        BaseResponse response = task.getResponse();
        if (mSleepRecordCount > 0) {
            // 处理record
            ComplexDataParse.parseDailySleepRecord(formatDatas, mSleepsMap);
            mSleepRecordCount--;
            if (mSleepRecordCount > 0) {
                LogModule.i("还有" + mSleepRecordCount + "条睡眠record数据未同步");
                return;
            }
        }
        mSleepsMap.clear();
        response.code = FitConstant.ORDER_CODE_SUCCESS;
        // 重新设置回index头，方便前端调用
        mQueue.poll();
        task.setOrder(OrderEnum.getDailySleepIndex);
        task.getCallback().onOrderResult(task.getOrder(), response);
        executeOrder(task.getCallback());
    }

    private void formatSleepHeartCountTask(String[] formatDatas, OrderTask task) {
        BaseResponse response = task.getResponse();
        response.code = FitConstant.ORDER_CODE_SUCCESS;
        mSleepIndexCount = Integer.parseInt(DigitalConver.decodeToString(formatDatas[2]));
        mSleepRecordCount = Integer.parseInt(DigitalConver.decodeToString(formatDatas[3]));
        mHeartRateCount = Integer.parseInt(DigitalConver.decodeToString(formatDatas[4]));
        LogModule.i("有" + mSleepIndexCount + "条睡眠index");
        LogModule.i("有" + mSleepRecordCount + "条睡眠record");
        LogModule.i("有" + mHeartRateCount + "条心率数据");
        if (mDailySleeps != null) {
            mDailySleeps.clear();
        } else {
            mDailySleeps = new ArrayList<>();
        }
        if (mSleepsMap != null) {
            mSleepsMap.clear();
        } else {
            mSleepsMap = new HashMap<>();
        }
        if (mHeartRates != null) {
            mHeartRates.clear();
        } else {
            mHeartRates = new ArrayList<>();
        }
        if (mHeartRatesMap != null) {
            mHeartRatesMap.clear();
        } else {
            mHeartRatesMap = new HashMap<>();
        }
        mQueue.poll();
        task.getCallback().onOrderResult(task.getOrder(), response);
        executeOrder(task.getCallback());
    }

    private void formatBatteryDailyStepCountTask(String[] formatDatas, OrderTask task) {
        BaseResponse response = task.getResponse();
        response.code = FitConstant.ORDER_CODE_SUCCESS;
        mDailyStepCount = Integer.parseInt(DigitalConver.decodeToString(formatDatas[1]));
        LogModule.i("有" + mDailyStepCount + "条记步数据");
        if (mDailySteps != null) {
            mDailySteps.clear();
        } else {
            mDailySteps = new ArrayList<>();
        }
        mBatteryQuantity = Integer.parseInt(DigitalConver.decodeToString(formatDatas[3]));
        mQueue.poll();
        task.getCallback().onOrderResult(task.getOrder(), response);
        executeOrder(task.getCallback());
    }

    private void formatFirmwareTask(String[] formatDatas, OrderTask task) {
        BaseResponse response = task.getResponse();
        response.code = FitConstant.ORDER_CODE_SUCCESS;
        int major = Integer.parseInt(DigitalConver.decodeToString(formatDatas[1]));
        int minor = Integer.parseInt(DigitalConver.decodeToString(formatDatas[2]));
        int revision = Integer.parseInt(DigitalConver.decodeToString(formatDatas[3]));
        String version = String.format("%s.%s.%s", major, minor, revision);
        mFirmwareVersion = version;
        mQueue.poll();
        task.getCallback().onOrderResult(task.getOrder(), response);
        executeOrder(task.getCallback());
    }

    private void formatFirmwareParamTask(String[] formatDatas, OrderTask task) {
        BaseResponse response = task.getResponse();
        response.code = FitConstant.ORDER_CODE_SUCCESS;
        if (formatDatas.length > 14) {
            LogModule.i("flash状态：" + formatDatas[2]);
            LogModule.i("当前反光阈值：" + DigitalConver.decodeToString(formatDatas[3] + formatDatas[4]));
            LogModule.i("当前反光值：" + DigitalConver.decodeToString(formatDatas[5] + formatDatas[6]));
            mLastChargeTime = String.format("%s-%s-%s %s:%s", 2000 + Integer.parseInt(formatDatas[7], 16) + "",
                    DigitalConver.decodeToString(formatDatas[8]),
                    DigitalConver.decodeToString(formatDatas[9]),
                    DigitalConver.decodeToString(formatDatas[10]),
                    DigitalConver.decodeToString(formatDatas[11]));
            LogModule.i("手环上一次充电时间：" + mLastChargeTime);
            LogModule.i("生产批次年：" + (2000 + Integer.parseInt(formatDatas[12], 16)));
            LogModule.i("生产批次周：" + DigitalConver.decodeToString(formatDatas[13]));
        }
        mQueue.poll();
        task.getCallback().onOrderResult(task.getOrder(), response);
        executeOrder(task.getCallback());
    }

    private void formatBandAlarmTask(OrderTask task) {
        BandAlarmTask bandAlarmTask = (BandAlarmTask) task;
        BaseResponse response = bandAlarmTask.getResponse();
        response.code = FitConstant.ORDER_CODE_SUCCESS;
        if (!bandAlarmTask.isAlarmFinish()) {
            bandAlarmTask.setAlarmFinish(true);
        } else {
            mQueue.poll();
            bandAlarmTask.getCallback().onOrderResult(task.getOrder(), response);
            bandAlarmTask.setAlarmFinish(false);
        }
        executeOrder(task.getCallback());
    }

    private void formatInnerTask(String[] formatDatas, OrderTask task) {
        BaseResponse response = task.getResponse();
        response.code = FitConstant.ORDER_CODE_SUCCESS;
        if (formatDatas.length > 4) {
            isSupportTodayData = true;
            String rateShow = formatDatas[3].substring(formatDatas[3].length() - 1, formatDatas[3].length());
            isSupportHeartRate = Integer.parseInt(rateShow) == 1;
            int endVersion = Integer.parseInt(formatDatas[4], 16);
            isSupportNewData = endVersion > 25;
            mFirmwareHeader = formatDatas[2];
            if (!TextUtils.isEmpty(mFirmwareHeader) && "EE".equals(formatDatas[2])) {
                isShouldUpgrade = endVersion < 30;
            } else {
                isShouldUpgrade = endVersion < 30;
            }
        } else {
            isSupportTodayData = false;
        }
        // 打印内部版本
        StringBuilder sb = new StringBuilder();
        for (int i = 2; i < formatDatas.length; i++) {
            sb.append(formatDatas[i]);
            if (i < formatDatas.length - 1) {
                sb.append(".");
            }
        }
        mInnerVersion = sb.toString();
        mQueue.poll();
        task.getCallback().onOrderResult(task.getOrder(), response);
        executeOrder(task.getCallback());
    }

    private void formatCommonOrder(OrderTask task) {
        BaseResponse response = task.getResponse();
        response.code = FitConstant.ORDER_CODE_SUCCESS;
        mQueue.poll();
        task.getCallback().onOrderResult(task.getOrder(), response);
        executeOrder(task.getCallback());
    }

    ///////////////////////////////////////////////////////////////////////////
    //
    ///////////////////////////////////////////////////////////////////////////

    private ServiceHandler mHandler;

    private class ServiceHandler extends BaseHandler<BluetoothModule> {

        public ServiceHandler(BluetoothModule module) {
            super(module);
        }

        @Override
        protected void handleMessage(BluetoothModule module, Message msg) {
        }
    }

    private ExecutorService mExecutorService;
    private ReConnRunnable mRunnableReconnect;

    private void startReConnect(Context context, ConnStateCallback connCallBack) {
        if (isOpenReConnect) {
            LogModule.i("开始重连...");
            mRunnableReconnect = new ReConnRunnable(context, connCallBack);
            mExecutorService.execute(mRunnableReconnect);
        }
    }

    private class ReConnRunnable implements Runnable {
        private Context mContext;
        private ConnStateCallback mConnCallBack;

        private ReConnRunnable(Context context, ConnStateCallback connCallBack) {
            this.mContext = context;
            this.mConnCallBack = connCallBack;
        }

        @Override
        public void run() {
            try {
                if (!isConnDevice(mContext, mDeviceAddress)) {
                    if (!isOpenReConnect) {
                        isReConnecting = false;
                        return;
                    }
                    LogModule.i("设备未连接，重连中...");
                    isReConnecting = true;
                    if (isBluetoothOpen()) {
                        LogModule.i("重新扫描设备...");
                        // 如果app处于前台，且栈顶页面为首页、配对页面、升级页面，则提示失败，否则一直重连
                        String topActivity = Utils.getTopActivity(mContext);
                        if (!TextUtils.isEmpty(topActivity)
                                && (topActivity.contains("MainActivity")
                                || topActivity.contains("MatchDevicesActivity")
                                || topActivity.contains("UpgradeBandActivity"))) {
                            LogModule.i(topActivity + "为前台页面，重连限制次数！！！");
                            if (reConnectCount > 0) {
                                reConnectCount--;
                                LogModule.i("重连次数：" + reConnectCount);
                                if (reConnectCount == 2) {
                                    mConnCallBack.onConnTimeout(reConnectCount);
                                }
                            } else {
                                isReConnecting = false;
                                mConnCallBack.onConnFailure(FitConstant.CONN_ERROR_CODE_FAILURE);
                                return;
                            }
                        }
                        startScanDevice(new ScanDeviceCallback() {
                            String deviceAddress = "";

                            @Override
                            public void onStartScan() {

                            }

                            @Override
                            public void onScanDevice(BleDevice device) {
                                if (mDeviceAddress.equals(device.address) && TextUtils.isEmpty(deviceAddress)) {
                                    deviceAddress = device.address;
                                    LogModule.i("扫描到设备，开始连接...");
                                    final BluetoothDevice bluetoothDevice = mBluetoothAdapter.getRemoteDevice(deviceAddress);
                                    if (mGattCallback == null) {
                                        mGattCallback = getBluetoothGattCallback(mContext, mConnCallBack);
                                    }
                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            mBluetoothGatt = (new BleConnectionCompat(mContext)).connectGatt(bluetoothDevice, false, mGattCallback);
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onStopScan() {
                                if (TextUtils.isEmpty(deviceAddress)) {
                                    LogModule.i("未扫描到设备...");
                                    mExecutorService.execute(mRunnableReconnect);
                                }

                            }
                        });
                    } else {
                        LogModule.i("蓝牙未开启...");
                        Thread.sleep(5000);
                        mExecutorService.execute(mRunnableReconnect);
                    }
                } else {
                    LogModule.i("设备已连接...");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @Date 2017/5/10
     * @Author wenzheng.liu
     * @Description 将所有手环特征设置为notify方式
     */
    private void setCharacteristicNotify(BluetoothGatt mBluetoothGatt) {
        List<BluetoothGattService> gattServices = mBluetoothGatt.getServices();
        if (gattServices == null)
            return;
        String uuid;
        // 遍历所有服务，找到手环的服务
        for (BluetoothGattService gattService : gattServices) {
            uuid = gattService.getUuid().toString();
            if (uuid.startsWith("0000ffc0")) {
                List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
                // 遍历所有特征，找到发出的特征
                for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                    uuid = gattCharacteristic.getUuid().toString();
                    if (uuid.startsWith("0000ffc2")) {
                        int charaProp = gattCharacteristic.getProperties();
                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                            if (mNotifyCharacteristic != null) {
                                setCharacteristicNotification(mBluetoothGatt, mNotifyCharacteristic, false);
                                synchronized (LOCK) {
                                    mNotifyCharacteristic = null;
                                }
                            }
                            mBluetoothGatt.readCharacteristic(gattCharacteristic);
                        }
                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                            synchronized (LOCK) {
                                mNotifyCharacteristic = gattCharacteristic;
                            }
                            setCharacteristicNotification(mBluetoothGatt, gattCharacteristic, true);
                        }
                    }
                }
            }
        }
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled        If true, enable notification. False otherwise.
     */
    private void setCharacteristicNotification(BluetoothGatt mBluetoothGatt,
                                               BluetoothGattCharacteristic characteristic, boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
        /**
         * 打开数据FFF4
         */
        // This is specific to Heart Rate Measurement.
        if (CHARACTERISTIC_UUID_NOTIFY.equals(characteristic.getUuid())) {
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(CHARACTERISTIC_DESCRIPTOR_UUID);
            if (descriptor == null) {
                return;
            }
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            mBluetoothGatt.writeDescriptor(descriptor);
        }
    }

    /**
     * @Date 2017/5/10
     * @Author wenzheng.liu
     * @Description 发送数据
     */
    private void writeCharacteristicData(BluetoothGatt mBluetoothGatt,
                                         byte[] byteArray) {
        if (mBluetoothGatt == null) {
            return;
        }
        BluetoothGattService service = mBluetoothGatt.getService(SERVIE_UUID);
        if (service == null) {
            return;
        }
        BluetoothGattCharacteristic characteristic = service.getCharacteristic(CHARACTERISTIC_UUID_WRITE);
        if (characteristic == null) {
            return;
        }
        LogModule.i("发送数据：");
        DigitalConver.formatData(byteArray);
        characteristic.setValue(byteArray);
        characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
        mBluetoothGatt.writeCharacteristic(characteristic);
    }

    /**
     * Clears the internal cache and forces a refresh of the services from the
     * remote device.
     */
    public boolean refreshDeviceCache() {
        if (mBluetoothGatt != null) {
            try {
                BluetoothGatt localBluetoothGatt = mBluetoothGatt;
                Method localMethod = localBluetoothGatt.getClass().getMethod("refresh", new Class[0]);
                if (localMethod != null) {
                    boolean bool = ((Boolean) localMethod.invoke(localBluetoothGatt, new Object[0])).booleanValue();
                    return bool;
                }
            } catch (Exception localException) {
                LogModule.i("An exception occured while refreshing device");
            }
        }
        return false;
    }
}
