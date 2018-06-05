package com.fitpolo.support.task;

import com.fitpolo.support.MokoSupport;
import com.fitpolo.support.callback.MokoOrderTaskCallback;
import com.fitpolo.support.entity.HeartRate;
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
 * @Description 获取心率数据
 * @ClassPath com.fitpolo.support.task.AllHeartRateTask
 */
public class AllHeartRateTask extends OrderTask {
    private static final int ORDERDATA_LENGTH = 2;
    // 获取数据
    private static final int HEADER_GETDATA = 0x16;
    // 获取心率数据
    private static final int GET_HEART_RATE = 0x18;

    private byte[] orderData;

    private HashMap<Integer, Boolean> heartRatesMap;
    private int heartRateCount;
    private ArrayList<HeartRate> heartRates;

    public AllHeartRateTask(MokoOrderTaskCallback callback) {
        super(OrderType.WRITE, OrderEnum.getAllHeartRate, callback, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
        orderData = new byte[ORDERDATA_LENGTH];
        orderData[0] = (byte) HEADER_GETDATA;
        orderData[1] = (byte) GET_HEART_RATE;
    }

    @Override
    public byte[] assemble() {
        return orderData;
    }

    @Override
    public void parseValue(byte[] value) {
        if (order.getOrderHeader() != DigitalConver.byte2Int(value[1]) && 0xA8 != DigitalConver.byte2Int(value[0])) {
            return;
        }
        LogModule.i(order.getOrderName() + "成功");
        heartRateCount = MokoSupport.getInstance().getHeartRateCount();
        heartRates = MokoSupport.getInstance().getHeartRates();
        heartRatesMap = MokoSupport.getInstance().getHeartRatesMap();
        if (heartRateCount > 0) {
            if (heartRates == null) {
                heartRates = new ArrayList<>();
            }
            if (heartRatesMap == null) {
                heartRatesMap = new HashMap<>();
            }
            if (value.length <= 2 || orderStatus == OrderTask.ORDER_STATUS_SUCCESS)
                return;
            heartRatesMap.put(heartRateCount, true);
            ComplexDataParse.parseHeartRate(value, heartRates);
            heartRateCount--;

            MokoSupport.getInstance().setHeartRatesCount(heartRateCount);
            MokoSupport.getInstance().setHeartRates(heartRates);
            if (heartRateCount > 0) {
                LogModule.i("还有" + heartRateCount + "条心率数据未同步");
                heartRatesMap.put(heartRateCount, false);
                MokoSupport.getInstance().setHeartRatesMap(heartRatesMap);
                orderTimeoutHandler(heartRateCount);
                return;
            }
        }
        MokoSupport.getInstance().setHeartRatesCount(heartRateCount);
        MokoSupport.getInstance().setHeartRates(heartRates);
        MokoSupport.getInstance().setHeartRatesMap(heartRatesMap);
        orderStatus = OrderTask.ORDER_STATUS_SUCCESS;
        MokoSupport.getInstance().pollTask();
        callback.onOrderResult(response);
        MokoSupport.getInstance().executeTask(callback);
    }

    private void orderTimeoutHandler(final int heartRateCount) {
        MokoSupport.getInstance().getHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!heartRatesMap.get(heartRateCount)) {
                    orderStatus = OrderTask.ORDER_STATUS_SUCCESS;
                    LogModule.i("获取心率第" + heartRateCount + "个数据超时");
                    MokoSupport.getInstance().pollTask();
                    callback.onOrderTimeout(response);
                    MokoSupport.getInstance().executeTask(callback);
                }
            }
        }, delayTime);
    }

    @Override
    public boolean timeoutPreTask() {
        int heartRateCount = MokoSupport.getInstance().getHeartRateCount();
        if (!MokoSupport.getInstance().getHeartRatesMap().get(heartRateCount)) {
            orderStatus = OrderTask.ORDER_STATUS_SUCCESS;
            LogModule.i("获取心率第" + heartRateCount + "个数据超时");
        } else {
            return false;
        }
        return super.timeoutPreTask();
    }
}
