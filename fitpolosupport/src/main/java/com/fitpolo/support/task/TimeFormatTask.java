package com.fitpolo.support.task;

import com.fitpolo.support.FitConstant;
import com.fitpolo.support.OrderEnum;
import com.fitpolo.support.callback.OrderCallback;
import com.fitpolo.support.entity.BaseResponse;

/**
 * @Date 2017/5/11
 * @Author wenzheng.liu
 * @Description 设置显示时间格式
 * @ClassPath com.fitpolo.support.task.TimeFormatTask
 */
public class TimeFormatTask extends OrderTask {
    private int timeFormat;// 0：24；1：12

    public TimeFormatTask(OrderCallback callback, int timeFormat) {
        setOrder(OrderEnum.setTimeFormat);
        setCallback(callback);
        setResponse(new BaseResponse());
        this.timeFormat = timeFormat;
    }

    @Override
    public byte[] assemble(Object... objects) {
        byte[] byteArray = new byte[2];
        byteArray[0] = (byte) FitConstant.HEADER_SET_TIME_FORMAT;
        byteArray[1] = (byte) timeFormat;
        return byteArray;
    }
}
