package com.fitpolo.support.task;

import com.fitpolo.support.MokoConstants;
import com.fitpolo.support.MokoSupport;
import com.fitpolo.support.callback.MokoOrderTaskCallback;
import com.fitpolo.support.entity.BandAlarm;
import com.fitpolo.support.entity.OrderEnum;
import com.fitpolo.support.entity.OrderType;
import com.fitpolo.support.log.LogModule;
import com.fitpolo.support.utils.DigitalConver;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * @Date 2017/5/11
 * @Author wenzheng.liu
 * @Description 读取闹钟
 * @ClassPath com.fitpolo.support.task.ReadAlarmsTask
 */
public class ReadAlarmsTask extends OrderTask {
    private static final int ORDERDATA_LENGTH = 2;

    private byte[] orderData;
    private ArrayList<BandAlarm> alarms;

    public ReadAlarmsTask(MokoOrderTaskCallback callback) {
        super(OrderType.WRITE, OrderEnum.READ_ALARMS, callback, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
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
        LogModule.i(order.getOrderName() + "成功");
        if (alarms == null) {
            alarms = new ArrayList<>();
        }

        for (int i = 0; i < 16; i++) {
            BandAlarm alarm = new BandAlarm();
            alarm.type = DigitalConver.byte2Int(value[3 + i]);
            i++;
            if (value[3 + i] == 0) {
                continue;
            }
            alarm.state = DigitalConver.byte2binaryString((value[3 + i]));
            i++;
            int hour = DigitalConver.byte2Int(value[3 + i]);
            i++;
            int min = DigitalConver.byte2Int(value[3 + i]);
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, min);
            alarm.time = new SimpleDateFormat("HH:mm").format(calendar.getTime());
            alarms.add(alarm);
        }

        int group = DigitalConver.byte2Int(value[2]);
        if (group == 0)
            return;
        orderStatus = OrderTask.ORDER_STATUS_SUCCESS;
        MokoSupport.getInstance().setAlarms(alarms);

        MokoSupport.getInstance().pollTask();
        callback.onOrderResult(response);
        MokoSupport.getInstance().executeTask(callback);
    }
}
