package com.fitpolo.support.task;

import android.text.TextUtils;

import com.fitpolo.support.FitConstant;
import com.fitpolo.support.OrderEnum;
import com.fitpolo.support.callback.OrderCallback;
import com.fitpolo.support.entity.BaseResponse;
import com.fitpolo.support.entity.req.SitLongTimeAlert;
import com.fitpolo.support.log.LogModule;
import com.fitpolo.support.utils.DigitalConver;

/**
 * @Date 2017/5/11
 * @Author wenzheng.liu
 * @Description 设置自动亮屏
 * @ClassPath com.fitpolo.support.task.AutoLightenTask
 */
public class SitLongTimeAlertTask extends OrderTask {
    SitLongTimeAlert sitLongTimeAlert;

    public SitLongTimeAlertTask(OrderCallback callback, SitLongTimeAlert sitLongTimeAlert) {
        setOrder(OrderEnum.setSitLongTimeAlert);
        setCallback(callback);
        setResponse(new BaseResponse());
        this.sitLongTimeAlert = sitLongTimeAlert;
    }

    @Override
    public byte[] assemble(Object... objects) {
        if (sitLongTimeAlert != null) {
            LogModule.i(sitLongTimeAlert.toString());
        }
        byte[] byteArray = new byte[17];
        byteArray[0] = (byte) FitConstant.HEADER_SET_SIT_LONG_TIME_ALERT;
        byteArray[1] = 0x00;
        String stateStr = "1111111";
        String alertSwitch = sitLongTimeAlert != null && sitLongTimeAlert.alertSwitch == 1 ? "1" : "0";
        int state = Integer.parseInt(DigitalConver.binaryString2hexString(alertSwitch + stateStr), 16);
        byteArray[2] = (byte) state;
        String startTime = sitLongTimeAlert != null && !TextUtils.isEmpty(sitLongTimeAlert.startTime)
                ? sitLongTimeAlert.startTime : "09:00";
        String endTime = sitLongTimeAlert != null && !TextUtils.isEmpty(sitLongTimeAlert.endTime)
                ? sitLongTimeAlert.endTime : "22:00";
        byteArray[3] = (byte) Integer.parseInt(startTime.split(":")[0]);
        byteArray[4] = (byte) Integer.parseInt(startTime.split(":")[1]);
        byteArray[5] = (byte) Integer.parseInt(endTime.split(":")[0]);
        byteArray[6] = (byte) Integer.parseInt(endTime.split(":")[1]);
        byteArray[7] = (byte) 0;
        byteArray[8] = (byte) 0;
        byteArray[9] = (byte) 0;
        byteArray[10] = (byte) 0;
        byteArray[11] = (byte) 0;
        byteArray[12] = (byte) 0;
        byteArray[13] = (byte) 0;
        byteArray[14] = (byte) 0;
        byteArray[15] = (byte) 0;
        byteArray[16] = (byte) 0;
        return byteArray;
    }
}
