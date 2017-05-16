package com.fitpolo.support.utils;

import com.fitpolo.support.log.LogModule;

/**
 * @Date 2017/5/15
 * @Author wenzheng.liu
 * @Description 数字转换类
 * @ClassPath com.fitpolo.support.utils.DigitalConver
 */
public class DigitalConver {
    /**
     * @Date 2017/5/10
     * @Author wenzheng.liu
     * @Description 格式化数据
     */
    public static String[] formatData(byte[] data) {
        if (data != null && data.length > 0) {
            StringBuilder stringBuilder = new StringBuilder(data.length);
            for (byte byteChar : data)
                stringBuilder.append(byte2HexString(byteChar));
            LogModule.i(stringBuilder.toString());
            String[] datas = stringBuilder.toString().split(" ");
            return datas;
        }
        return null;
    }

    /**
     * @Date 2017/5/10
     * @Author wenzheng.liu
     * @Description byte转16进制
     */
    public static String byte2HexString(byte b) {
        return String.format("%02X ", b);
    }

    /**
     * @Date 2017/5/15
     * @Author wenzheng.liu
     * @Description 16进制转10进制
     */
    public static String decodeToString(String data) {
        String string = Integer.toString(Integer.parseInt(data, 16));
        return string;
    }

    /**
     * @Date 2017/5/16
     * @Author wenzheng.liu
     * @Description 16进制转2进制
     */
    public static String hexString2binaryString(String hexString) {
        if (hexString == null || hexString.length() % 2 != 0)
            return null;
        String bString = "", tmp;
        for (int i = 0; i < hexString.length(); i++) {
            tmp = "0000"
                    + Integer.toBinaryString(Integer.parseInt(
                    hexString.substring(i, i + 1), 16));
            bString += tmp.substring(tmp.length() - 4);
        }
        return bString;
    }
}
