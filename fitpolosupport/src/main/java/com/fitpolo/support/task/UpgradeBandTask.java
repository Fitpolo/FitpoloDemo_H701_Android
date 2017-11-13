package com.fitpolo.support.task;

import com.fitpolo.support.FitConstant;
import com.fitpolo.support.OrderEnum;
import com.fitpolo.support.callback.OrderCallback;
import com.fitpolo.support.entity.CRCVerifyResponse;

/**
 * @Date 2017/8/15
 * @Author wenzheng.liu
 * @Description 升级固件
 * @ClassPath com.fitpolo.support.task.UpgradeBandTask
 */
public class UpgradeBandTask extends OrderTask {
    private byte[] packageIndex;
    private byte[] fileBytes;

    public UpgradeBandTask(OrderCallback callback, byte[] packageIndex, byte[] fileBytes) {
        setOrder(OrderEnum.getCRCVerifyResult);
        setCallback(callback);
        setResponse(new CRCVerifyResponse());
        this.packageIndex = packageIndex;
        this.fileBytes = fileBytes;
    }

    @Override
    public byte[] assemble(Object... objects) {
        byte[] byteArray = new byte[20];
        byteArray[0] = FitConstant.HEADER_UPGRADE_BAND;
        byteArray[1] = packageIndex[0];
        byteArray[2] = packageIndex[1];
        for (int i = 0; i < fileBytes.length; i++) {
            byteArray[3 + i] = fileBytes[i];
        }
        return byteArray;
    }
}
