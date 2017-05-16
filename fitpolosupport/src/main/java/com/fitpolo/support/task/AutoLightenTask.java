package com.fitpolo.support.task;

import com.fitpolo.support.FitConstant;
import com.fitpolo.support.OrderEnum;
import com.fitpolo.support.callback.OrderCallback;
import com.fitpolo.support.entity.BaseResponse;

/**
 * @Date 2017/5/11
 * @Author wenzheng.liu
 * @Description 设置自动亮屏
 * @ClassPath com.fitpolo.support.task.AutoLightenTask
 */
public class AutoLightenTask extends OrderTask {
    private int autoLighten;// 0：打开；1：关闭

    public AutoLightenTask(OrderCallback callback, int autoLighten) {
        setOrder(OrderEnum.setAutoLigten);
        setCallback(callback);
        setResponse(new BaseResponse());
        this.autoLighten = autoLighten;
    }

    @Override
    public byte[] assemble(Object... objects) {
        byte[] byteArray = new byte[2];
        byteArray[0] = (byte) FitConstant.HEADER_SET_AUTO_LIGHTEN;
        byteArray[1] = (byte) autoLighten;
        return byteArray;
    }
}
