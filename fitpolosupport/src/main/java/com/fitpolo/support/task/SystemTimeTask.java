package com.fitpolo.support.task;

import com.fitpolo.support.MokoSupport;
import com.fitpolo.support.callback.MokoOrderTaskCallback;
import com.fitpolo.support.entity.OrderEnum;
import com.fitpolo.support.entity.OrderType;
import com.fitpolo.support.log.LogModule;
import com.fitpolo.support.utils.DigitalConver;

import java.util.Calendar;

/**
 * @Date 2017/5/11
 * @Author wenzheng.liu
 * @Description 设置系统时间
 * @ClassPath com.fitpolo.support.task.InnerVersionTask
 */
public class SystemTimeTask extends OrderTask {
    private static final int ORDERDATA_LENGTH = 7;
    // 设置系统时间
    private static final int HEADER_SET_SYSTEM_TIME = 0x11;

    private byte[] orderData;

    public SystemTimeTask(MokoOrderTaskCallback callback) {
        super(OrderType.WRITE, OrderEnum.setSystemTime, callback, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
        orderData = new byte[ORDERDATA_LENGTH];
    }

    @Override
    public byte[] assemble() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int date = calendar.get(Calendar.DATE);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
        orderData[0] = (byte) HEADER_SET_SYSTEM_TIME;
        orderData[1] = (byte) (year - 2000);
        orderData[2] = (byte) month;
        orderData[3] = (byte) date;
        orderData[4] = (byte) hour;
        orderData[5] = (byte) minute;
        orderData[6] = (byte) second;
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
