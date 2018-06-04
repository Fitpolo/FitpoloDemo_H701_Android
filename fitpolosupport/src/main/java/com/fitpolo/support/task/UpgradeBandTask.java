package com.fitpolo.support.task;

import com.fitpolo.support.callback.MokoOrderTaskCallback;
import com.fitpolo.support.entity.CRCVerifyResponse;
import com.fitpolo.support.entity.OrderEnum;
import com.fitpolo.support.entity.OrderType;

/**
 * @Date 2017/8/15
 * @Author wenzheng.liu
 * @Description 升级固件
 * @ClassPath com.fitpolo.support.task.UpgradeBandTask
 */
public class UpgradeBandTask extends OrderTask {
    private static final int ORDERDATA_LENGTH = 20;
    // 固件升级
    public static final int HEADER_UPGRADE_BAND = 0x29;

    private byte[] orderData;

    public UpgradeBandTask(MokoOrderTaskCallback callback, byte[] packageIndex, byte[] fileBytes) {
        super(OrderType.WRITE, OrderEnum.getCRCVerifyResult, callback, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
        setResponse(new CRCVerifyResponse());
        orderData = new byte[ORDERDATA_LENGTH];
        orderData[0] = HEADER_UPGRADE_BAND;
        orderData[1] = packageIndex[0];
        orderData[2] = packageIndex[1];
        for (int i = 0; i < fileBytes.length; i++) {
            orderData[3 + i] = fileBytes[i];
        }
    }

    @Override
    public byte[] assemble() {
        return orderData;
    }
}
