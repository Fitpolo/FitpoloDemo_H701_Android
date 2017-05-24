package com.fitpolo.support.task;

import com.fitpolo.support.OrderEnum;
import com.fitpolo.support.callback.OrderCallback;
import com.fitpolo.support.entity.BaseResponse;

/**
 * @Date 2017/5/11
 * @Author wenzheng.liu
 * @Description 设置短信震动
 * @ClassPath com.fitpolo.support.task.SmsComingShakeTask
 */
public class SmsComingShakeTask extends OrderTask {

    private String showText;
    private boolean isPhoneNumber;

    public SmsComingShakeTask(OrderCallback callback, String showText, boolean isPhoneNumber) {
        setOrder(OrderEnum.setSmsComingShake);
        setCallback(callback);
        setResponse(new BaseResponse());
        this.showText = showText;
        this.isPhoneNumber = isPhoneNumber;
    }

    @Override
    public byte[] assemble(Object... objects) {
        byte[] byteArray = new byte[20];
        byteArray[0] = Integer.valueOf(Integer.toHexString(154), 16).byteValue();
        if (isPhoneNumber) {
            byteArray[1] = 0x01;
        } else {
            byteArray[1] = 0x00;
        }
        byteArray[2] = 0x00;
        byteArray[3] = showText.length() > 16 ? Integer.valueOf(Integer.toHexString(16), 16).byteValue() : Integer.valueOf(Integer.toHexString(showText.length()), 16).byteValue();
        for (int i = 0; i < showText.length() && i < 16; i++) {
            int c = (int) showText.charAt(i);
            byteArray[i + 4] = Integer.valueOf(Integer.toHexString(c), 16).byteValue();
        }
        return byteArray;
    }
}
