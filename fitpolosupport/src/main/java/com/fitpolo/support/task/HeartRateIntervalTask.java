package com.fitpolo.support.task;

import com.fitpolo.support.FitConstant;
import com.fitpolo.support.OrderEnum;
import com.fitpolo.support.callback.OrderCallback;
import com.fitpolo.support.entity.BaseResponse;

/**
 * @Date 2017/5/11
 * @Author wenzheng.liu
 * @Description 设置心率间隔
 * @ClassPath com.fitpolo.support.task.HeartRateIntervalTask
 */
public class HeartRateIntervalTask extends OrderTask {
    private int heartRateInterval;// 0：关闭；1：10分钟；2：20分钟；3：30分钟

    public HeartRateIntervalTask(OrderCallback callback, int heartRateInterval) {
        setOrder(OrderEnum.setHeartRateInterval);
        setCallback(callback);
        setResponse(new BaseResponse());
        this.heartRateInterval = heartRateInterval;
    }

    @Override
    public byte[] assemble(Object... objects) {
        byte[] byteArray = new byte[4];
        byteArray[0] = (byte) FitConstant.HEADER_GETDATA;
        byteArray[1] = (byte) FitConstant.GET_SET_HEART_RATE_INTERVAL;
        byteArray[2] = (byte) heartRateInterval;
        byteArray[3] = 0;
        return byteArray;
    }
}
