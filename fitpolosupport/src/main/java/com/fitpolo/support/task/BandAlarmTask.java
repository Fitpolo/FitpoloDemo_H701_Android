package com.fitpolo.support.task;

import com.fitpolo.support.FitConstant;
import com.fitpolo.support.OrderEnum;
import com.fitpolo.support.callback.OrderCallback;
import com.fitpolo.support.entity.BaseResponse;
import com.fitpolo.support.entity.req.BandAlarm;
import com.fitpolo.support.log.LogModule;
import com.fitpolo.support.utils.DigitalConver;

import java.util.List;

/**
 * @Date 2017/5/11
 * @Author wenzheng.liu
 * @Description 设置手环闹钟
 * @ClassPath com.fitpolo.support.task.BandAlarmTask
 */
public class BandAlarmTask extends OrderTask {
    private List<BandAlarm> alarms;
    private boolean isAlarmFinish;

    public BandAlarmTask(OrderCallback callback, List<BandAlarm> alarms) {
        setOrder(OrderEnum.setBandAlarm);
        setCallback(callback);
        setResponse(new BaseResponse());
        this.alarms = alarms;
        isAlarmFinish = false;
    }

    @Override
    public byte[] assemble(Object... objects) {
        byte[] byteArray = new byte[18];
        byteArray[0] = (byte) FitConstant.HEADER_SET_BANDALARM;
        if (!isAlarmFinish) {
            // 第一组
            byteArray[1] = 0x00;
            for (int i = 0; i < 4; i++) {
                if (alarms.size() > i) {
                    BandAlarm alarm = alarms.get(i);
                    if (alarm != null) {
                        LogModule.i(alarm.toString());
                    }
                    byteArray[i * 4 + 2] = Byte.valueOf(Integer.toHexString(alarm.type), 16);
                    byteArray[i * 4 + 3] = (byte) Integer.parseInt(DigitalConver.binaryString2hexString(alarm.state), 16);
                    byteArray[i * 4 + 4] = Byte.valueOf(Integer.toHexString(Integer.valueOf(alarm.time.split(":")[0])), 16);
                    byteArray[i * 4 + 5] = Byte.valueOf(Integer.toHexString(Integer.valueOf(alarm.time.split(":")[1])), 16);
                } else {
                    byteArray[i * 4 + 2] = 3;
                    byteArray[i * 4 + 3] = 0;
                    byteArray[i * 4 + 4] = 0;
                    byteArray[i * 4 + 5] = 0;
                }
            }
        } else {
            // 第二组
            byteArray[1] = 0x01;
            for (int i = 0; i < 4; i++) {
                int index = i + 4;
                if (alarms.size() > index) {
                    BandAlarm alarm = alarms.get(index);
                    if (alarm != null) {
                        LogModule.i(alarm.toString());
                    }
                    byteArray[i * 4 + 2] = Byte.valueOf(Integer.toHexString(alarm.type), 16);
                    byteArray[i * 4 + 3] = (byte) Integer.parseInt(DigitalConver.binaryString2hexString(alarm.state), 16);
                    byteArray[i * 4 + 4] = Byte.valueOf(Integer.toHexString(Integer.valueOf(alarm.time.split(":")[0])), 16);
                    byteArray[i * 4 + 5] = Byte.valueOf(Integer.toHexString(Integer.valueOf(alarm.time.split(":")[1])), 16);
                } else {
                    byteArray[i * 4 + 2] = 3;
                    byteArray[i * 4 + 3] = 0;
                    byteArray[i * 4 + 4] = 0;
                    byteArray[i * 4 + 5] = 0;
                }
            }
        }
        return byteArray;
    }

    public boolean isAlarmFinish() {
        return isAlarmFinish;
    }

    public void setAlarmFinish(boolean alarmFinish) {
        isAlarmFinish = alarmFinish;
    }
}
