package com.fitpolo.support.utils;

import android.app.ActivityManager;
import android.content.Context;

import java.util.List;

/**
 * @Date 2017/12/5 0005
 * @Author wenzheng.liu
 * @Description fitpolo工具类
 * @ClassPath com.fitpolo.support.utils.Utils
 */
public class Utils {
    /**
     * @Date 2017/12/5
     * @Author wenzheng.liu
     * @Description 获取顶部Activity
     */
    public static String getTopActivity(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> runningTaskInfos = manager.getRunningTasks(1);
        if (runningTaskInfos != null) {
            return (runningTaskInfos.get(0).topActivity.getShortClassName()).toString();
        } else
            return null;
    }
}
