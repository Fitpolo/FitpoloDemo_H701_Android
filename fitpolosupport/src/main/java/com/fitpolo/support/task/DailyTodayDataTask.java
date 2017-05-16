package com.fitpolo.support.task;

import com.fitpolo.support.FitConstant;
import com.fitpolo.support.OrderEnum;
import com.fitpolo.support.callback.OrderCallback;
import com.fitpolo.support.entity.BaseResponse;

/**
 * @Date 2017/5/11
 * @Author wenzheng.liu
 * @Description 获取今天的记步，睡眠，心率数据
 * @ClassPath com.fitpolo.support.task.DailyTodayDataTask
 */
public class DailyTodayDataTask extends OrderTask {

    public DailyTodayDataTask(OrderCallback callback) {
        setOrder(OrderEnum.getTodayData);
        setCallback(callback);
        setResponse(new BaseResponse());
    }

    @Override
    public byte[] assemble(Object... objects) {
        byte[] byteArray = new byte[2];
        byteArray[0] = (byte) FitConstant.HEADER_GETDATA;
        byteArray[1] = (byte) FitConstant.GET_TODAY_DATA;
        return byteArray;
    }
}
