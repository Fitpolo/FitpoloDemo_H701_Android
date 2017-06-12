package com.fitpolo.support.task;

import com.fitpolo.support.FitConstant;
import com.fitpolo.support.OrderEnum;
import com.fitpolo.support.callback.OrderCallback;
import com.fitpolo.support.entity.BaseResponse;
import com.fitpolo.support.entity.req.UserInfo;
import com.fitpolo.support.log.LogModule;

/**
 * @Date 2017/5/11
 * @Author wenzheng.liu
 * @Description 设置个人信息
 * @ClassPath com.fitpolo.support.task.UserInfoTask
 */
public class UserInfoTask extends OrderTask {
    private UserInfo userInfo;

    public UserInfoTask(OrderCallback callback, UserInfo userInfo) {
        setOrder(OrderEnum.setUserInfo);
        setCallback(callback);
        setResponse(new BaseResponse());
        this.userInfo = userInfo;
    }

    @Override
    public byte[] assemble(Object... objects) {
        if (userInfo != null) {
            LogModule.i(userInfo.toString());
        }
        byte[] byteArray = new byte[6];
        int weight = userInfo != null && userInfo.weight > 30 ? userInfo.weight : 30;
        int height = userInfo != null && userInfo.height > 100 ? userInfo.height : 100;
        int age = userInfo != null && userInfo.age > 5 ? userInfo.age : 5;
        int gender = userInfo != null ? userInfo.gender : 0;
        int stride = (int) Math.floor(height * 0.45);
        LogModule.i("步幅：" + stride);
        byteArray[0] = (byte) FitConstant.HEADER_SET_USERINFO;
        byteArray[1] = (byte) weight;
        byteArray[2] = (byte) height;
        byteArray[3] = (byte) age;
        byteArray[4] = (byte) gender;
        byteArray[5] = (byte) stride;
        return byteArray;
    }
}
