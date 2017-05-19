package com.fitpolo.demo;

import android.app.Application;
import android.content.Intent;

import com.fitpolo.demo.service.FitpoloService;
import com.fitpolo.support.Fitpolo;

/**
 * @Date 2017/5/11
 * @Author wenzheng.liu
 * @Description
 */

public class BaseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // 初始化
        Fitpolo.init(this);
        startService(new Intent(this, FitpoloService.class));
    }
}
