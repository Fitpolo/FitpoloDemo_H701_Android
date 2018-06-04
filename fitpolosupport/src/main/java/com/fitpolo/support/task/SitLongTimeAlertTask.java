package com.fitpolo.support.task;

import android.text.TextUtils;

import com.fitpolo.support.MokoSupport;
import com.fitpolo.support.callback.MokoOrderTaskCallback;
import com.fitpolo.support.entity.OrderEnum;
import com.fitpolo.support.entity.OrderType;
import com.fitpolo.support.entity.SitAlert;
import com.fitpolo.support.log.LogModule;
import com.fitpolo.support.utils.DigitalConver;

/**
 * @Date 2017/5/11
 * @Author wenzheng.liu
 * @Description 设置久坐提醒
 * @ClassPath com.fitpolo.support.task.SitLongTimeAlertTask
 */
public class SitLongTimeAlertTask extends OrderTask {
    private static final int ORDERDATA_LENGTH = 17;
    // 久坐提醒
    private static final int HEADER_SET_SIT_LONG_TIME_ALERT = 0x2A;

    private byte[] orderData;
    SitAlert sitAlert;

    public SitLongTimeAlertTask(MokoOrderTaskCallback callback, SitAlert sitAlert) {
        super(OrderType.WRITE, OrderEnum.setSitLongTimeAlert, callback, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
        this.sitAlert = sitAlert;
        orderData = new byte[ORDERDATA_LENGTH];
    }

    @Override
    public byte[] assemble() {
        if (sitAlert != null) {
            LogModule.i(sitAlert.toString());
        }
        orderData[0] = (byte) HEADER_SET_SIT_LONG_TIME_ALERT;
        orderData[1] = 0x00;
        String stateStr = "1111111";
        String alertSwitch = sitAlert != null && sitAlert.alertSwitch == 1 ? "1" : "0";
        int state = Integer.parseInt(DigitalConver.binaryString2hexString(alertSwitch + stateStr), 16);
        orderData[2] = (byte) state;
        String startTime = sitAlert != null && !TextUtils.isEmpty(sitAlert.startTime)
                ? sitAlert.startTime : "09:00";
        String endTime = sitAlert != null && !TextUtils.isEmpty(sitAlert.endTime)
                ? sitAlert.endTime : "22:00";
        orderData[3] = (byte) Integer.parseInt(startTime.split(":")[0]);
        orderData[4] = (byte) Integer.parseInt(startTime.split(":")[1]);
        orderData[5] = (byte) Integer.parseInt(endTime.split(":")[0]);
        orderData[6] = (byte) Integer.parseInt(endTime.split(":")[1]);
        orderData[7] = (byte) 0;
        orderData[8] = (byte) 0;
        orderData[9] = (byte) 0;
        orderData[10] = (byte) 0;
        orderData[11] = (byte) 0;
        orderData[12] = (byte) 0;
        orderData[13] = (byte) 0;
        orderData[14] = (byte) 0;
        orderData[15] = (byte) 0;
        orderData[16] = (byte) 0;
        return orderData;
    }

    @Override
    public void parseValue(byte[] value) {
        if (order.getOrderHeader() != DigitalConver.byte2Int(value[1])) {
            return;
        }
        LogModule.i(order.getOrderName() + "成功");
        orderStatus = OrderTask.ORDER_STATUS_SUCCESS;
        MokoSupport.getInstance().pollTask();
        callback.onOrderResult(response);
        MokoSupport.getInstance().executeTask(callback);
    }
}
