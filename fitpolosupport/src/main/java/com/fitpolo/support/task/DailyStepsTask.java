package com.fitpolo.support.task;

import com.fitpolo.support.FitConstant;
import com.fitpolo.support.OrderEnum;
import com.fitpolo.support.callback.OrderCallback;
import com.fitpolo.support.entity.BaseResponse;

/**
 * @Date 2017/5/11
 * @Author wenzheng.liu
 * @Description 获取记步数据
 * @ClassPath com.fitpolo.support.task.DailyStepsTask
 */
public class DailyStepsTask extends OrderTask {

    public DailyStepsTask(OrderCallback callback) {
        setOrder(OrderEnum.getDailySteps);
        setCallback(callback);
        setResponse(new BaseResponse());
    }

    @Override
    public byte[] assemble(Object... objects) {
        byte[] byteArray = new byte[2];
        byteArray[0] = (byte) FitConstant.HEADER_GETDATA;
        byteArray[1] = (byte) FitConstant.GET_DAILY_STEPS;
        return byteArray;
    }
}
