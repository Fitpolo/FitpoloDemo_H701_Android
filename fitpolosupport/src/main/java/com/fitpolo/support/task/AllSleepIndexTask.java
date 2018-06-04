package com.fitpolo.support.task;

import com.fitpolo.support.MokoSupport;
import com.fitpolo.support.callback.MokoOrderTaskCallback;
import com.fitpolo.support.entity.DailySleep;
import com.fitpolo.support.entity.OrderEnum;
import com.fitpolo.support.entity.OrderType;
import com.fitpolo.support.log.LogModule;
import com.fitpolo.support.utils.ComplexDataParse;
import com.fitpolo.support.utils.DigitalConver;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @Date 2017/5/11
 * @Author wenzheng.liu
 * @Description 获取睡眠指数
 * @ClassPath com.fitpolo.support.task.AllSleepIndexTask
 */
public class AllSleepIndexTask extends OrderTask {

    private static final int ORDERDATA_LENGTH = 2;
    // 获取数据
    private static final int HEADER_GETDATA = 0x16;
    // 获取睡眠概况
    private static final int GET_DAILY_SLEEP_INDEX = 0x02;
    // 返回睡眠record数据头
    private static final int RESPONSE_HEADER_SLEEP_RECORD = 0x94;

    private byte[] orderData;

    private HashMap<Integer, DailySleep> sleepMap;
    private ArrayList<DailySleep> sleepList;
    private int sleepIndexCount;
    private int sleepRecordCount;

    public AllSleepIndexTask(MokoOrderTaskCallback callback) {
        super(OrderType.WRITE, OrderEnum.getAllSleepIndex, callback, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
        orderData = new byte[ORDERDATA_LENGTH];
        orderData[0] = (byte) HEADER_GETDATA;
        orderData[1] = (byte) GET_DAILY_SLEEP_INDEX;
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
        sleepIndexCount = MokoSupport.getInstance().getSleepIndexCount();
        sleepList = MokoSupport.getInstance().getDailySleeps();
        sleepMap = MokoSupport.getInstance().getSleepsMap();
        if (sleepIndexCount > 0) {
            if (sleepList == null) {
                sleepList = new ArrayList<>();
            }
            if (sleepMap == null) {
                sleepMap = new HashMap<>();
            }
            sleepList.add(ComplexDataParse.parseDailySleepIndex(value, sleepMap, 1));
            sleepIndexCount--;

            MokoSupport.getInstance().setSleepIndexCount(sleepIndexCount);
            MokoSupport.getInstance().setDailySleeps(sleepList);
            MokoSupport.getInstance().setSleepsMap(sleepMap);
            if (sleepIndexCount > 0) {
                LogModule.i("还有" + sleepIndexCount + "条睡眠index数据未同步");
                return;
            }
        }
        if (!sleepList.isEmpty()) {
            // 请求完index后请求record
            AllSleepRecordTask sleepRecordTask = new AllSleepRecordTask(callback);
            MokoSupport.getInstance().sendCustomOrder(sleepRecordTask);
        } else {
            orderStatus = OrderTask.ORDER_STATUS_SUCCESS;
            MokoSupport.getInstance().pollTask();
            callback.onOrderResult(response);
            MokoSupport.getInstance().executeTask(callback);
        }
    }

    public void parseRecordValue(byte[] value) {
        if (RESPONSE_HEADER_SLEEP_RECORD != (value[0] & 0xFF)) {
            return;
        }
        LogModule.i("获取睡眠record成功");
        sleepRecordCount = MokoSupport.getInstance().getSleepRecordCount();
        if (sleepRecordCount > 0) {
            // 处理record
            ComplexDataParse.parseDailySleepRecord(value, sleepMap, 1);
            sleepRecordCount--;
            MokoSupport.getInstance().setSleepRecordCount(sleepRecordCount);
            if (sleepRecordCount > 0) {
                LogModule.i("还有" + sleepRecordCount + "条睡眠record数据未同步");
                return;
            }
        }
        MokoSupport.getInstance().setSleepRecordCount(sleepRecordCount);
        MokoSupport.getInstance().setDailySleeps(sleepList);
        sleepMap.clear();
        MokoSupport.getInstance().setSleepsMap(sleepMap);
        orderStatus = OrderTask.ORDER_STATUS_SUCCESS;
        MokoSupport.getInstance().pollTask();
        callback.onOrderResult(response);
        MokoSupport.getInstance().executeTask(callback);
    }

}
