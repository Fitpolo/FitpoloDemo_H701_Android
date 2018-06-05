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
