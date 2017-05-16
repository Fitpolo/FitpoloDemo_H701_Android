package com.fitpolo.support.task;

import com.fitpolo.support.FitConstant;
import com.fitpolo.support.OrderEnum;
import com.fitpolo.support.callback.OrderCallback;
import com.fitpolo.support.entity.BaseResponse;

/**
 * @Date 2017/5/11
 * @Author wenzheng.liu
 * @Description 设置单位制式
 * @ClassPath com.fitpolo.support.task.UnitTypeTask
 */
public class UnitTypeTask extends OrderTask {
    private int unitType;// 0：中式；1：英式

    public UnitTypeTask(OrderCallback callback, int unitType) {
        setOrder(OrderEnum.setUnitType);
        setCallback(callback);
        setResponse(new BaseResponse());
        this.unitType = unitType;
    }

    @Override
    public byte[] assemble(Object... objects) {
        byte[] byteArray = new byte[2];
        byteArray[0] = (byte) FitConstant.HEADER_SET_UNIT_TYPE;
        byteArray[1] = (byte) unitType;
        return byteArray;
    }
}
