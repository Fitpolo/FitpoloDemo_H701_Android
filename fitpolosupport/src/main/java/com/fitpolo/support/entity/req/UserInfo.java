package com.fitpolo.support.entity.req;

/**
 * @Date 2017/5/14 0014
 * @Author wenzheng.liu
 * @Description 个人信息
 * @ClassPath com.fitpolo.support.entity.req.UserInfo
 */
public class UserInfo {
    public int weight;// 体重
    public int height;// 身高
    public int age;// 年龄
    public int gender;// 性别 男：0；女：1

    @Override
    public String toString() {
        return "UserInfo{" +
                "weight=" + weight +
                ", height=" + height +
                ", age=" + age +
                ", gender=" + gender +
                '}';
    }
}
