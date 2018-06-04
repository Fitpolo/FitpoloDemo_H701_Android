package com.fitpolo.support.task;

import com.fitpolo.support.MokoSupport;
import com.fitpolo.support.callback.MokoOrderTaskCallback;
import com.fitpolo.support.entity.CustomScreen;
import com.fitpolo.support.entity.OrderEnum;
import com.fitpolo.support.entity.OrderType;
import com.fitpolo.support.log.LogModule;
import com.fitpolo.support.utils.DigitalConver;

/**
 * @Date 2017/5/11
 * @Author wenzheng.liu
 * @Description 设置功能显示
 * @ClassPath com.fitpolo.support.task.FunctionDisplayTask
 */
public class FunctionDisplayTask extends OrderTask {
    private static final int ORDERDATA_LENGTH = 6;
    // 获取数据
    private static final int HEADER_GETDATA = 0x16;
    // 设置功能显示
    private static final int GET_SET_FUNCTION_DISPLAY = 0x19;

    private byte[] orderData;

    public FunctionDisplayTask(MokoOrderTaskCallback callback, CustomScreen functions) {
        super(OrderType.WRITE, OrderEnum.setFunctionDisplay, callback, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
        orderData = new byte[ORDERDATA_LENGTH];
        orderData[0] = (byte) HEADER_GETDATA;
        orderData[1] = (byte) GET_SET_FUNCTION_DISPLAY;
        orderData[2] = (byte) 255;
        orderData[3] = (byte) 255;
        boolean duration = functions.duration;
        boolean calorie = functions.calorie;
        boolean distance = functions.distance;
        boolean heartrate = functions.heartrate;
        boolean step = functions.step;
        orderData[4] = (byte) 255;
        StringBuilder sb = new StringBuilder("00");
        sb.append(duration ? "1" : "0");
        sb.append(calorie ? "1" : "0");
        sb.append(distance ? "1" : "0");
        sb.append(heartrate ? "1" : "0");
        sb.append(step ? "1" : "0");
        sb.append("1");
        orderData[5] = (byte) Integer.parseInt(sb.toString(), 2);
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
        orderStatus = OrderTask.ORDER_STATUS_SUCCESS;

        MokoSupport.getInstance().pollTask();
        callback.onOrderResult(response);
        MokoSupport.getInstance().executeTask(callback);
    }
}
