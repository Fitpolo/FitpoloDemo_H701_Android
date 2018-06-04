package com.fitpolo.support.task;

import com.fitpolo.support.MokoSupport;
import com.fitpolo.support.callback.MokoOrderTaskCallback;
import com.fitpolo.support.entity.CRCVerifyResponse;
import com.fitpolo.support.entity.OrderEnum;
import com.fitpolo.support.entity.OrderType;
import com.fitpolo.support.log.LogModule;
import com.fitpolo.support.utils.DigitalConver;

/**
 * @Date 2017/8/14 0014
 * @Author wenzheng.liu
 * @Description 固件升级CRC校验
 * @ClassPath com.fitpolo.support.task.CRCVerifyTask
 */
public class CRCVerifyTask extends OrderTask {
    private static final int ORDERDATA_LENGTH = 7;
    // CRC校验
    public static final int HEADER_CRC_VERIFY = 0x28;
    // 返回应答头
    public static final int RESPONSE_HEADER_ACK = 0x96;
    // 返回数据包头
    public static final int RESPONSE_HEADER_PACKAGE = 0xA6;
    // 返回数据包结果
    public static final int RESPONSE_HEADER_PACKAGE_RESULT = 0xA7;
    private byte[] orderData;

    public CRCVerifyTask(MokoOrderTaskCallback callback, int fileCRCResult, int fileLengthResult) {
        super(OrderType.WRITE, OrderEnum.getCRCVerifyResult, callback, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
        this.response = new CRCVerifyResponse();
        this.response.order = OrderEnum.getCRCVerifyResult;
        this.response.responseType = OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE;
        this.delayTime = 5000;

        orderData = new byte[ORDERDATA_LENGTH];
        orderData[0] = HEADER_CRC_VERIFY;
        // crc
        byte[] crc = DigitalConver.int2ByteArr(fileCRCResult, 2);
        orderData[1] = crc[0];
        orderData[2] = crc[1];
        // length
        byte[] fileLength = DigitalConver.int2ByteArr(fileLengthResult, 4);
        orderData[3] = fileLength[0];
        orderData[4] = fileLength[1];
        orderData[5] = fileLength[2];
        orderData[6] = fileLength[3];
    }

    @Override
    public byte[] assemble() {
        return orderData;
    }

    @Override
    public void parseValue(byte[] value) {
        if (order.getOrderHeader() != DigitalConver.byte2Int(value[1]) && 0xA6 != DigitalConver.byte2Int(value[0]) && 0xA7 != DigitalConver.byte2Int(value[0])) {
            return;
        }
        CRCVerifyResponse response = (CRCVerifyResponse) this.response;
        int header = DigitalConver.byte2Int(value[0]);
        response.header = header;
        switch (header) {
            case RESPONSE_HEADER_ACK:
                LogModule.i("CRC校验成功！");
                break;
            case RESPONSE_HEADER_PACKAGE:
                orderStatus = OrderTask.ORDER_STATUS_SUCCESS;
                byte[] index = new byte[2];
                System.arraycopy(value, 1, index, 0, 2);
                response.packageResult = index;
                callback.onOrderResult(response);
                break;
            case RESPONSE_HEADER_PACKAGE_RESULT:
                orderStatus = OrderTask.ORDER_STATUS_SUCCESS;
                int ack = DigitalConver.byte2Int(value[1]);
                response.ack = ack;
                MokoSupport.getInstance().pollTask();
                callback.onOrderResult(response);
                MokoSupport.getInstance().executeTask(callback);
                break;
        }
    }
}
