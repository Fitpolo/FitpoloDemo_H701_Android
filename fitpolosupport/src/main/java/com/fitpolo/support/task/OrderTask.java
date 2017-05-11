package com.fitpolo.support.task;

import com.fitpolo.support.OrderEnum;
import com.fitpolo.support.callback.OrderCallback;
import com.fitpolo.support.entity.BaseResponse;

/**
 * @Date 2017/5/11
 * @Author wenzheng.liu
 * @Description 发送命令任务
 * @ClassPath com.fitpolo.support.task.OrderTask
 */
public abstract class OrderTask {
    private OrderEnum order;
    private OrderCallback callback;
    private BaseResponse response;

    public BaseResponse getResponse() {
        return response;
    }

    public void setResponse(BaseResponse response) {
        this.response = response;
    }


    public abstract byte[] assemble();


    public OrderCallback getCallback() {
        return callback;
    }

    public void setCallback(OrderCallback callback) {
        this.callback = callback;
    }


    public OrderEnum getOrder() {
        return order;
    }

    public void setOrder(OrderEnum order) {
        this.order = order;
    }
}
