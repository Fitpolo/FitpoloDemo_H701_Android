package com.fitpolo.support.task;

import com.fitpolo.support.MokoSupport;
import com.fitpolo.support.callback.MokoOrderTaskCallback;
import com.fitpolo.support.entity.OrderEnum;
import com.fitpolo.support.entity.OrderType;
import com.fitpolo.support.log.LogModule;
import com.fitpolo.support.utils.DigitalConver;

/**
 * @Date 2017/5/11
 * @Author wenzheng.liu
 * @Description 获取固件版本号
 * @ClassPath com.fitpolo.support.task.FirmwareVersionTask
 */
public class FirmwareVersionTask extends OrderTask {
    private static final int ORDERDATA_LENGTH = 2;
    // 获取数据
    private static final int HEADER_GETDATA = 0x16;
    // 获取固件版本号
    private static final int GET_FIRMWARE_VERSION = 0x06;

    private byte[] orderData;

    public FirmwareVersionTask(MokoOrderTaskCallback callback) {
        super(OrderType.WRITE, OrderEnum.getFirmwareVersion, callback, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
        orderData = new byte[ORDERDATA_LENGTH];
        orderData[0] = (byte) HEADER_GETDATA;
        orderData[1] = (byte) GET_FIRMWARE_VERSION;
    }

    @Override
    public byte[] assemble() {
        return orderData;
    }

    @Override
    public void parseValue(byte[] value) {
        if (order.getOrderHeader() != DigitalConver.byte2Int(value[0])) {
            return;
        }
        LogModule.i(order.getOrderName() + "成功");
        orderStatus = OrderTask.ORDER_STATUS_SUCCESS;

        int major = DigitalConver.byte2Int(value[1]);
        int minor = DigitalConver.byte2Int(value[2]);
        int revision = DigitalConver.byte2Int(value[3]);
        String version = String.format("%s.%s.%s", major, minor, revision);
        MokoSupport.versionCodeShow = version;

        MokoSupport.getInstance().pollTask();
        callback.onOrderResult(response);
        MokoSupport.getInstance().executeTask(callback);
    }
}
