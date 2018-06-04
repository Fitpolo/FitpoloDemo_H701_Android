package com.fitpolo.support.task;

import com.fitpolo.support.callback.MokoOrderTaskCallback;
import com.fitpolo.support.entity.OrderEnum;
import com.fitpolo.support.entity.OrderType;

/**
 * @Date 2017/5/11
 * @Author wenzheng.liu
 * @Description 获取睡眠记录
 * @ClassPath com.fitpolo.support.task.AllSleepRecordTask
 */
public class AllSleepRecordTask extends OrderTask {

    private static final int ORDERDATA_LENGTH = 2;
    // 获取数据
    private static final int HEADER_GETDATA = 0x16;
    // 获取睡眠详情
    private static final int GET_DAILY_SLEEP_RECORD = 0x03;

    private byte[] orderData;

    public AllSleepRecordTask(MokoOrderTaskCallback callback) {
        super(OrderType.WRITE, OrderEnum.getAllSleepRecord, callback, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
        orderData = new byte[ORDERDATA_LENGTH];
        orderData[0] = (byte) HEADER_GETDATA;
        orderData[1] = (byte) GET_DAILY_SLEEP_RECORD;
    }

    @Override
    public byte[] assemble() {
        return orderData;
    }
}
