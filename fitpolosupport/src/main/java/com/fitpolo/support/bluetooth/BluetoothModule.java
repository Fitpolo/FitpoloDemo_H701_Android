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
import com.fitpolo.support.callback.ConnStateCallback;
import com.fitpolo.support.callback.GattCallback;
import com.fitpolo.support.callback.OrderCallback;
import com.fitpolo.support.callback.ScanDeviceCallback;
import com.fitpolo.support.entity.BaseResponse;
import com.fitpolo.support.entity.BleDevice;
import com.fitpolo.support.entity.InnerVersion;
import com.fitpolo.support.log.LogModule;
import com.fitpolo.support.task.OrderTask;
import com.fitpolo.support.utils.BaseHandler;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
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
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        BluetoothGattCallback callback = getBluetoothGattCallback(connCallBack);
        mBluetoothGatt = device.connectGatt(context, false, callback);
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

    private int mStepCount;
    private int mSleepIndexCount;
    private int mSleepRecordCount;
    private int mHeartRateCount;


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
            case getSteps:
                delayTime = mStepCount == 0 ? delayTime : 1000 * mStepCount;
                break;
            case getSleepIndex:
                delayTime = mSleepIndexCount == 0 ? delayTime : 1000 * mSleepIndexCount;
                break;
            case getSleepRecord:
                delayTime = mSleepRecordCount == 0 ? delayTime : 1000 * mSleepRecordCount;
                break;
            case getHeartRate:
                delayTime = mHeartRateCount == 0 ? delayTime : 1000 * mHeartRateCount;
                break;
        }
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (task.getResponse().code != FitConstant.ORDER_CODE_SUCCESS) {
                    task.getResponse().code = FitConstant.ORDER_CODE_ERROR_TIMEOUT;
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
    private boolean isConnDevice(Context context, String address) {
        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        int connState = bluetoothManager.getConnectionState(mBluetoothAdapter.getRemoteDevice(address), BluetoothProfile.GATT);
        return connState == BluetoothProfile.STATE_CONNECTED;
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
    private boolean isBluetoothOpen() {
        return mBluetoothAdapter != null && mBluetoothAdapter.isEnabled();
    }

    /**
     * @Date 2017/5/10
     * @Author wenzheng.liu
     * @Description 断开gattt
     */
    private void disConnectBle() {
        if (mBluetoothGatt != null) {
            synchronized (LOCK) {
                mNotifyCharacteristic = null;
            }
            mBluetoothGatt.close();
        }
    }

    /**
     * @param connCallBack
     * @Date 2017/5/10
     * @Author wenzheng.liu
     * @Description 获取蓝牙连接回调
     */
    private BluetoothGattCallback getBluetoothGattCallback(final ConnStateCallback connCallBack) {
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
            }

            @Override
            public void onDisConn() {
                disConnectBle();
                connCallBack.onDisconnect();
            }

            @Override
            public void onResponse(BluetoothGattCharacteristic characteristic) {
                byte[] data = characteristic.getValue();
                LogModule.i("接收数据：");
                String[] formatDatas = formatData(data);
                OrderTask task = mQueue.peek();
                if (formatDatas != null && formatDatas.length > 0) {
                    switch (task.getOrder()) {
                        case getInnerVersion:
                            InnerVersion version = (InnerVersion) task.getResponse();
                            version.code = FitConstant.ORDER_CODE_SUCCESS;
                            if (formatDatas.length > 4) {
                                version.isOldBand = false;
                                String rateShow = formatDatas[3].substring(formatDatas[3].length() - 1, formatDatas[3].length());
                                if (Integer.parseInt(rateShow) == 1) {
                                    version.isSupportHeartRate = true;
                                } else {
                                    version.isSupportHeartRate = false;
                                }
                            } else {
                                version.isOldBand = true;
                            }
                            task.getCallback().onOrderResult(task.getOrder(), version);
                            mQueue.poll();
                            executeOrder(task.getCallback());
                            break;
                        case setSystemTime:
                            BaseResponse response = task.getResponse();
                            response.code = FitConstant.ORDER_CODE_SUCCESS;
                            task.getCallback().onOrderResult(task.getOrder(), response);
                            mQueue.poll();
                            executeOrder(task.getCallback());
                            break;
                    }
                }

            }
        });
        return callback;
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
                                mNotifyCharacteristic = null;
                            }
                            mBluetoothGatt.readCharacteristic(gattCharacteristic);
                        }
                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                            mNotifyCharacteristic = gattCharacteristic;
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
        formatData(byteArray);
        characteristic.setValue(byteArray);
        characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
        mBluetoothGatt.writeCharacteristic(characteristic);
    }

    private ServiceHandler mHandler;


    private static class ServiceHandler extends BaseHandler<BluetoothModule> {

        public ServiceHandler(BluetoothModule module) {
            super(module);
        }

        @Override
        protected void handleMessage(BluetoothModule module, Message msg) {
        }
    }

    /**
     * @Date 2017/5/10
     * @Author wenzheng.liu
     * @Description 格式化数据
     */
    private String[] formatData(byte[] data) {
        if (data != null && data.length > 0) {
            StringBuilder stringBuilder = new StringBuilder(data.length);
            for (byte byteChar : data)
                stringBuilder.append(byte2HexString(byteChar));
            LogModule.i(stringBuilder.toString());
            String[] datas = stringBuilder.toString().split(" ");
            return datas;
        }
        return null;
    }

    /**
     * @Date 2017/5/10
     * @Author wenzheng.liu
     * @Description byte转16进制
     */
    private String byte2HexString(byte b) {
        return String.format("%02X ", b);
    }
}
