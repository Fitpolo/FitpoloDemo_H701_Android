package com.fitpolo.support.task;

import com.fitpolo.support.MokoSupport;
import com.fitpolo.support.callback.MokoOrderTaskCallback;
import com.fitpolo.support.entity.OrderEnum;
import com.fitpolo.support.entity.OrderType;
import com.fitpolo.support.log.LogModule;

/**
 * @Date 2017/5/11
 * @Author wenzheng.liu
 * @Description 设置手环震动
 * @ClassPath com.fitpolo.support.task.ShakeBandTask
 */
public class ShakeBandTask extends OrderTask {
    private static final int ORDERDATA_LENGTH = 5;
    // 设置手环震动
    private static final int HEADER_SET_SHAKE_BAND = 0x17;
    private byte[] orderData;

    public ShakeBandTask(MokoOrderTaskCallback callback) {
        super(OrderType.WRITE, OrderEnum.setShakeBand, callback, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
        orderData = new byte[ORDERDATA_LENGTH];
        orderData[0] = (byte) HEADER_SET_SHAKE_BAND;
        orderData[1] = 0x02;
        orderData[2] = 0x03;
        orderData[3] = 0x0A;
        orderData[4] = 0x0A;
    }

    @Override
    public byte[] assemble() {
        return orderData;
    }

    @Override
    public void parseValue(byte[] value) {
        LogModule.i(order.getOrderName() + "成功");
        orderStatus = OrderTask.ORDER_STATUS_SUCCESS;

        MokoSupport.getInstance().pollTask();
        callback.onOrderResult(response);
        MokoSupport.getInstance().executeTask(callback);
    }
}
