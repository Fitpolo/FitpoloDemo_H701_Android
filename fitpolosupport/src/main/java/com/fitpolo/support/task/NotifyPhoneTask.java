package com.fitpolo.support.task;

import com.fitpolo.support.callback.MokoOrderTaskCallback;
import com.fitpolo.support.entity.OrderEnum;
import com.fitpolo.support.entity.OrderType;

/**
 * @Date 2017/5/11
 * @Author wenzheng.liu
 * @Description 设置来电震动
 * @ClassPath com.fitpolo.support.task.NotifyPhoneTask
 */
public class NotifyPhoneTask extends OrderTask {
    private static final int ORDERDATA_LENGTH = 20;

    private static final int HEADER_PHONE_SHAKE = 0X99;
    private byte[] orderData;

    public NotifyPhoneTask(MokoOrderTaskCallback callback, String showText, boolean isPhoneNumber) {
        super(OrderType.WRITE, OrderEnum.setPhoneComingShake, callback, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
        orderData = new byte[ORDERDATA_LENGTH];
        orderData[0] = (byte) HEADER_PHONE_SHAKE;
        if (isPhoneNumber) {
            orderData[1] = 0x01;
        } else {
            orderData[1] = 0x00;
        }
        orderData[2] = 0x00;
        orderData[3] = showText.length() > 16 ? Integer.valueOf(Integer.toHexString(16), 16).byteValue() : Integer.valueOf(Integer.toHexString(showText.length()), 16).byteValue();
        for (int i = 0; i < showText.length() && i < 16; i++) {
            int c = (int) showText.charAt(i);
            orderData[i + 4] = Integer.valueOf(Integer.toHexString(c), 16).byteValue();
        }
    }

    @Override
    public byte[] assemble() {
        return orderData;
    }
}
