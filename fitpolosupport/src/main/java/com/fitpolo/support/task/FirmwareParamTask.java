package com.fitpolo.support.task;

import com.fitpolo.support.FitConstant;
import com.fitpolo.support.OrderEnum;
import com.fitpolo.support.callback.OrderCallback;
import com.fitpolo.support.entity.BaseResponse;

/**
 * @Date 2017/11/22
 * @Author wenzheng.liu
 * @Description 获取硬件参数
 * @ClassPath com.fitpolo.support.task.FirmwareParamTask
 */
public class FirmwareParamTask extends OrderTask {

    public FirmwareParamTask(OrderCallback callback) {
        setOrder(OrderEnum.getFirmwareParam);
        setCallback(callback);
        setResponse(new BaseResponse());
    }

    @Override
    public byte[] assemble(Object... objects) {
        byte[] byteArray = new byte[2];
        byteArray[0] = (byte) FitConstant.HEADER_GETDATA;
        byteArray[1] = (byte) FitConstant.GET_FIRMWARE_PARAM;
        return byteArray;
    }
}
