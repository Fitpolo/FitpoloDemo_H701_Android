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
 * @Description 设置心率间隔
 * @ClassPath com.fitpolo.support.task.HeartRateIntervalTask
 */
public class HeartRateIntervalTask extends OrderTask {
    private static final int ORDERDATA_LENGTH = 4;
    // 获取数据
    private static final int HEADER_GETDATA = 0x16;
    // 设置心率间隔
    private static final int GET_SET_HEART_RATE_INTERVAL = 0x17;

    private byte[] orderData;

    public HeartRateIntervalTask(MokoOrderTaskCallback callback, int heartRateInterval) {
        super(OrderType.WRITE, OrderEnum.setHeartRateInterval, callback, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
        int interval = heartRateInterval;
        orderData = new byte[ORDERDATA_LENGTH];
        orderData[0] = (byte) HEADER_GETDATA;
        orderData[1] = (byte) GET_SET_HEART_RATE_INTERVAL;
        orderData[2] = (byte) interval;
        orderData[3] = 0;
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
        orderStatus = OrderTask.ORDER_STATUS_SUCCESS;

        MokoSupport.getInstance().pollTask();
        callback.onOrderResult(response);
        MokoSupport.getInstance().executeTask(callback);
    }
}
