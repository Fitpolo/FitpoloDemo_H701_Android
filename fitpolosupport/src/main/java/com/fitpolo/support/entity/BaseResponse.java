package com.fitpolo.support.entity;

import java.io.Serializable;

/**
 * @Date 2017/5/9
 * @Author wenzheng.liu
 * @Description 反馈数据基类
 * @ClassPath com.fitpolo.support.entity.BaseResponse
 */
public class BaseResponse implements Serializable{
    public int code;
    public String msg;
}
