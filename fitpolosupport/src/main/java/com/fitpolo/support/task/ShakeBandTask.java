package com.fitpolo.support.task;

import com.fitpolo.support.FitConstant;
import com.fitpolo.support.OrderEnum;
import com.fitpolo.support.callback.OrderCallback;
import com.fitpolo.support.entity.BaseResponse;

/**
 * @Date 2017/5/11
 * @Author wenzheng.liu
 * @Description 设置手环震动
 * @ClassPath com.fitpolo.support.task.ShakeBandTask
 */
public class ShakeBandTask extends OrderTask {

    public ShakeBandTask(OrderCallback callback) {
        setOrder(OrderEnum.setShakeBand);
        setCallback(callback);
        setResponse(new BaseResponse());
    }

    @Override
    public byte[] assemble(Object... objects) {
        byte[] byteArray = new byte[5];
        byteArray[0] = (byte) FitConstant.HEADER_SET_SHAKE_BAND;
        byteArray[1] = 0x02;
        byteArray[2] = 0x03;
        byteArray[3] = 0x0A;
        byteArray[4] = 0x0A;
        return byteArray;
    }
}
