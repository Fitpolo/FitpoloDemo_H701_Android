package com.fitpolo.support.task;

import com.fitpolo.support.callback.MokoOrderTaskCallback;
import com.fitpolo.support.entity.OrderEnum;
import com.fitpolo.support.entity.OrderType;

import java.util.Calendar;

/**
 * @Date 2017/5/11
 * @Author wenzheng.liu
 * @Description 获取未同步的睡眠详情数据
 * @ClassPath com.fitpolo.support.task.LastestSleepRecordTask
 */
public class LastestSleepRecordTask extends OrderTask {
    private static final int ORDERDATA_LENGTH = 7;
    // 获取最新数据
    private static final int HEADER_GET_NEW_DATA = 0x2C;
    // 返回睡眠record数据头
    private static final int RESPONSE_HEADER_SLEEP_RECORD = 0x94;

    private byte[] orderData;

    public LastestSleepRecordTask(MokoOrderTaskCallback callback, Calendar lastSyncTime) {
        super(OrderType.WRITE, OrderEnum.getLastestSleepRecord, callback, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);

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
        orderData[6] = (byte) RESPONSE_HEADER_SLEEP_RECORD;
    }

    @Override
    public byte[] assemble() {
        return orderData;
    }
}
