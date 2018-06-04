package com.fitpolo.support.entity;

import java.io.Serializable;

/**
 * @Date 2017/5/11
 * @Author wenzheng.liu
 * @Description 命令枚举
 * @ClassPath com.fitpolo.support.entity.OrderEnum
 */
public enum OrderEnum implements Serializable {
    getInnerVersion("获取内部版本", 0x09),
    setSystemTime("设置手环时间", 0x11),
    setUserInfo("设置用户信息", 0x12),
    setBandAlarm("设置闹钟数据", 0x26),
    setUnitType("设置单位类型", 0x23),
    setTimeFormat("设置显示时间格式", 0x24),
    setAutoLigten("设置翻腕自动亮屏", 0x25),
    setSitLongTimeAlert("设置久坐提醒", 0x2A),
    setLastScreen("设置最后显示", 0x27),
    setHeartRateInterval("设置心率时间间隔", 0x17),
    setFunctionDisplay("设置功能显示", 0x19),
    getFirmwareVersion("获取固件版本", 0x90),
    getBatteryDailyStepCount("获取电量和记步总数", 0x91),
    getSleepHeartCount("获取睡眠和心率总数", 0x12),
    getAllSteps("获取记步数据", 0x92),
    getAllSleepIndex("获取睡眠index", 0x93),
    getAllSleepRecord("获取睡眠record", 0x94),
    getAllHeartRate("获取心率数据", 0x18),
    getLastestSteps("获取未同步的记步数据", 0x92),
    getLastestSleepIndex("获取未同步的睡眠记录数据", 0x93),
    getLastestSleepRecord("获取未同步的睡眠详情数据", 0x94),
    getLastestHeartRate("获取未同步的心率数据", 0xA8),
    getCRCVerifyResult("CRC校验", 0x28),
    getFirmwareParam("获取硬件参数", 0xA5),
    setShakeBand("设置手环震动", 0),
    setPhoneComingShake("设置来电震动", 0),
    setSmsComingShake("设置短信震动", 0),
    setFacebookNotify("设置facebook震动", 0),
    setQQNotify("设置QQ震动", 0),
    setSkypeNotify("设置Skype震动", 0),
    setTwitterNotify("设置Twitter震动", 0),
    setWhatsAppNotify("设置WhatsApp震动", 0),
    setWechatNotify("设置微信震动", 0),
    setSnapchatNotify("设置Snapchat震动", 0),
    setLineNotify("设置Line震动", 0),
    openNotify("打开设备通知", 0),

    READ_ALARMS("读取闹钟", 0x01),
    READ_SIT_ALERT("读取久坐提醒", 0x02),
    READ_SETTING("读取设置参数", 0x04),

    READ_NOTIFY("打开读取通知", 0),
    WRITE_NOTIFY("打开设置通知", 0),
    STEP_NOTIFY("打开记步通知", 0),
    HEART_RATE_NOTIFY("打开心率通知", 0),

    Z_READ_ALARMS("读取闹钟", 0x01),
    Z_READ_SIT_ALERT("读取久坐提醒", 0x04),
    Z_READ_STEP_TARGET("读取记步目标", 0x06),
    Z_READ_UNIT_TYPE("读取单位类型", 0x07),
    Z_READ_TIME_FORMAT("读取时间格式", 0x08),
    Z_READ_CUSTOM_SCREEN("读取自定义屏幕", 0x09),
    Z_READ_LAST_SCREEN("读取显示上次屏幕", 0x0A),
    Z_READ_HEART_RATE_INTERVAL("读取心率间隔", 0x0B),
    Z_READ_AUTO_LIGHTEN("读取翻腕亮屏", 0x0D),
    Z_READ_USER_INFO("读取个人信息", 0x0E),
    Z_READ_PARAMS("读取硬件参数", 0x10),
    Z_READ_VERSION("读取版本", 0x11),
    Z_READ_SLEEP_GENERAL("读取睡眠概况", 0x12),
    Z_READ_SLEEP_DETAIL("读取睡眠详情", 0x14),
    Z_READ_LAST_CHARGE_TIME("读取上次充电时间", 0x18),
    Z_READ_BATTERY("读取电量", 0x19),
    Z_READ_DIAL("读取表盘", 0x0F),
    Z_READ_NODISTURB("读取勿扰模式", 0x0C),

    Z_WRITE_ALARMS("设置闹钟", 0x01),
    Z_WRITE_SIT_ALERT("设置久坐提醒", 0x04),
    Z_WRITE_STEP_TARGET("设置记步目标", 0x06),
    Z_WRITE_UNIT_TYPE("设置单位类型", 0x07),
    Z_WRITE_TIME_FORMAT("设置时间格式", 0x08),
    Z_WRITE_CUSTOM_SCREEN("设置自定义屏幕", 0x09),
    Z_WRITE_LAST_SCREEN("设置显示上次屏幕", 0x0A),
    Z_WRITE_HEART_RATE_INTERVAL("设置心率间隔", 0x0B),
    Z_WRITE_AUTO_LIGHTEN("设置翻腕亮屏", 0x0D),
    Z_WRITE_USER_INFO("设置个人信息", 0x0E),
    Z_WRITE_SYSTEM_TIME("设置系统时间", 0x0F),
    Z_WRITE_SHAKE("设置手环震动", 0x13),
    Z_WRITE_NOTIFY("设置通知", 0x14),
    Z_WRITE_DIAL("设置表盘", 0x10),
    Z_WRITE_NODISTURB("设置勿扰模式", 0x0C),

    Z_READ_STEPS("读取记步", 0x01),
    Z_STEPS_CHANGES_LISTENER("监听记步", 0x03),
    Z_READ_HEART_RATE("读取心率", 0x01),
    Z_MEASURE_HEART_RATE("测量心率", 0x03),
    ;


    private String orderName;
    private int orderHeader;

    OrderEnum(String orderName, int orderHeader) {
        this.orderName = orderName;
        this.orderHeader = orderHeader;
    }

    public int getOrderHeader() {
        return orderHeader;
    }

    public String getOrderName() {
        return orderName;
    }
}
