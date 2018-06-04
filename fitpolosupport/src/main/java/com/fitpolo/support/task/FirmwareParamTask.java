package com.fitpolo.support.task;

import com.fitpolo.support.MokoSupport;
import com.fitpolo.support.callback.MokoOrderTaskCallback;
import com.fitpolo.support.entity.OrderEnum;
import com.fitpolo.support.entity.OrderType;
import com.fitpolo.support.log.LogModule;
import com.fitpolo.support.utils.DigitalConver;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * @Date 2017/11/22
 * @Author wenzheng.liu
 * @Description 获取硬件参数
 * @ClassPath com.fitpolo.support.task.FirmwareParamTask
 */
public class FirmwareParamTask extends OrderTask {
    private static final int ORDERDATA_LENGTH = 2;
    // 获取数据
    private static final int HEADER_GETDATA = 0x16;
    // 获取硬件阐述
    private static final int GET_FIRMWARE_PARAM = 0x22;

    private byte[] orderData;

    public FirmwareParamTask(MokoOrderTaskCallback callback) {
        super(OrderType.WRITE, OrderEnum.getFirmwareParam, callback, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
        orderData = new byte[ORDERDATA_LENGTH];
        orderData[0] = (byte) HEADER_GETDATA;
        orderData[1] = (byte) GET_FIRMWARE_PARAM;
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

        if (value.length > 14) {
            LogModule.i("flash状态：" + DigitalConver.byte2HexString(value[2]));
            byte[] reflectiveThreshold = new byte[2];
            System.arraycopy(value, 3, reflectiveThreshold, 0, 2);
            LogModule.i("当前反光阈值：" + DigitalConver.byteArr2Str(reflectiveThreshold));
            byte[] reflectiveValue = new byte[2];
            System.arraycopy(value, 5, reflectiveValue, 0, 2);
            LogModule.i("当前反光值：" + DigitalConver.byteArr2Str(reflectiveValue));
            // 上次充电时间
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.YEAR, 2000 + DigitalConver.byte2Int(value[7]));
            calendar.set(Calendar.MONTH, DigitalConver.byte2Int(value[8]) - 1);
            calendar.set(Calendar.DAY_OF_MONTH, DigitalConver.byte2Int(value[9]));
            calendar.set(Calendar.HOUR_OF_DAY, DigitalConver.byte2Int(value[10]));
            calendar.set(Calendar.MINUTE, DigitalConver.byte2Int(value[11]));
            String lastChargeTime = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(calendar.getTime());
            MokoSupport.getInstance().setLastChargeTime(lastChargeTime);
            // 生产日期
            int batchYear = 2000 + DigitalConver.byte2Int(value[12]);
            int batchWeek = DigitalConver.byte2Int(value[13]);
            LogModule.i("生产批次年：" + batchYear);
            LogModule.i("生产批次周：" + batchWeek);
            String productBatch = String.format("%d.%d", batchYear, batchWeek);
            MokoSupport.getInstance().setProductBatch(productBatch);
        }

        MokoSupport.getInstance().pollTask();
        callback.onOrderResult(response);
        MokoSupport.getInstance().executeTask(callback);
    }
}
