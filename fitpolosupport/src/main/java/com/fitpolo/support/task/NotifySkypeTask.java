package com.fitpolo.support.task;

import androidx.annotation.NonNull;

import com.fitpolo.support.callback.MokoOrderTaskCallback;
import com.fitpolo.support.entity.OrderEnum;
import com.fitpolo.support.entity.OrderType;

/**
 * @Date 2017/5/11
 * @Author wenzheng.liu
 * @Description skype提醒
 * @ClassPath com.fitpolo.support.task.NotifySkypeTask
 */
public class NotifySkypeTask extends OrderTask {
    private static final int ORDERDATA_LENGTH = 20;

    private static final int HEADER_SKYPE_SHAKE = 0xA0;
    private byte[] orderData;


    public NotifySkypeTask(MokoOrderTaskCallback callback,@NonNull String showText) {
        super(OrderType.WRITE, OrderEnum.setSkypeNotify, callback, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
        orderData = new byte[ORDERDATA_LENGTH];
        orderData[0] = (byte) HEADER_SKYPE_SHAKE;
        orderData[1] = 0x00;
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
