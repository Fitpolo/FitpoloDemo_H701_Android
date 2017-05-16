package com.fitpolo.support.task;

import com.fitpolo.support.FitConstant;
import com.fitpolo.support.OrderEnum;
import com.fitpolo.support.callback.OrderCallback;
import com.fitpolo.support.entity.BaseResponse;

/**
 * @Date 2017/5/11
 * @Author wenzheng.liu
 * @Description 设置上次显示
 * @ClassPath com.fitpolo.support.task.LastShowTask
 */
public class LastShowTask extends OrderTask {
    private int lastShow;// 1：打开；0：关闭

    public LastShowTask(OrderCallback callback, int lastShow) {
        setOrder(OrderEnum.setLastShow);
        setCallback(callback);
        setResponse(new BaseResponse());
        this.lastShow = lastShow;
    }

    @Override
    public byte[] assemble(Object... objects) {
        byte[] byteArray = new byte[2];
        byteArray[0] = (byte) FitConstant.HEADER_SET_LAST_SHOW;
        byteArray[1] = (byte) lastShow;
        return byteArray;
    }
}
