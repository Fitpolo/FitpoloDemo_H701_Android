package com.fitpolo.support.task;

import com.fitpolo.support.MokoSupport;
import com.fitpolo.support.callback.MokoOrderTaskCallback;
import com.fitpolo.support.entity.FirmwareEnum;
import com.fitpolo.support.entity.OrderEnum;
import com.fitpolo.support.entity.OrderType;
import com.fitpolo.support.log.LogModule;
import com.fitpolo.support.utils.DigitalConver;

/**
 * @Date 2017/5/11
 * @Author wenzheng.liu
 * @Description 获取内部版本号
 * @ClassPath com.fitpolo.support.task.InnerVersionTask
 */
public class InnerVersionTask extends OrderTask {
    private static final int ORDERDATA_LENGTH = 2;
    // 获取数据
    private static final int HEADER_GETDATA = 0x16;
    // 获取内部版本号
    private static final int GET_INNER_VERSION = 0x09;

    private byte[] orderData;

    public InnerVersionTask(MokoOrderTaskCallback callback) {
        super(OrderType.WRITE, OrderEnum.getInnerVersion, callback, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
        orderData = new byte[ORDERDATA_LENGTH];
        orderData[0] = (byte) HEADER_GETDATA;
        orderData[1] = (byte) GET_INNER_VERSION;
    }

    @Override
    public byte[] assemble() {
        return orderData;
    }

    @Override
    public void parseValue(byte[] value) {
        if (order.getOrderHeader() != DigitalConver.byte2Int(value[1])) {
            return;
        }
        LogModule.i(order.getOrderName() + "成功");
        orderStatus = OrderTask.ORDER_STATUS_SUCCESS;
        MokoSupport.showHeartRate = (value[3] & 0xFF) == 1;
        MokoSupport.supportNewData = (value[4] & 0xFF) > 25;
        MokoSupport.supportNotifyAndRead = (value[4] & 0xFF) > 31;

        StringBuilder sb = new StringBuilder();
        for (int i = 2; i < value.length; i++) {
            sb.append(DigitalConver.byte2HexString(value[i]));
            if (i < value.length - 1) {
                sb.append(".");
            }
        }
        // 内部版本，判断升级用
        MokoSupport.versionCode = sb.toString();
        // 大版本号，区分升级固件
        MokoSupport.firmwareEnum = FirmwareEnum.fromHeader(DigitalConver.byte2HexString(value[2]));
        if (MokoSupport.firmwareEnum == null) {
            return;
        }
        // 小版本号，判断部分功能有无
        MokoSupport.versionCodeLast = value[4] & 0xFF;
        LogModule.i("Version code last：" + MokoSupport.versionCodeLast);
        // 判断是否升级
        MokoSupport.canUpgrade = MokoSupport.versionCodeLast < MokoSupport.firmwareEnum.getLastestVersion();

        MokoSupport.getInstance().pollTask();
        callback.onOrderResult(response);
        MokoSupport.getInstance().executeTask(callback);
    }
}
