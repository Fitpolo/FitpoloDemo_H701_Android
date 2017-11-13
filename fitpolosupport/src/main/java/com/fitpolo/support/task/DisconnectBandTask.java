package com.fitpolo.support.task;

import com.fitpolo.support.FitConstant;
import com.fitpolo.support.OrderEnum;
import com.fitpolo.support.callback.OrderCallback;
import com.fitpolo.support.entity.BaseResponse;

/**
 * @Date 2017/11/1
 * @Author wenzheng.liu
 * @Description 断开手环
 * @ClassPath com.fitpolo.support.task.DisconnectBandTask
 */
public class DisconnectBandTask extends OrderTask {

    public DisconnectBandTask(OrderCallback callback) {
        setOrder(OrderEnum.disconnectBand);
        setCallback(callback);
        setResponse(new BaseResponse());
    }

    @Override
    public byte[] assemble(Object... objects) {
        byte[] byteArray = new byte[2];
        byteArray[0] = (byte) FitConstant.HEADER_CLEAR_BAND_DATA;
        byteArray[1] = (byte) 22;
        return byteArray;
    }
}
