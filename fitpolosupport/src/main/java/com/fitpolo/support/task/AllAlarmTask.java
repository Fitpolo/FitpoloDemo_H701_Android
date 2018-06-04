package com.fitpolo.support.task;

import com.fitpolo.support.MokoSupport;
import com.fitpolo.support.callback.MokoOrderTaskCallback;
import com.fitpolo.support.entity.OrderEnum;
import com.fitpolo.support.entity.OrderType;
import com.fitpolo.support.entity.BandAlarm;
import com.fitpolo.support.log.LogModule;
import com.fitpolo.support.utils.DigitalConver;

import java.util.List;

/**
 * @Date 2017/5/11
 * @Author wenzheng.liu
 * @Description 设置手环闹钟
 * @ClassPath com.fitpolo.support.task.AllAlarmTask
 */
public class AllAlarmTask extends OrderTask {
    private static final int ORDERDATA_LENGTH = 18;
    // 设置手环闹钟
    private static final int HEADER_SET_BANDALARM = 0x26;

    private byte[] orderData;
    private List<BandAlarm> alarms;
    private boolean isAlarmFinish;

    public AllAlarmTask(MokoOrderTaskCallback callback, List<BandAlarm> alarms) {
        super(OrderType.WRITE, OrderEnum.setBandAlarm, callback, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
        this.alarms = alarms;
        isAlarmFinish = false;
        orderData = new byte[ORDERDATA_LENGTH];
    }

    @Override
    public byte[] assemble() {
        orderData[0] = (byte) HEADER_SET_BANDALARM;
        if (!isAlarmFinish) {
            // 第一组
            orderData[1] = 0x00;
            for (int i = 0; i < 4; i++) {
                if (alarms.size() > i) {
                    BandAlarm alarm = alarms.get(i);
                    if (alarm != null) {
                        LogModule.i(alarm.toString());
                    }
                    orderData[i * 4 + 2] = Byte.valueOf(Integer.toHexString(alarm.type), 16);
                    orderData[i * 4 + 3] = (byte) Integer.parseInt(DigitalConver.binaryString2hexString(alarm.state), 16);
                    orderData[i * 4 + 4] = Byte.valueOf(Integer.toHexString(Integer.valueOf(alarm.time.split(":")[0])), 16);
                    orderData[i * 4 + 5] = Byte.valueOf(Integer.toHexString(Integer.valueOf(alarm.time.split(":")[1])), 16);
                } else {
                    orderData[i * 4 + 2] = 3;
                    orderData[i * 4 + 3] = 0;
                    orderData[i * 4 + 4] = 0;
                    orderData[i * 4 + 5] = 0;
                }
            }
        } else {
            // 第二组
            orderData[1] = 0x01;
            for (int i = 0; i < 4; i++) {
                int index = i + 4;
                if (alarms.size() > index) {
                    BandAlarm alarm = alarms.get(index);
                    if (alarm != null) {
                        LogModule.i(alarm.toString());
                    }
                    orderData[i * 4 + 2] = Byte.valueOf(Integer.toHexString(alarm.type), 16);
                    orderData[i * 4 + 3] = (byte) Integer.parseInt(DigitalConver.binaryString2hexString(alarm.state), 16);
                    orderData[i * 4 + 4] = Byte.valueOf(Integer.toHexString(Integer.valueOf(alarm.time.split(":")[0])), 16);
                    orderData[i * 4 + 5] = Byte.valueOf(Integer.toHexString(Integer.valueOf(alarm.time.split(":")[1])), 16);
                } else {
                    orderData[i * 4 + 2] = 3;
                    orderData[i * 4 + 3] = 0;
                    orderData[i * 4 + 4] = 0;
                    orderData[i * 4 + 5] = 0;
                }
            }
        }
        return orderData;
    }

    @Override
    public void parseValue(byte[] value) {
        if (order.getOrderHeader() != DigitalConver.byte2Int(value[1])) {
            return;
        }
        LogModule.i(order.getOrderName() + "成功");
        orderStatus = OrderTask.ORDER_STATUS_SUCCESS;

        if (!isAlarmFinish) {
            isAlarmFinish = true;
        } else {
            MokoSupport.getInstance().pollTask();
            callback.onOrderResult(response);
            isAlarmFinish = false;
        }
        MokoSupport.getInstance().executeTask(callback);
    }
}
