package com.fitpolo.support;

import java.io.Serializable;

/**
 * @Date 2017/5/11
 * @Author wenzheng.liu
 * @Description 命令枚举
 * @ClassPath com.fitpolo.support.OrderEnum
 */
public enum OrderEnum implements Serializable {
    // 获取内部版本号
    getInnerVersion,
    // 设置系统时间
    setSystemTime,
    // 设置个人数据
    setUserInfo,
    // 设置闹钟
    setBandAlarm,
    // 设置单位制式
    setUnitType,
    // 设置显示时间格式
    setTimeFormat,
    // 设置自动点亮屏幕
    setAutoLigten,
    // 设置久坐提醒
    setSitLongTimeAlert,
    // 设置上次显示
    setLastShow,
    // 设置心率监测间隔
    setHeartRateInterval,
    // 设置功能显示
    setFunctionDisplay,
    // 设置手环震动
    setShakeBand,
    // 清除手环数据
    clearBandData,
    // 断开手环
    disconnectBand,
    // 设置来电震动
    setPhoneComingShake,
    // 设置短信震动
    setSmsComingShake,
    // 获取固件版本号
    getFirmwareVersion,
    // 获取电量和记步总数
    getBatteryDailyStepCount,
    // 获取睡眠和心率总数
    getSleepHeartCount,
    // 获取记步数据
    getDailySteps,
    // 获取睡眠index
    getDailySleepIndex,
    // 获取睡眠record
    getDailySleepRecord,
    // 获取心率
    getHeartRate,
    // 获取今天的记步，睡眠，心率数据
    getTodayData,
    // 获取未同步的记步数据
    getNewDailySteps,
    // 获取未同步的睡眠记录数据
    getNewDailySleepIndex,
    // 获取未同步的睡眠详情数据
    getNewDailySleepRecord,
    // 获取未同步的心率数据
    getNewHeartRate,
    // 获取CRC校验结果
    getCRCVerifyResult,
    // 获取手环硬件参数
    getFirmwareParam
}
