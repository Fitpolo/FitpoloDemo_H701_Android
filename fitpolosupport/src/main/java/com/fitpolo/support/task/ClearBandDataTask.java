package com.fitpolo.support.task;

import com.fitpolo.support.FitConstant;
import com.fitpolo.support.OrderEnum;
import com.fitpolo.support.callback.OrderCallback;
import com.fitpolo.support.entity.BaseResponse;

/**
 * @Date 2017/5/11
 * @Author wenzheng.liu
 * @Description 清除手环数据
 * @ClassPath com.fitpolo.support.task.ClearBandDataTask
 */
public class ClearBandDataTask extends OrderTask {

    public ClearBandDataTask(OrderCallback callback) {
        setOrder(OrderEnum.clearBandData);
        setCallback(callback);
        setResponse(new BaseResponse());
    }

    @Override
    public byte[] assemble(Object... objects) {
        byte[] byteArray = new byte[1];
        byteArray[0] = (byte) FitConstant.HEADER_CLEAR_BAND_DATA;
        return byteArray;
    }
}
