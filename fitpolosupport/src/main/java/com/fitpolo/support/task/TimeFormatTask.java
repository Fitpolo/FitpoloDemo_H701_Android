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
 * @Description 设置显示时间格式
 * @ClassPath com.fitpolo.support.task.TimeFormatTask
 */
public class TimeFormatTask extends OrderTask {
    private static final int ORDERDATA_LENGTH = 2;
    // 时间格式
    private static final int HEADER_SET_TIME_FORMAT = 0x24;

    private byte[] orderData;

    public TimeFormatTask(MokoOrderTaskCallback callback, int timeFormat) {
        super(OrderType.WRITE, OrderEnum.setTimeFormat, callback, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
        orderData = new byte[ORDERDATA_LENGTH];
        orderData[0] = (byte) HEADER_SET_TIME_FORMAT;
        orderData[1] = (byte) timeFormat;
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
