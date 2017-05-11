package com.fitpolo.support;

/**
 * @Date 2017/5/10
 * @Author wenzheng.liu
 * @Description 蓝牙常量
 */
public class FitConstant {
    // 扫描结束时间
    public static final long SCAN_PERIOD = 5000;
    // 连接失败错误码
    public static final int CONN_ERROR_CODE_ADDRESS_NULL = 0;// 地址为空
    public static final int CONN_ERROR_CODE_BLUTOOTH_CLOSE = 1;// 蓝牙关闭
    public static final int CONN_ERROR_CODE_CONNECTED = 2;// 已连接
    public static final int CONN_ERROR_CODE_FAILURE = 3;// 连接失败
    // 获取命令状态码
    public static final int ORDER_CODE_SUCCESS = 200;// 成功
    public static final int ORDER_CODE_ERROR_TIMEOUT = 201;// 超时

    // 命令
    public static final int HEADER_GETDATA = 22;// 获取数据
    public static final int GET_INNER_VERSION = 9;// 获取内部版本号
    public static final int HEADER_SET_SYSTEM_TIME = 17;// 设置系统时间
}
