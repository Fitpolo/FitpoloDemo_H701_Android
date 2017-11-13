package com.fitpolo.support.task;

import com.fitpolo.support.FitConstant;
import com.fitpolo.support.OrderEnum;
import com.fitpolo.support.callback.OrderCallback;
import com.fitpolo.support.entity.CRCVerifyResponse;
import com.fitpolo.support.utils.DigitalConver;

/**
 * @Date 2017/8/14 0014
 * @Author wenzheng.liu
 * @Description 固件升级CRC校验
 * @ClassPath com.fitpolo.support.task.CRCVerifyTask
 */
public class CRCVerifyTask extends OrderTask {
    private int fileCRCResult;
    private int fileLengthResult;

    public CRCVerifyTask(OrderCallback callback, int fileCRCResult, int fileLengthResult) {
        setOrder(OrderEnum.getCRCVerifyResult);
        setCallback(callback);
        setResponse(new CRCVerifyResponse());
        this.fileCRCResult = fileCRCResult;
        this.fileLengthResult = fileLengthResult;
    }

    @Override
    public byte[] assemble(Object... objects) {
        byte[] byteArray = new byte[7];
        byteArray[0] = FitConstant.HEADER_CRC_VERIFY;
        // crc
        byte[] crc = DigitalConver.toByteArray(fileCRCResult, 2);
        byteArray[1] = crc[0];
        byteArray[2] = crc[1];
        // length
        byte[] fileLength = DigitalConver.toByteArray(fileLengthResult, 4);
        byteArray[3] = fileLength[0];
        byteArray[4] = fileLength[1];
        byteArray[5] = fileLength[2];
        byteArray[6] = fileLength[3];
        return byteArray;
    }


}
