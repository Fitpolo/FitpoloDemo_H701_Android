package com.fitpolo.support.task;

import com.fitpolo.support.MokoSupport;
import com.fitpolo.support.callback.MokoOrderTaskCallback;
import com.fitpolo.support.entity.OrderEnum;
import com.fitpolo.support.entity.OrderType;
import com.fitpolo.support.log.LogModule;
import com.fitpolo.support.utils.DigitalConver;

/**
 * @Date 2017/5/11
 * @Author wenzheng.liu
 * @Description 获取睡眠和心率总数
 * @ClassPath com.fitpolo.support.task.SleepHeartCountTask
 */
public class SleepHeartCountTask extends OrderTask {

    private static final int ORDERDATA_LENGTH = 2;
    // 获取数据
    private static final int HEADER_GETDATA = 0x16;
    // 获取睡眠和心率总数
    private static final int GET_SLEEP_HEART_COUNT = 0x12;

    private byte[] orderData;

    public SleepHeartCountTask(MokoOrderTaskCallback callback) {
        super(OrderType.WRITE, OrderEnum.getSleepHeartCount, callback, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
        orderData = new byte[ORDERDATA_LENGTH];
        orderData[0] = (byte) HEADER_GETDATA;
        orderData[1] = (byte) GET_SLEEP_HEART_COUNT;
    }

    @Override
    public byte[] assemble() {
        return orderData;
    }

    @Override
    public void parseValue(byte[] value) {
        if (order.getOrderHeader() != DigitalConver.byte2Int(value[1])) {
            return;
        }
        LogModule.i(order.getOrderName() + "成功");
        int sleepIndexCount = DigitalConver.byte2Int(value[2]);
        int sleepRecordCount = DigitalConver.byte2Int(value[3]);
        int heartRateCount = DigitalConver.byte2Int(value[4]);
        LogModule.i("有" + sleepIndexCount + "条睡眠index");
        LogModule.i("有" + sleepRecordCount + "条睡眠record");
        LogModule.i("有" + heartRateCount + "条心率数据");
        MokoSupport.getInstance().setSleepIndexCount(sleepIndexCount);
        MokoSupport.getInstance().setSleepRecordCount(sleepRecordCount);
        MokoSupport.getInstance().setHeartRatesCount(heartRateCount);
        MokoSupport.getInstance().initSleepIndexList();
        MokoSupport.getInstance().initHeartRatesList();
        orderStatus = OrderTask.ORDER_STATUS_SUCCESS;
        MokoSupport.getInstance().pollTask();
        callback.onOrderResult(response);
        MokoSupport.getInstance().executeTask(callback);
    }
}
