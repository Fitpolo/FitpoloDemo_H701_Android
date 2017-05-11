package com.fitpolo.support.task;

import com.fitpolo.support.FitConstant;
import com.fitpolo.support.OrderEnum;
import com.fitpolo.support.callback.OrderCallback;
import com.fitpolo.support.entity.InnerVersion;

/**
 * @Date 2017/5/11
 * @Author wenzheng.liu
 * @Description 获取内部版本号
 * @ClassPath com.fitpolo.support.task.InnerVersionTask
 */
public class InnerVersionTask extends OrderTask {

    public InnerVersionTask(OrderCallback callback, InnerVersion innerVersion) {
        setOrder(OrderEnum.getInnerVersion);
        setCallback(callback);
        setResponse(innerVersion);
    }

    @Override
    public byte[] assemble() {
        byte[] byteArray = new byte[2];
        byteArray[0] = (byte) FitConstant.HEADER_GETDATA;
        byteArray[1] = (byte) FitConstant.GET_INNER_VERSION;
        return byteArray;
    }
}
