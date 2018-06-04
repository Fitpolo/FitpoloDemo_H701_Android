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

/**
 * @Date 2017/5/11
 * @Author wenzheng.liu
 * @Description 获取记步数据
 * @ClassPath com.fitpolo.support.task.AllStepsTask
 */
public class AllStepsTask extends OrderTask {
    private static final int ORDERDATA_LENGTH = 2;
    // 获取数据
    private static final int HEADER_GETDATA = 0x16;
    // 获取记步数据
    private static final int GET_DAILY_STEPS = 0x01;

    private byte[] orderData;

    private int stepCount;
    private ArrayList<DailyStep> dailySteps;

    public AllStepsTask(MokoOrderTaskCallback callback) {
        super(OrderType.WRITE, OrderEnum.getAllSteps, callback, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
        orderData = new byte[ORDERDATA_LENGTH];
        orderData[0] = (byte) HEADER_GETDATA;
        orderData[1] = (byte) GET_DAILY_STEPS;
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
        stepCount = MokoSupport.getInstance().getDailyStepCount();
        dailySteps = MokoSupport.getInstance().getDailySteps();
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
        MokoSupport.getInstance().setDailyStepCount(stepCount);
        MokoSupport.getInstance().setDailySteps(dailySteps);
        orderStatus = OrderTask.ORDER_STATUS_SUCCESS;
        MokoSupport.getInstance().pollTask();
        callback.onOrderResult(response);
        MokoSupport.getInstance().executeTask(callback);
    }
}
