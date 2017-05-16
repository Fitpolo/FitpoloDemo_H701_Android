package com.fitpolo.support.task;

import com.fitpolo.support.FitConstant;
import com.fitpolo.support.OrderEnum;
import com.fitpolo.support.callback.OrderCallback;
import com.fitpolo.support.entity.BaseResponse;

/**
 * @Date 2017/5/11
 * @Author wenzheng.liu
 * @Description 获取睡眠指数
 * @ClassPath com.fitpolo.support.task.DailySleepIndexTask
 */
public class DailySleepIndexTask extends OrderTask {

    public DailySleepIndexTask(OrderCallback callback) {
        setOrder(OrderEnum.getDailySleepIndex);
        setCallback(callback);
        setResponse(new BaseResponse());
    }

    @Override
    public byte[] assemble(Object... objects) {
        byte[] byteArray = new byte[2];
        byteArray[0] = (byte) FitConstant.HEADER_GETDATA;
        byteArray[1] = (byte) FitConstant.GET_DAILY_SLEEP_INDEX;
        return byteArray;
    }
}
