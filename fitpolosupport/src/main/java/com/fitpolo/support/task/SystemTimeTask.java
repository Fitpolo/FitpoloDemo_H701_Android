package com.fitpolo.support.task;

import com.fitpolo.support.FitConstant;
import com.fitpolo.support.OrderEnum;
import com.fitpolo.support.callback.OrderCallback;
import com.fitpolo.support.entity.BaseResponse;

import java.util.Calendar;

/**
 * @Date 2017/5/11
 * @Author wenzheng.liu
 * @Description 设置系统时间
 * @ClassPath com.fitpolo.support.task.InnerVersionTask
 */
public class SystemTimeTask extends OrderTask {

    public SystemTimeTask(OrderCallback callback) {
        setOrder(OrderEnum.setSystemTime);
        setCallback(callback);
        setResponse(new BaseResponse());
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
        byte[] byteArray = new byte[7];
        byteArray[0] = (byte) FitConstant.HEADER_SET_SYSTEM_TIME;
        byteArray[1] = (byte) (year - 2000);
        byteArray[2] = (byte) month;
        byteArray[3] = (byte) date;
        byteArray[4] = (byte) hour;
        byteArray[5] = (byte) minute;
        byteArray[6] = (byte) second;
        return byteArray;
    }
}
