package com.fitpolo.support.task;

import com.fitpolo.support.FitConstant;
import com.fitpolo.support.OrderEnum;
import com.fitpolo.support.callback.OrderCallback;
import com.fitpolo.support.entity.BaseResponse;

/**
 * @Date 2017/5/11
 * @Author wenzheng.liu
 * @Description 设置功能显示
 * @ClassPath com.fitpolo.support.task.FunctionDisplayTask
 */
public class FunctionDisplayTask extends OrderTask {
    private boolean[] functions;
    // functions[0]：是否显示运动时长；
    // functions[1]：是否显示运动消耗卡路里；
    // functions[2]：是否显示运动运动距离；
    // functions[3]：是否显示心率；
    // functions[4]：是否显示步数；

    public FunctionDisplayTask(OrderCallback callback, boolean[] functions) {
        setOrder(OrderEnum.setFunctionDisplay);
        setCallback(callback);
        setResponse(new BaseResponse());
        this.functions = functions;
    }

    @Override
    public byte[] assemble(Object... objects) {
        byte[] byteArray = new byte[6];
        byteArray[0] = (byte) FitConstant.HEADER_GETDATA;
        byteArray[1] = (byte) FitConstant.GET_SET_FUNCTION_DISPLAY;
        byteArray[2] = (byte) 255;
        byteArray[3] = (byte) 255;
        byteArray[4] = (byte) 255;
        boolean duration = functions[0];
        boolean calorie = functions[1];
        boolean distance = functions[2];
        boolean heartrate = functions[3];
        boolean step = functions[4];
        StringBuilder sb = new StringBuilder("00");
        sb.append(duration ? "1" : "0");
        sb.append(calorie ? "1" : "0");
        sb.append(distance ? "1" : "0");
        sb.append(heartrate ? "1" : "0");
        sb.append(step ? "1" : "0");
        sb.append("1");
        byteArray[5] = (byte) Integer.parseInt(sb.toString(), 2);
        return byteArray;
    }
}
