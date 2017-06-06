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
import com.fitpolo.support.entity.DailySleep;
import com.fitpolo.support.entity.DailyStep;
import com.fitpolo.support.entity.HeartRate;
import com.fitpolo.support.log.LogModule;
import com.fitpolo.support.task.BandAlarmTask;
import com.fitpolo.support.task.DailySleepRecordTask;
import com.fitpolo.support.task.NewDailySleepIndexTask;
import com.fitpolo.support.task.NewDailySleepRecordTask;
import com.fitpolo.support.task.OrderTask;
import com.fitpolo.support.utils.BaseHandler;
import com.fitpolo.support.utils.ComplexDataParse;
import com.fitpolo.support.utils.DigitalConver;

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
    private BluetoothGattCallback mGattCallback;
    private BlockingQueue<OrderTask> mQueue;
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
    public void createBluetoothGatt(Context context, String address, ConnStateCallback connCallBack) {
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
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (mGattCallback == null) {
            mGattCallback = getBluetoothGattCallback(context, connCallBack);
        }
        disConnectBle();
        mBluetoothGatt = device.connectGatt(context, false, mGattCallback);
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
    private boolean isSupportHeartRate;
    private boolean isSupportTodayData;
    private boolean isSupportNewData;
    private String mFirmwareVersion;
    private int mBatteryQuantity;
    private int mDailyStepCount;
    private int mSleepIndexCount;
    private int mSleepRecordCount;
    private int mHeartRateCount;
    private ArrayList<DailyStep> mDailySteps;
    private ArrayList<DailySleep> mDailySleeps;
    private HashMap<Integer, DailySleep> mSleepsMap;
    private ArrayList<HeartRate> mHeartRates;


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
        OrderTask task = mQueue.peek();
        writeCharacteristicData(mBluetoothGatt, task.assemble());
        orderTimeoutHandler(task);
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
                delayTime = mDailyStepCount == 0 ? delayTime : 3000 * mDailyStepCount;
                break;
            case getDailySleepIndex:
                delayTime = mSleepIndexCount == 0 ? delayTime : 3000 * (mSleepIndexCount + mSleepRecordCount);
                break;
            case getHeartRate:
                delayTime = mHeartRateCount == 0 ? delayTime : 3000 * mHeartRateCount;
                break;
            case getNewDailySteps:
            case getNewDailySleepIndex:
            case getNewHeartRate:
            case getTodayData:
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
                        case getBatteryDailyStepCount:
                            LogModule.i("获取电量和记步总数超时");
                            break;
                        case getSleepHeartCount:
                            LogModule.i("获取睡眠和心率总数超时");
                            break;
                        case getDailySteps:
                            LogModule.i("获取记步数据超时");
                            break;
                        case getDailySleepIndex:
                            LogModule.i("获取睡眠index超时");
                            break;
                        case getHeartRate:
                            LogModule.i("获取心率数据超时");
                            break;
                        case getTodayData:
                            LogModule.i("获取当天数据超时");
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
                        case getNewDailySteps:
                            LogModule.i("获取未同步的记步数据超时");
                            break;
                        case getNewDailySleepIndex:
                            LogModule.i("获取未同步的睡眠记录数据超时");
                            break;
                        case getNewHeartRate:
                            LogModule.i("获取未同步的心率数据超时");
                            break;
                    }
                    task.getCallback().onOrderTimeout(task.getOrder());
                    mQueue.poll();
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
        return connState == BluetoothProfile.STATE_CONNECTED;
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
     * @Date 2017/5/14 0014
     * @Author wenzheng.liu
     * @Description 设置重连
     */
    public void setOpenReConnect(boolean openReConnect) {
        LogModule.i(openReConnect ? "打开重连" : "关闭重连");
        isOpenReConnect = openReConnect;
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
     * @Date 2017/5/15
     * @Author wenzheng.liu
     * @Description 获取电池电量
     */
    public int getBatteryQuantity() {
        return mBatteryQuantity;
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
        if (mBluetoothGatt != null) {
            LogModule.i("断开连接");
            synchronized (LOCK) {
                mNotifyCharacteristic = null;
            }
            mBluetoothGatt.close();
            mBluetoothGatt = null;
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
            }

            @Override
            public void onConnFailure() {
                disConnectBle();
                connCallBack.onConnFailure(FitConstant.CONN_ERROR_CODE_FAILURE);
                startReConnect(context, connCallBack);
            }

            @Override
            public void onDisConn() {
                disConnectBle();
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
                            LogModule.i("获取内部版本成功");
                            formatInnerTask(formatDatas, task);
                            break;
                        case getFirmwareVersion:
                            LogModule.i("获取固件版本成功");
                            formatFirmwareTask(formatDatas, task);
                            break;
                        case getBatteryDailyStepCount:
                            LogModule.i("获取电量和记步总数成功");
                            formatBatteryDailyStepCountTask(formatDatas, task);
                            break;
                        case getSleepHeartCount:
                            LogModule.i("获取睡眠和心率总数成功");
                            formatSleepHeartCountTask(formatDatas, task);
                            break;
                        case getDailySteps:
                            LogModule.i("获取记步数据成功");
                            formatDailyStepsTask(formatDatas, task);
                            break;
                        case getDailySleepIndex:
                            if (mSleepIndexCount == 0 && !mDailySleeps.isEmpty()) {
                                LogModule.i("获取睡眠record成功");
                                formatDailySleepRecordTask(formatDatas, task);
                            } else {
                                LogModule.i("获取睡眠index成功");
                                formatDailySleepIndexTask(formatDatas, task);
                            }
                            break;
                        case getDailySleepRecord:
                            LogModule.i("获取睡眠record成功");
                            formatDailySleepRecordTask(formatDatas, task);
                            break;
                        case getHeartRate:
                            LogModule.i("获取心率数据成功");
                            formatHeartRateTask(formatDatas, task);
                            break;
                        case getTodayData:
                            LogModule.i("获取当天数据成功");
                            formatTodayDataTask(formatDatas, task);
                            break;
                        case getNewDailySteps:
                            LogModule.i("获取未同步的记步数据成功");
                            formatNewDailyStepsTask(formatDatas, task);
                            break;
                        case getNewDailySleepIndex:
                            if (mSleepIndexCount == 0 && mDailySleeps != null && !mDailySleeps.isEmpty()) {
                                LogModule.i("获取未同步的睡眠详情数据成功");
                                formatNewDailySleepRecordTask(formatDatas, task);
                            } else {
                                LogModule.i("获取未同步的睡眠记录数据成功");
                                formatNewDailySleepIndex(formatDatas, task);
                            }
                            break;
                        case getNewDailySleepRecord:
                            LogModule.i("获取未同步的睡眠详情数据成功");
                            formatNewDailySleepRecordTask(formatDatas, task);
                            break;
                        case getNewHeartRate:
                            LogModule.i("获取未同步的心率数据成功");
                            formatNewHeartRateTask(formatDatas, task);
                            break;
                        case setBandAlarm:
                            LogModule.i("设置闹钟数据成功");
                            formatBandAlarmTask(task);
                            break;
                        case setAutoLigten:
                            LogModule.i("设置翻腕自动亮屏成功");
                            formatCommonOrder(task);
                            break;
                        case setSystemTime:
                            LogModule.i("设置手环时间成功");
                            formatCommonOrder(task);
                            break;
                        case setTimeFormat:
                            LogModule.i("设置显示时间格式成功");
                            formatCommonOrder(task);
                            break;
                        case setUserInfo:
                            LogModule.i("设置用户信息成功");
                            formatCommonOrder(task);
                            break;
                        case setUnitType:
                            LogModule.i("设置单位类型成功");
                            formatCommonOrder(task);
                            break;
                        case setSitLongTimeAlert:
                            LogModule.i("设置久坐提醒成功");
                            formatCommonOrder(task);
                            break;
                        case setLastShow:
                            LogModule.i("设置最后显示成功");
                            formatCommonOrder(task);
                            break;
                        case setHeartRateInterval:
                            LogModule.i("设置心率时间间隔成功");
                            formatCommonOrder(task);
                            break;
                        case setFunctionDisplay:
                            LogModule.i("设置功能显示成功");
                            formatCommonOrder(task);
                            break;
                        case setShakeBand:
                            LogModule.i("设置手环震动成功");
                            formatCommonOrder(task);
                            break;
                    }
                }

            }
        });
        return callback;
    }

    private void formatNewHeartRateTask(String[] formatDatas, OrderTask task) {
        BaseResponse response = task.getResponse();
        int header = Integer.parseInt(DigitalConver.decodeToString(formatDatas[0]));
        if (header == FitConstant.RESPONSE_HEADER_NEW_DATA_COUNT) {
            mHeartRateCount = Integer.parseInt(DigitalConver.decodeToString(formatDatas[2]));
            LogModule.i("有" + mHeartRateCount + "条心率数据");
            if (mHeartRates == null) {
                mHeartRates = new ArrayList<>();
            }
        }
        if (header == FitConstant.RESPONSE_HEADER_HEART_RATE) {
            if (mHeartRateCount > 0) {
                if (formatDatas.length <= 2)
                    return;
                ComplexDataParse.parseHeartRate(formatDatas, mHeartRates);
                mHeartRateCount--;
                if (mHeartRateCount > 0) {
                    LogModule.i("还有" + mHeartRateCount + "条心率数据未同步");
                    return;
                }
            }
        }
        if (mHeartRateCount != 0) {
            return;
        }
        Collections.sort(mHeartRates);
        response.code = FitConstant.ORDER_CODE_SUCCESS;
        task.getCallback().onOrderResult(task.getOrder(), response);
        mQueue.poll();
        executeOrder(task.getCallback());
    }

    private void formatNewDailySleepRecordTask(String[] formatDatas, OrderTask task) {
        BaseResponse response = task.getResponse();
        int header = Integer.parseInt(DigitalConver.decodeToString(formatDatas[0]));
        if (header == FitConstant.RESPONSE_HEADER_NEW_DATA_COUNT) {
            mSleepRecordCount = Integer.parseInt(DigitalConver.decodeToString(formatDatas[2]));
            LogModule.i("有" + mSleepRecordCount + "条睡眠record");
        }
        if (header == FitConstant.RESPONSE_HEADER_SLEEP_INDEX) {
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
        response.code = FitConstant.ORDER_CODE_SUCCESS;
        // 重新设置回index头，方便前端调用
        task.setOrder(OrderEnum.getDailySleepIndex);
        task.getCallback().onOrderResult(task.getOrder(), response);
        mQueue.poll();
        executeOrder(task.getCallback());
    }

    private void formatNewDailySleepIndex(String[] formatDatas, OrderTask task) {
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
            response.code = FitConstant.ORDER_CODE_SUCCESS;
            task.getCallback().onOrderResult(task.getOrder(), response);
            mQueue.poll();
            executeOrder(task.getCallback());
        }
    }

    private void formatNewDailyStepsTask(String[] formatDatas, OrderTask task) {
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
        task.getCallback().onOrderResult(task.getOrder(), response);
        mQueue.poll();
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
        task.getCallback().onOrderResult(task.getOrder(), response);
        mQueue.poll();
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
        task.getCallback().onOrderResult(task.getOrder(), response);
        mQueue.poll();
        executeOrder(task.getCallback());
    }

    private void formatHeartRateTask(String[] formatDatas, OrderTask task) {
        BaseResponse response = task.getResponse();
        if (mHeartRateCount > 0) {
            if (mHeartRates == null) {
                mHeartRates = new ArrayList<>();
            }
            if (formatDatas.length <= 2)
                return;
            ComplexDataParse.parseHeartRate(formatDatas, mHeartRates);
            mHeartRateCount--;
            if (mHeartRateCount > 0) {
                LogModule.i("还有" + mHeartRateCount + "条心率数据未同步");
                return;
            }
        }
        Collections.sort(mHeartRates);
        response.code = FitConstant.ORDER_CODE_SUCCESS;
        task.getCallback().onOrderResult(task.getOrder(), response);
        mQueue.poll();
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
            task.getCallback().onOrderResult(task.getOrder(), response);
            mQueue.poll();
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
        response.code = FitConstant.ORDER_CODE_SUCCESS;
        // 重新设置回index头，方便前端调用
        task.setOrder(OrderEnum.getDailySleepIndex);
        task.getCallback().onOrderResult(task.getOrder(), response);
        mQueue.poll();
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
        task.getCallback().onOrderResult(task.getOrder(), response);
        mQueue.poll();
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
        task.getCallback().onOrderResult(task.getOrder(), response);
        mQueue.poll();
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
        task.getCallback().onOrderResult(task.getOrder(), response);
        mQueue.poll();
        executeOrder(task.getCallback());
    }

    private void formatBandAlarmTask(OrderTask task) {
        BandAlarmTask bandAlarmTask = (BandAlarmTask) task;
        BaseResponse response = bandAlarmTask.getResponse();
        response.code = FitConstant.ORDER_CODE_SUCCESS;
        if (!bandAlarmTask.isAlarmFinish()) {
            bandAlarmTask.setAlarmFinish(true);
        } else {
            bandAlarmTask.getCallback().onOrderResult(task.getOrder(), response);
            bandAlarmTask.setAlarmFinish(false);
            mQueue.poll();
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
            isSupportNewData = endVersion > 20;
        } else {
            isSupportTodayData = false;
        }
        task.getCallback().onOrderResult(task.getOrder(), response);
        mQueue.poll();
        executeOrder(task.getCallback());
    }

    private void formatCommonOrder(OrderTask task) {
        BaseResponse response = task.getResponse();
        response.code = FitConstant.ORDER_CODE_SUCCESS;
        task.getCallback().onOrderResult(task.getOrder(), response);
        mQueue.poll();
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
    private ReConnRunnable mrunnableReconnect;

    private void startReConnect(Context context, ConnStateCallback connCallBack) {
        if (isOpenReConnect) {
            LogModule.i("开始重连...");
            mrunnableReconnect = new ReConnRunnable(context, connCallBack);
            mExecutorService.execute(mrunnableReconnect);
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
                    LogModule.i("设备未连接，重连中...");
                    if (isBluetoothOpen()) {
                        createBluetoothGatt(mContext, mDeviceAddress, mConnCallBack);
                    } else {
                        LogModule.i("蓝牙未开启...");
                        Thread.sleep(30000);
                        mExecutorService.execute(mrunnableReconnect);
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


}
