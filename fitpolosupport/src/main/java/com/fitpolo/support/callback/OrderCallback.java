package com.fitpolo.support.callback;

import com.fitpolo.support.OrderEnum;
import com.fitpolo.support.entity.BaseResponse;

/**
 * @Date 2017/5/10
 * @Author wenzheng.liu
 * @Description 返回数据回调类
 * @ClassPath com.fitpolo.support.callback.OrderCallback
 */
public interface OrderCallback {

    void onOrderResult(OrderEnum order, BaseResponse response);

    void onOrderTimeout(OrderEnum order);

    void onOrderFinish();
}
