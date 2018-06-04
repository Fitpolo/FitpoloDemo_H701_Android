package com.fitpolo.support.task;

import com.fitpolo.support.MokoSupport;
import com.fitpolo.support.callback.MokoOrderTaskCallback;
import com.fitpolo.support.entity.OrderEnum;
import com.fitpolo.support.entity.OrderType;
import com.fitpolo.support.entity.UserInfo;
import com.fitpolo.support.log.LogModule;
import com.fitpolo.support.utils.DigitalConver;

/**
 * @Date 2017/5/11
 * @Author wenzheng.liu
 * @Description 设置个人信息
 * @ClassPath com.fitpolo.support.task.UserInfoTask
 */
public class UserInfoTask extends OrderTask {
    private static final int ORDERDATA_LENGTH = 6;
    // 设置个人信息
    private static final int HEADER_SET_USERINFO = 0x12;

    private byte[] orderData;

    public UserInfoTask(MokoOrderTaskCallback callback, UserInfo userInfo) {
        super(OrderType.WRITE, OrderEnum.setUserInfo, callback, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
        orderData = new byte[ORDERDATA_LENGTH];
        orderData[0] = (byte) HEADER_SET_USERINFO;
        orderData[1] = (byte) userInfo.weight;
        orderData[2] = (byte) userInfo.height;
        orderData[3] = (byte) userInfo.age;
        orderData[4] = (byte) userInfo.gender;
        orderData[5] = (byte) userInfo.stepExtent;
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
