package com.fitpolo.support.task;

import com.fitpolo.support.MokoSupport;
import com.fitpolo.support.callback.MokoOrderTaskCallback;
import com.fitpolo.support.entity.OrderEnum;
import com.fitpolo.support.entity.OrderType;
import com.fitpolo.support.log.LogModule;
import com.fitpolo.support.utils.DigitalConver;

/**
 * @Date 2017/5/11
 * @Author wenzheng.liu
 * @Description 获取电量和记步总数
 * @ClassPath com.fitpolo.support.task.BatteryDailyStepsCountTask
 */
public class BatteryDailyStepsCountTask extends OrderTask {
    private static final int ORDERDATA_LENGTH = 2;
    // 获取数据
    private static final int HEADER_GETDATA = 0x16;
    // 获取电量和记步总数
    private static final int GET_BATTERY_DAILY_STEP_COUNT = 0x00;

    private byte[] orderData;

    public BatteryDailyStepsCountTask(MokoOrderTaskCallback callback) {
        super(OrderType.WRITE, OrderEnum.getBatteryDailyStepCount, callback, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
        orderData = new byte[ORDERDATA_LENGTH];
        orderData[0] = (byte) HEADER_GETDATA;
        orderData[1] = (byte) GET_BATTERY_DAILY_STEP_COUNT;
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
        int dailyStepCount = DigitalConver.byte2Int(value[1]);
        MokoSupport.getInstance().setDailyStepCount(dailyStepCount);
        LogModule.i("有" + dailyStepCount + "条记步数据");
        MokoSupport.getInstance().initStepsList();
        int batteryQuantity = DigitalConver.byte2Int(value[3]);
        MokoSupport.getInstance().setBatteryQuantity(batteryQuantity);
        LogModule.i("电池电量：" + batteryQuantity);
        MokoSupport.getInstance().pollTask();
        callback.onOrderResult(response);
        MokoSupport.getInstance().executeTask(callback);
    }
}
