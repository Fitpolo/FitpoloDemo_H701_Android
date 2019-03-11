package com.fitpolo.support.task;

import com.fitpolo.support.MokoSupport;
import com.fitpolo.support.callback.MokoOrderTaskCallback;
import com.fitpolo.support.entity.HeartRate;
import com.fitpolo.support.entity.OrderEnum;
import com.fitpolo.support.entity.OrderType;
import com.fitpolo.support.log.LogModule;
import com.fitpolo.support.utils.ComplexDataParse;
import com.fitpolo.support.utils.DigitalConver;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

/**
 * @Date 2017/5/11
 * @Author wenzheng.liu
 * @Description 获取未同步的心率数据
 * @ClassPath com.fitpolo.support.task.LastestHeartRateTask
 */
public class LastestHeartRateTask extends OrderTask {
    private static final int ORDERDATA_LENGTH = 7;
    // 获取最新数据
    private static final int HEADER_GET_NEW_DATA = 0x2C;
    // 返回心率数据头
    private static final int RESPONSE_HEADER_HEART_RATE = 0xA8;
    // 返回未同步数据头
    private static final int RESPONSE_HEADER_NEW_DATA_COUNT = 0xAA;

    private byte[] orderData;

    private HashMap<Integer, Boolean> heartRatesMap;
    private int heartRateCount;
    private ArrayList<HeartRate> heartRates;

    public LastestHeartRateTask(MokoOrderTaskCallback callback, Calendar lastSyncTime) {
        super(OrderType.WRITE, OrderEnum.getLastestHeartRate, callback, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
        isNewDataSuccess = false;

        orderData = new byte[ORDERDATA_LENGTH];
        int year = lastSyncTime.get(Calendar.YEAR) - 2000;
        int month = lastSyncTime.get(Calendar.MONTH) + 1;
        int day = lastSyncTime.get(Calendar.DAY_OF_MONTH);

        int hour = lastSyncTime.get(Calendar.HOUR_OF_DAY);
        int minute = lastSyncTime.get(Calendar.MINUTE);
        orderData[0] = (byte) HEADER_GET_NEW_DATA;
        orderData[1] = (byte) year;
        orderData[2] = (byte) month;
        orderData[3] = (byte) day;
        orderData[4] = (byte) hour;
        orderData[5] = (byte) minute;
        orderData[6] = (byte) RESPONSE_HEADER_HEART_RATE;
    }

    @Override
    public byte[] assemble() {
        return orderData;
    }

    @Override
    public void parseValue(byte[] value) {
        if (order.getOrderHeader() != DigitalConver.byte2Int(value[0]) && order.getOrderHeader() != DigitalConver.byte2Int(value[1])) {
            return;
        }
        LogModule.i(order.getOrderName() + "成功");
        // 获取心率总数标记成功
        isNewDataSuccess = true;
        int header = DigitalConver.byte2Int(value[0]);
        switch (header) {
            case RESPONSE_HEADER_NEW_DATA_COUNT:
                byte[] count = new byte[value.length - 2];
                System.arraycopy(value, 2, count, 0, value.length - 2);
                heartRateCount = DigitalConver.byteArr2Int(count);
                MokoSupport.getInstance().setHeartRatesCount(heartRateCount);
                LogModule.i("有" + heartRateCount + "条心率数据");
                MokoSupport.getInstance().initHeartRatesList();
                heartRatesMap = MokoSupport.getInstance().getHeartRatesMap();
                heartRates = MokoSupport.getInstance().getHeartRates();
                if (heartRateCount != 0) {
                    // 拿到条数后再启动超时任务
                    heartRatesMap.put(heartRateCount, false);
                    MokoSupport.getInstance().setHeartRatesMap(heartRatesMap);
                    MokoSupport.getInstance().timeoutHandler(this);
                }
                break;
            case RESPONSE_HEADER_HEART_RATE:
                if (heartRateCount > 0) {
                    if (value.length <= 2 || orderStatus == OrderTask.ORDER_STATUS_SUCCESS)
                        return;
                    if (heartRates == null) {
                        heartRates = new ArrayList<>();
                    }
                    if (heartRatesMap == null) {
                        heartRatesMap = new HashMap<>();
                    }
                    heartRatesMap.put(heartRateCount, true);
                    ComplexDataParse.parseHeartRate(value, heartRates);
                    heartRateCount--;

                    MokoSupport.getInstance().setHeartRatesCount(heartRateCount);
                    MokoSupport.getInstance().setHeartRates(heartRates);
                    if (heartRateCount > 0) {
                        LogModule.i("还有" + heartRateCount + "条心率数据未同步");
                        heartRatesMap.put(heartRateCount, false);
                        MokoSupport.getInstance().setHeartRatesMap(heartRatesMap);
                        orderTimeoutHandler(heartRateCount);
                        return;
                    }
                }
                break;
            default:
                return;
        }
        if (heartRateCount != 0) {
            return;
        }
        // 对心率数据做判重处理，避免时间重复造成的数据问题
        HashMap<String, HeartRate> removeRepeatMap = new HashMap<>();
        for (HeartRate heartRate : heartRates) {
            removeRepeatMap.put(heartRate.time, heartRate);
        }
        if (heartRates.size() != removeRepeatMap.size()) {
            heartRates.clear();
            heartRates.addAll(removeRepeatMap.values());
        }
        MokoSupport.getInstance().setHeartRatesCount(heartRateCount);
        MokoSupport.getInstance().setHeartRates(heartRates);
        MokoSupport.getInstance().setHeartRatesMap(heartRatesMap);
        orderStatus = OrderTask.ORDER_STATUS_SUCCESS;
        MokoSupport.getInstance().pollTask();
        callback.onOrderResult(response);
        MokoSupport.getInstance().executeTask(callback);
    }

    private void orderTimeoutHandler(final int heartRateCount) {
        MokoSupport.getInstance().getHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (heartRatesMap != null
                        && !heartRatesMap.isEmpty()
                        && heartRatesMap.get(heartRateCount) != null
                        && !heartRatesMap.get(heartRateCount)) {
                    orderStatus = OrderTask.ORDER_STATUS_SUCCESS;
                    LogModule.i("获取心率第" + heartRateCount + "个数据超时");
                    MokoSupport.getInstance().pollTask();
                    callback.onOrderTimeout(response);
                    MokoSupport.getInstance().executeTask(callback);
                }
            }
        }, delayTime);
    }

    private boolean isNewDataSuccess;

    @Override
    public boolean timeoutPreTask() {
        if (!isNewDataSuccess) {
            LogModule.i("获取未同步的心率个数超时");
        } else {
            return false;
        }
        return super.timeoutPreTask();
    }
}
