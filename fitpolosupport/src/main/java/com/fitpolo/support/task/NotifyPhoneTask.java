package com.fitpolo.support.task;

import androidx.annotation.NonNull;

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

    public NotifyPhoneTask(MokoOrderTaskCallback callback, @NonNull String showText, boolean isPhoneNumber) {
        super(OrderType.WRITE, OrderEnum.setPhoneComingShake, callback, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
        orderData = new byte[ORDERDATA_LENGTH];
        orderData[0] = (byte) HEADER_PHONE_SHAKE;
        if (isPhoneNumber) {
            orderData[1] = 0x01;
        } else {
            orderData[1] = 0x00;
        }
        orderData[2] = 0x00;
        int length = showText == null ? 0 : showText.length() > 16 ? 0x10 : showText.length();
        orderData[3] = (byte) length;
        for (int i = 0; i < length && i < 16; i++) {
            int c = (int) showText.charAt(i);
            orderData[i + 4] = (byte) c;
        }
    }

    @Override
    public byte[] assemble() {
        return orderData;
    }
}
