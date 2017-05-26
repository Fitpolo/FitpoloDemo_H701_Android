package com.fitpolo.support.task;

import com.fitpolo.support.FitConstant;
import com.fitpolo.support.OrderEnum;
import com.fitpolo.support.callback.OrderCallback;
import com.fitpolo.support.entity.BaseResponse;

/**
 * @Date 2017/5/11
 * @Author wenzheng.liu
 * @Description 获取未同步的睡眠详情数据
 * @ClassPath com.fitpolo.support.task.NewDailySleepRecordTask
 */
public class NewDailySleepRecordTask extends OrderTask {
    private String lastSyncTime;// yyyy-MM-dd HH:mm

    public NewDailySleepRecordTask(OrderCallback callback, String lastSyncTime) {
        setOrder(OrderEnum.getNewDailySleepRecord);
        setCallback(callback);
        setResponse(new BaseResponse());
        this.lastSyncTime = lastSyncTime;
    }

    @Override
    public byte[] assemble(Object... objects) {
        String[] dataTime = lastSyncTime.split(" ");
        String[] data = dataTime[0].split("-");
        int year = Integer.parseInt(data[0]) - 2000;
        int month = Integer.parseInt(data[1]);
        int day = Integer.parseInt(data[2]);

        String[] time = dataTime[1].split(":");
        int hour = Integer.parseInt(time[0]);
        int minute = Integer.parseInt(time[1]);

        byte[] byteArray = new byte[7];
        byteArray[0] = (byte) FitConstant.HEADER_GET_NEW_DATA;
        byteArray[1] = (byte) year;
        byteArray[2] = (byte) month;
        byteArray[3] = (byte) day;
        byteArray[4] = (byte) hour;
        byteArray[5] = (byte) minute;
        byteArray[6] = (byte) FitConstant.RESPONSE_HEADER_SLEEP_RECORD;
        return byteArray;
    }
}
