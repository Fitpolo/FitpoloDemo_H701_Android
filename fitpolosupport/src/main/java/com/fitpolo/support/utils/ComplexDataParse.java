package com.fitpolo.support.utils;

import com.fitpolo.support.entity.DailySleep;
import com.fitpolo.support.entity.DailyStep;
import com.fitpolo.support.entity.HeartRate;
import com.fitpolo.support.log.LogModule;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

/**
 * @Date 2017/5/15
 * @Author wenzheng.liu
 * @Description 复杂数据解析类
 * @ClassPath com.fitpolo.support.DigitalConver.ComplexDataParse
 */
public class ComplexDataParse {
    public static DailyStep parseDailyStep(String[] formatDatas) {
        String year = formatDatas[2];
        String month = formatDatas[3];
        String day = formatDatas[4];
        Calendar calendar = Calendar.getInstance();
        calendar.set(2000 + Integer.parseInt(DigitalConver.decodeToString(year)),
                Integer.parseInt(DigitalConver.decodeToString(month)) - 1,
                Integer.parseInt(DigitalConver.decodeToString(day)));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date = calendar.getTime();
        // 步数
        String step3 = formatDatas[5];
        String step2 = formatDatas[6];
        String step1 = formatDatas[7];
        String step0 = formatDatas[8];
        StringBuilder sb = new StringBuilder();
        sb.append(step3).append(step2).append(step1).append(step0);
        // 时长
        String duration1 = formatDatas[9];
        String duration0 = formatDatas[10];
        // 距离
        String distance1 = formatDatas[11];
        String distance0 = formatDatas[12];
        // 卡路里
        String calories1 = formatDatas[13];
        String calories0 = formatDatas[14];
        String dateStr = sdf.format(date);

        String count = DigitalConver.decodeToString(sb.toString());
        String duration = DigitalConver.decodeToString(duration1 + duration0);
        String distance = new DecimalFormat().format(Integer.parseInt(DigitalConver
                .decodeToString(distance1 + distance0)) * 0.1);
        String calories = DigitalConver.decodeToString(calories1 + calories0);

        DailyStep dailyStep = new DailyStep();
        dailyStep.date = dateStr;
        dailyStep.count = count;
        dailyStep.duration = duration;
        dailyStep.distance = distance;
        dailyStep.calories = calories;
        LogModule.i(dailyStep.toString());
        return dailyStep;
    }

    public static DailySleep parseDailySleepIndex(String[] formatDatas, HashMap<Integer, DailySleep> sleepsMap) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Calendar calendar = Calendar.getInstance();
        // 起始时间
        String startYear = formatDatas[2];
        String startMonth = formatDatas[3];
        String startDay = formatDatas[4];
        String startHour = formatDatas[5];
        String startMin = formatDatas[6];
        calendar.set(2000 + Integer.parseInt(DigitalConver.decodeToString(startYear)),
                Integer.parseInt(DigitalConver.decodeToString(startMonth)) - 1,
                Integer.parseInt(DigitalConver.decodeToString(startDay)),
                Integer.parseInt(DigitalConver.decodeToString(startHour)),
                Integer.parseInt(DigitalConver.decodeToString(startMin)));
        Date startDate = calendar.getTime();
        String startDateStr = sdf.format(startDate);
        // 结束时间
        String endYear = formatDatas[7];
        String endMonth = formatDatas[8];
        String endDay = formatDatas[9];
        String endHour = formatDatas[10];
        String endMin = formatDatas[11];
        calendar.set(2000 + Integer.parseInt(DigitalConver.decodeToString(endYear)),
                Integer.parseInt(DigitalConver.decodeToString(endMonth)) - 1,
                Integer.parseInt(DigitalConver.decodeToString(endDay)),
                Integer.parseInt(DigitalConver.decodeToString(endHour)),
                Integer.parseInt(DigitalConver.decodeToString(endMin)));
        Date endDate = calendar.getTime();
        String endDateStr = sdf.format(endDate);
        // 深睡
        String deep1 = formatDatas[12];
        String deep0 = formatDatas[13];
        String deep = DigitalConver.decodeToString(deep1 + deep0);
        // 浅睡
        String light1 = formatDatas[14];
        String light0 = formatDatas[15];
        String light = DigitalConver.decodeToString(light1 + light0);
        // 清醒
        String awake1 = formatDatas[16];
        String awake0 = formatDatas[17];
        String awake = DigitalConver.decodeToString(awake1 + awake0);

        // 记录睡眠日期
        String date = new SimpleDateFormat("yyy-MM-dd").format(endDate);

        // 构造睡眠数据
        DailySleep dailySleep = new DailySleep();
        dailySleep.date = date;
        dailySleep.startTime = startDateStr;
        dailySleep.endTime = endDateStr;
        dailySleep.deepDuration = deep;
        dailySleep.lightDuration = light;
        dailySleep.awakeDuration = awake;
        LogModule.i(dailySleep.toString());
        // 暂存睡眠数据，以index为key，以实例为value，方便更新record;
        sleepsMap.put(Integer.valueOf(DigitalConver.decodeToString(formatDatas[1])), dailySleep);
        return dailySleep;
    }

    public static void parseDailySleepRecord(String[] formatDatas, HashMap<Integer, DailySleep> mSleepsMap) {
        DailySleep dailySleep = mSleepsMap.get(Integer.valueOf(DigitalConver.decodeToString(formatDatas[1])));
        if (dailySleep != null) {
            int len = Integer.valueOf(DigitalConver.decodeToString(formatDatas[3]));
            if (dailySleep.records == null) {
                dailySleep.records = new String[]{};
            }
            int sourceLen = dailySleep.records.length;
            for (int i = 0; i < len && 4 + i < formatDatas.length; i++) {
                String hex = formatDatas[4 + i];
                // 转换为二进制
                String binary = DigitalConver.hexString2binaryString(hex);
                for (int j = binary.length(); j > 0; ) {
                    j -= 2;
                    String status = binary.substring(j, j + 2);
                    dailySleep.records[sourceLen] = status;
                    sourceLen++;
                }
            }
            LogModule.i(dailySleep.toString());
        }
    }

    public static void parseHeartRate(String[] formatDatas, ArrayList<HeartRate> heartRates) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Calendar calendar = Calendar.getInstance();
        for (int i = 0; i < 3; i++) {
            String year = formatDatas[i * 6 + 2];
            String month = formatDatas[i * 6 + 3];
            String day = formatDatas[i * 6 + 4];
            String hour = formatDatas[i * 6 + 5];
            String min = formatDatas[i * 6 + 6];
            String value = formatDatas[i * 6 + 7];
            if (Integer.parseInt(DigitalConver.decodeToString(year)) == 0) {
                continue;
            }
            calendar.set(2000 + Integer.parseInt(DigitalConver.decodeToString(year)),
                    Integer.parseInt(DigitalConver.decodeToString(month)) - 1,
                    Integer.parseInt(DigitalConver.decodeToString(day)),
                    Integer.parseInt(DigitalConver.decodeToString(hour)),
                    Integer.parseInt(DigitalConver.decodeToString(min)));
            Date time = calendar.getTime();
            String heartRateTime = sdf.format(time);
            String heartRateValue = DigitalConver.decodeToString(value);
            HeartRate heartRate = new HeartRate();
            heartRate.time = heartRateTime;
            heartRate.value = heartRateValue;
            heartRates.add(heartRate);
        }
    }
}
