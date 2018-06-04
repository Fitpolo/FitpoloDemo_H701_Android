package com.fitpolo.support.task;

import com.fitpolo.support.MokoSupport;
import com.fitpolo.support.callback.MokoOrderTaskCallback;
import com.fitpolo.support.entity.DailyStep;
import com.fitpolo.support.entity.OrderEnum;
import com.fitpolo.support.entity.OrderType;
import com.fitpolo.support.log.LogModule;
import com.fitpolo.support.utils.ComplexDataParse;
import com.fitpolo.support.utils.DigitalConver;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * @Date 2017/5/11
 * @Author wenzheng.liu
 * @Description 获取未同步的记步数据
 * @ClassPath com.fitpolo.support.task.LastestStepsTask
 */
public class LastestStepsTask extends OrderTask {

    private static final int ORDERDATA_LENGTH = 7;
    // 获取最新数据
    private static final int HEADER_GET_NEW_DATA = 0x2C;
    // 返回记步数据头
    private static final int RESPONSE_HEADER_STEP = 0x92;
    // 返回未同步数据头
    public static final int RESPONSE_HEADER_NEW_DATA_COUNT = 0xAA;

    private byte[] orderData;

    private int stepCount;
    private ArrayList<DailyStep> dailySteps;

    public LastestStepsTask(MokoOrderTaskCallback callback, Calendar lastSyncTime) {
        super(OrderType.WRITE, OrderEnum.getLastestSteps, callback, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
        isNewDataSuccess = false;
        orderData = new byte[ORDERDATA_LENGTH];
        int year = lastSyncTime.get(Calendar.YEAR) - 2000;
        int month = lastSyncTime.get(Calendar.MONTH) + 1;
        int day = lastSyncTime.get(Calendar.DAY_OF_MONTH);

        int hour = lastSyncTime.get(Calendar.HOUR_OF_DAY);
        int minute = lastSyncTime.get(Calendar.MINUTE);

        orderData[0] = (byte) HEADER_GET_NEW_DATA;
        orderData[1] = (byte) year;
        orderData[2] = (byte) month;
        orderData[3] = (byte) day;
        orderData[4] = (byte) hour;
        orderData[5] = (byte) minute;
        orderData[6] = (byte) RESPONSE_HEADER_STEP;
    }

    @Override
    public byte[] assemble() {
        return orderData;
    }

    @Override
    public void parseValue(byte[] value) {
        if (order.getOrderHeader() != DigitalConver.byte2Int(value[0]) && order.getOrderHeader() != DigitalConver.byte2Int(value[1])) {
            return;
        }
        LogModule.i(order.getOrderName() + "成功");
        // 获取记步总数标记成功
        isNewDataSuccess = true;
        int header = DigitalConver.byte2Int(value[0]);
        switch (header) {
            case RESPONSE_HEADER_NEW_DATA_COUNT:
                byte[] count = new byte[value.length - 2];
                System.arraycopy(value, 2, count, 0, value.length - 2);
                stepCount = DigitalConver.byteArr2Int(count);
                MokoSupport.getInstance().setDailyStepCount(stepCount);
                LogModule.i("有" + stepCount + "条记步数据");
                MokoSupport.getInstance().initStepsList();
                dailySteps = MokoSupport.getInstance().getDailySteps();
                delayTime = stepCount == 0 ? DEFAULT_DELAY_TIME : DEFAULT_DELAY_TIME + 100 * stepCount;
                // 拿到条数后再启动超时任务
                MokoSupport.getInstance().timeoutHandler(this);
                break;
            case RESPONSE_HEADER_STEP:
                if (stepCount > 0) {
                    if (dailySteps == null) {
                        dailySteps = new ArrayList<>();
                    }
                    dailySteps.add(ComplexDataParse.parseDailyStep(value, 2));
                    stepCount--;
                    MokoSupport.getInstance().setDailySteps(dailySteps);
                    MokoSupport.getInstance().setDailyStepCount(stepCount);
                    if (stepCount > 0) {
                        LogModule.i("还有" + stepCount + "条记步数据未同步");
                        return;
                    }
                }
                break;
            default:
                return;
        }
        if (stepCount != 0) {
            return;
        }
        MokoSupport.getInstance().setDailyStepCount(stepCount);
        MokoSupport.getInstance().setDailySteps(dailySteps);
        orderStatus = OrderTask.ORDER_STATUS_SUCCESS;
        MokoSupport.getInstance().pollTask();
        callback.onOrderResult(response);
        MokoSupport.getInstance().executeTask(callback);
    }

    private boolean isNewDataSuccess;
    private boolean isReceiveDetail;

    @Override
    public boolean timeoutPreTask() {
        if (!isReceiveDetail) {
            if (!isNewDataSuccess) {
                LogModule.i("获取未同步的记步个数超时");
            } else {
                isReceiveDetail = true;
                return false;
            }
        }
        return super.timeoutPreTask();
    }
}
