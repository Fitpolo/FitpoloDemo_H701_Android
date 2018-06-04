package com.fitpolo.support.task;

import com.fitpolo.support.MokoConstants;
import com.fitpolo.support.MokoSupport;
import com.fitpolo.support.callback.MokoOrderTaskCallback;
import com.fitpolo.support.entity.AutoLighten;
import com.fitpolo.support.entity.CustomScreen;
import com.fitpolo.support.entity.OrderEnum;
import com.fitpolo.support.entity.OrderType;
import com.fitpolo.support.entity.SitAlert;
import com.fitpolo.support.log.LogModule;
import com.fitpolo.support.utils.DigitalConver;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * @Date 2017/5/11
 * @Author wenzheng.liu
 * @Description 读取手环设置
 * @ClassPath com.fitpolo.support.task.ReadSettingTask
 */
public class ReadSettingTask extends OrderTask {
    private static final int ORDERDATA_LENGTH = 2;

    private byte[] orderData;

    public ReadSettingTask(MokoOrderTaskCallback callback) {
        super(OrderType.WRITE, OrderEnum.READ_SETTING, callback, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
        orderData = new byte[ORDERDATA_LENGTH];
        orderData[0] = (byte) MokoConstants.HEADER_READ_SEND;
        orderData[1] = (byte) order.getOrderHeader();
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
        if (0x06 != DigitalConver.byte2Int(value[2])) {
            return;
        }
        MokoSupport.getInstance().setUnitTypeBritish(DigitalConver.byte2Int(value[3]) == 1);

        MokoSupport.getInstance().setTimeFormat(DigitalConver.byte2Int(value[4]));

        String binaryStr = DigitalConver.byte2binaryString(value[5]);
        CustomScreen customScreen = new CustomScreen(
                str2Boolean(binaryStr, 2, 3),
                str2Boolean(binaryStr, 3, 4),
                str2Boolean(binaryStr, 4, 5),
                str2Boolean(binaryStr, 5, 6),
                str2Boolean(binaryStr, 6, 7));
        MokoSupport.getInstance().setCustomScreen(customScreen);

        MokoSupport.getInstance().setLastScreen(DigitalConver.byte2Int(value[6]) == 1);

        MokoSupport.getInstance().setHeartRateInterval(DigitalConver.byte2Int(value[7]));

        AutoLighten autoLighten = new AutoLighten();
        autoLighten.autoLighten = DigitalConver.byte2Int(value[8]);

        MokoSupport.getInstance().setAutoLighten(autoLighten);
        LogModule.i(order.getOrderName() + "成功");
        orderStatus = OrderTask.ORDER_STATUS_SUCCESS;

        MokoSupport.getInstance().pollTask();
        callback.onOrderResult(response);
        MokoSupport.getInstance().executeTask(callback);
    }

    private boolean str2Boolean(String str, int start, int end) {
        return "1".equals(str.substring(start, end));
    }
}
