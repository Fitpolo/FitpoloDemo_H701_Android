package com.fitpolo.support.entity;

/**
 * @Date 2017/5/15
 * @Author wenzheng.liu
 * @Description 记步数据
 * @ClassPath com.fitpolo.support.entity.DailyStep
 */
public class DailyStep {
    public String date;// 日期，yyyy-MM-dd
    public String count;// 步数
    public String duration;// 运动时间
    public String distance;// 运动距离
    public String calories;// 运动消耗卡路里

    @Override
    public String toString() {
        return "DailyStep{" +
                "date='" + date + '\'' +
                ", count='" + count + '\'' +
                ", duration='" + duration + '\'' +
                ", distance='" + distance + '\'' +
                ", calories='" + calories + '\'' +
                '}';
    }
}
