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
    public static final int GET_BATTERY_DAILY_STEP_COUNT = 0;// 获取电量和记步总数
    public static final int GET_DAILY_STEPS = 1;// 获取记步数据
    public static final int GET_DAILY_SLEEP_INDEX = 2;// 获取睡眠数据
    public static final int GET_DAILY_SLEEP_RECORD = 3;// 获取睡眠记录
    public static final int GET_FIRMWARE_VERSION = 6;// 获取固件版本号
    public static final int GET_INNER_VERSION = 9;// 获取内部版本号
    public static final int GET_SLEEP_HEART_COUNT = 18;// 获取睡眠和心率总数
    public static final int GET_TODAY_DATA = 19;// 获取今天的数据
    public static final int GET_SET_HEART_RATE_INTERVAL = 23;// 设置心率间隔
    public static final int GET_HEART_RATE = 24;// 获取心率数据
    public static final int GET_SET_FUNCTION_DISPLAY = 25;// 设置功能显示
    public static final int HEADER_SET_SYSTEM_TIME = 17;// 设置系统时间
    public static final int HEADER_SET_USERINFO = 18;// 设置个人信息
    public static final int HEADER_CLEAR_BAND_DATA = 21;// 清除数据
    public static final int HEADER_SET_SHAKE_BAND = 23;// 设置手环震动
    public static final int HEADER_SET_BANDALARM = 38;// 设置手环闹钟
    public static final int HEADER_SET_UNIT_TYPE = 35;// 单位制式
    public static final int HEADER_SET_TIME_FORMAT = 36;// 时间格式
    public static final int HEADER_SET_AUTO_LIGHTEN = 37;// 自动亮屏幕
    public static final int HEADER_SET_SIT_LONG_TIME_ALERT = 42;// 久坐提醒
    public static final int HEADER_SET_LAST_SHOW = 39;// 上次显示
    public static final int RESPONSE_HEADER_STEP = 146;// 返回记步数据头
    public static final int RESPONSE_HEADER_SLEEP_INDEX = 147;// 返回睡眠index数据头
    public static final int RESPONSE_HEADER_SLEEP_RECORD = 148;// 返回睡眠record数据头
    public static final int RESPONSE_HEADER_HEART_RATE = 168;// 返回心率数据头

}
