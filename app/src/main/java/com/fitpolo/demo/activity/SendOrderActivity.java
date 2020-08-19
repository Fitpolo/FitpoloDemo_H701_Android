package com.fitpolo.demo.activity;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.fitpolo.demo.AppConstants;
import com.fitpolo.demo.R;
import com.fitpolo.demo.service.MokoService;
import com.fitpolo.demo.utils.FileUtils;
import com.fitpolo.demo.utils.Utils;
import com.fitpolo.support.MokoConstants;
import com.fitpolo.support.MokoSupport;
import com.fitpolo.support.entity.AutoLighten;
import com.fitpolo.support.entity.BandAlarm;
import com.fitpolo.support.entity.CustomScreen;
import com.fitpolo.support.entity.DailySleep;
import com.fitpolo.support.entity.DailyStep;
import com.fitpolo.support.entity.HeartRate;
import com.fitpolo.support.entity.OrderEnum;
import com.fitpolo.support.entity.OrderTaskResponse;
import com.fitpolo.support.entity.SitAlert;
import com.fitpolo.support.entity.UserInfo;
import com.fitpolo.support.handler.UpgradeHandler;
import com.fitpolo.support.log.LogModule;
import com.fitpolo.support.task.AllAlarmTask;
import com.fitpolo.support.task.AllHeartRateTask;
import com.fitpolo.support.task.AllSleepIndexTask;
import com.fitpolo.support.task.AllStepsTask;
import com.fitpolo.support.task.AutoLightenTask;
import com.fitpolo.support.task.BatteryDailyStepsCountTask;
import com.fitpolo.support.task.FirmwareParamTask;
import com.fitpolo.support.task.FirmwareVersionTask;
import com.fitpolo.support.task.FunctionDisplayTask;
import com.fitpolo.support.task.HeartRateIntervalTask;
import com.fitpolo.support.task.InnerVersionTask;
import com.fitpolo.support.task.LastScreenTask;
import com.fitpolo.support.task.LastestHeartRateTask;
import com.fitpolo.support.task.LastestSleepIndexTask;
import com.fitpolo.support.task.LastestStepsTask;
import com.fitpolo.support.task.NotifyPhoneTask;
import com.fitpolo.support.task.NotifySmsTask;
import com.fitpolo.support.task.OrderTask;
import com.fitpolo.support.task.ReadAlarmsTask;
import com.fitpolo.support.task.ReadSettingTask;
import com.fitpolo.support.task.ReadSitAlertTask;
import com.fitpolo.support.task.ShakeBandTask;
import com.fitpolo.support.task.SitLongTimeAlertTask;
import com.fitpolo.support.task.SleepHeartCountTask;
import com.fitpolo.support.task.SystemTimeTask;
import com.fitpolo.support.task.TimeFormatTask;
import com.fitpolo.support.task.UnitTypeTask;
import com.fitpolo.support.task.UserInfoTask;

import java.util.ArrayList;
import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @Date 2017/5/11
 * @Author wenzheng.liu
 * @Description
 */

public class SendOrderActivity extends BaseActivity {
    private static final String TAG = "SendOrderActivity";

    @BindView(R.id.btn_heart_rate_interval)
    Button btnHeartRateInterval;
    @BindView(R.id.btn_lastest_steps)
    Button btnLastestSteps;
    @BindView(R.id.btn_lastest_sleeps)
    Button btnLastestSleeps;
    @BindView(R.id.btn_lastest_heart_rate)
    Button btnLastestHeartRate;
    @BindView(R.id.btn_all_heart_rate)
    Button btnAllHeartRate;
    @BindView(R.id.btn_read_all_alarms)
    Button btnReadAllAlarms;
    @BindView(R.id.btn_read_sit_alert)
    Button btnReadSitAlert;
    @BindView(R.id.btn_read_settings)
    Button btnReadSettings;
    @BindView(R.id.btn_notification)
    Button btnNotification;
    @BindView(R.id.btn_firmware_params)
    Button btnFirmwareParams;
    private MokoService mService;
    private String deviceMacAddress;
    private boolean mIsUpgrade;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.send_order_layout);
        ButterKnife.bind(this);
        deviceMacAddress = getIntent().getStringExtra("deviceMacAddress");
        bindService(new Intent(this, MokoService.class), mServiceConnection, BIND_AUTO_CREATE);
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                    int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                    switch (blueState) {
                        case BluetoothAdapter.STATE_TURNING_OFF:
                        case BluetoothAdapter.STATE_OFF:
                            SendOrderActivity.this.finish();
                            break;
                    }
                }
                if (MokoConstants.ACTION_CONN_STATUS_DISCONNECTED.equals(action)) {
                    abortBroadcast();
                    if (!mIsUpgrade) {
                        Toast.makeText(SendOrderActivity.this, "Connect failed", Toast.LENGTH_SHORT).show();
                        SendOrderActivity.this.finish();
                    }
                }
                if (MokoConstants.ACTION_ORDER_RESULT.equals(action)) {
                    OrderTaskResponse response = (OrderTaskResponse) intent.getSerializableExtra(MokoConstants.EXTRA_KEY_RESPONSE_ORDER_TASK);
                    OrderEnum orderEnum = response.order;
                    switch (orderEnum) {
                        case getInnerVersion:

                            btnAllHeartRate.setVisibility(MokoSupport.showHeartRate ? View.VISIBLE : View.GONE);
                            btnHeartRateInterval.setVisibility(MokoSupport.showHeartRate ? View.VISIBLE : View.GONE);

                            btnLastestSteps.setVisibility(MokoSupport.supportNewData ? View.VISIBLE : View.GONE);
                            btnLastestSleeps.setVisibility(MokoSupport.supportNewData ? View.VISIBLE : View.GONE);
                            btnLastestHeartRate.setVisibility(MokoSupport.showHeartRate && MokoSupport.supportNewData ? View.VISIBLE : View.GONE);

                            btnFirmwareParams.setVisibility(MokoSupport.versionCodeLast >= 28 ? View.VISIBLE : View.GONE);

                            btnReadAllAlarms.setVisibility(MokoSupport.supportNotifyAndRead ? View.VISIBLE : View.GONE);
                            btnReadSettings.setVisibility(MokoSupport.supportNotifyAndRead ? View.VISIBLE : View.GONE);
                            btnReadSitAlert.setVisibility(MokoSupport.supportNotifyAndRead ? View.VISIBLE : View.GONE);
                            btnNotification.setVisibility(MokoSupport.supportNotifyAndRead ? View.VISIBLE : View.GONE);

                            LogModule.i("Support heartRate：" + MokoSupport.showHeartRate);
                            LogModule.i("Support newData：" + MokoSupport.supportNewData);
                            LogModule.i("Support notify and read：" + MokoSupport.supportNotifyAndRead);
                            LogModule.i("Version code：" + MokoSupport.versionCode);
                            LogModule.i("Should upgrade：" + MokoSupport.canUpgrade);
                            break;
                        case setSystemTime:
                            break;
                        case setUserInfo:
                            break;
                        case setBandAlarm:
                            break;
                        case setUnitType:
                            break;
                        case setTimeFormat:
                            break;
                        case setAutoLigten:
                            break;
                        case setSitLongTimeAlert:
                            break;
                        case setLastScreen:
                            break;
                        case setHeartRateInterval:
                            break;
                        case setFunctionDisplay:
                            break;
                        case getFirmwareVersion:
                            LogModule.i("firmware version：" + MokoSupport.versionCodeShow);
                            break;
                        case getBatteryDailyStepCount:
                            LogModule.i("battery：" + MokoSupport.getInstance().getBatteryQuantity());
                            break;
                        case getSleepHeartCount:
                            break;
                        case getAllSteps:
                            ArrayList<DailyStep> steps = MokoSupport.getInstance().getDailySteps();
                            if (steps == null || steps.isEmpty()) {
                                return;
                            }
                            for (DailyStep step : steps) {
                                LogModule.i(step.toString());
                            }
                            break;
                        case getAllSleepIndex:
                            ArrayList<DailySleep> sleeps = MokoSupport.getInstance().getDailySleeps();
                            if (sleeps == null || sleeps.isEmpty()) {
                                return;
                            }
                            for (DailySleep sleep : sleeps) {
                                LogModule.i(sleep.toString());
                            }
                            break;
                        case getAllHeartRate:
                            ArrayList<HeartRate> heartRates = MokoSupport.getInstance().getHeartRates();
                            if (heartRates == null || heartRates.isEmpty()) {
                                return;
                            }
                            for (HeartRate heartRate : heartRates) {
                                LogModule.i(heartRate.toString());
                            }
                            break;
                        case getLastestSteps:
                            ArrayList<DailyStep> lastestSteps = MokoSupport.getInstance().getDailySteps();
                            if (lastestSteps == null || lastestSteps.isEmpty()) {
                                return;
                            }
                            for (DailyStep step : lastestSteps) {
                                LogModule.i(step.toString());
                            }
                            break;
                        case getLastestSleepIndex:
                            ArrayList<DailySleep> lastestSleeps = MokoSupport.getInstance().getDailySleeps();
                            if (lastestSleeps == null || lastestSleeps.isEmpty()) {
                                return;
                            }
                            for (DailySleep sleep : lastestSleeps) {
                                LogModule.i(sleep.toString());
                            }
                            break;
                        case getLastestHeartRate:
                            ArrayList<HeartRate> lastestHeartRate = MokoSupport.getInstance().getHeartRates();
                            if (lastestHeartRate == null || lastestHeartRate.isEmpty()) {
                                return;
                            }
                            for (HeartRate heartRate : lastestHeartRate) {
                                LogModule.i(heartRate.toString());
                            }
                            break;
                        case getFirmwareParam:
                            LogModule.i("Last charge time：" + MokoSupport.getInstance().getLastChargeTime());
                            LogModule.i("Product batch：" + MokoSupport.getInstance().getProductBatch());
                            break;
                        case READ_ALARMS:
                            ArrayList<BandAlarm> bandAlarms = MokoSupport.getInstance().getAlarms();
                            for (BandAlarm bandAlarm : bandAlarms) {
                                LogModule.i(bandAlarm.toString());
                            }
                            break;
                        case READ_SETTING:
                            boolean unitType = MokoSupport.getInstance().getUnitTypeBritish();
                            int timeFormat = MokoSupport.getInstance().getTimeFormat();
                            CustomScreen customScreen = MokoSupport.getInstance().getCustomScreen();
                            boolean lastScreen = MokoSupport.getInstance().getLastScreen();
                            int interval = MokoSupport.getInstance().getHeartRateInterval();
                            AutoLighten autoLighten = MokoSupport.getInstance().getAutoLighten();
                            LogModule.i("Unit type:" + unitType);
                            LogModule.i("Time format:" + timeFormat);
                            LogModule.i("Function display:" + customScreen.toString());
                            LogModule.i("Last screen:" + lastScreen);
                            LogModule.i("HeartRate interval:" + interval);
                            LogModule.i("Auto light:" + autoLighten.toString());

                            break;
                        case READ_SIT_ALERT:
                            SitAlert sitAlert = MokoSupport.getInstance().getSitAlert();
                            LogModule.i("Sit alert:" + sitAlert.toString());
                            break;
                    }

                }
                if (MokoConstants.ACTION_ORDER_TIMEOUT.equals(action)) {
                    Toast.makeText(SendOrderActivity.this, "Timeout", Toast.LENGTH_SHORT).show();
                }
                if (MokoConstants.ACTION_ORDER_FINISH.equals(action)) {
                    Toast.makeText(SendOrderActivity.this, "Success", Toast.LENGTH_SHORT).show();
                }
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
        unbindService(mServiceConnection);
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = ((MokoService.LocalBinder) service).getService();
            // 注册广播接收器
            IntentFilter filter = new IntentFilter();
            filter.addAction(MokoConstants.ACTION_CONN_STATUS_DISCONNECTED);
            filter.addAction(MokoConstants.ACTION_DISCOVER_TIMEOUT);
            filter.addAction(MokoConstants.ACTION_ORDER_RESULT);
            filter.addAction(MokoConstants.ACTION_ORDER_TIMEOUT);
            filter.addAction(MokoConstants.ACTION_ORDER_FINISH);
            filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
            filter.setPriority(200);
            registerReceiver(mReceiver, filter);
            // first
            MokoSupport.getInstance().sendOrder(new InnerVersionTask(mService));
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };


    public void getInnerVersion(View view) {
        MokoSupport.getInstance().sendOrder(new InnerVersionTask(mService));
    }

    public void setSystemTime(View view) {
        MokoSupport.getInstance().sendOrder(new SystemTimeTask(mService));
    }

    public void setUserInfo(View view) {
        UserInfo userInfo = new UserInfo();
        userInfo.age = 23;
        userInfo.gender = 0;
        userInfo.height = 170;
        userInfo.weight = 80;
        userInfo.stepExtent = (int) Math.floor(userInfo.height * 0.45);
        MokoSupport.getInstance().sendOrder(new UserInfoTask(mService, userInfo));
    }

    public void setAllAlarms(View view) {
        MokoSupport.getInstance().sendOrder(new AllAlarmTask(mService, new ArrayList<BandAlarm>()));
    }

    public void setUnitType(View view) {
        MokoSupport.getInstance().sendOrder(new UnitTypeTask(mService, 0));
    }

    public void setTimeFormat(View view) {
        MokoSupport.getInstance().sendOrder(new TimeFormatTask(mService, 0));
    }

    public void setAutoLigten(View view) {
        AutoLighten autoLighten = new AutoLighten();
        autoLighten.autoLighten = 0;
        MokoSupport.getInstance().sendOrder(new AutoLightenTask(mService, autoLighten.autoLighten));
    }

    public void setSitAlert(View view) {
        SitAlert alert = new SitAlert();
        alert.alertSwitch = 0;
        alert.startTime = "11:00";
        alert.endTime = "18:00";
        MokoSupport.getInstance().sendOrder(new SitLongTimeAlertTask(mService, alert));
    }

    public void setLastScreen(View view) {
        MokoSupport.getInstance().sendOrder(new LastScreenTask(mService, 1));
    }

    public void setHeartRateInterval(View view) {
        MokoSupport.getInstance().sendOrder(new HeartRateIntervalTask(mService, 3));
    }

    public void setFunctionDisplay(View view) {
        CustomScreen customScreen = new CustomScreen(true, true, true, true, true);
        MokoSupport.getInstance().sendOrder(new FunctionDisplayTask(mService, customScreen));
    }

    public void getFirmwareVersion(View view) {
        MokoSupport.getInstance().sendOrder(new FirmwareVersionTask(mService));
    }

    public void getBatteryDailyStepCount(View view) {
        MokoSupport.getInstance().sendOrder(new BatteryDailyStepsCountTask(mService));
    }

    public void getSleepHeartCount(View view) {
        MokoSupport.getInstance().sendOrder(new SleepHeartCountTask(mService));
    }

    public void getAllSteps(View view) {
        if (MokoSupport.getInstance().getDailyStepCount() == 0) {
            Toast.makeText(this, "Get step count first", Toast.LENGTH_SHORT).show();
            return;
        }
        MokoSupport.getInstance().sendOrder(new AllStepsTask(mService));
    }

    public void getAllSleeps(View view) {
        if (MokoSupport.getInstance().getSleepIndexCount() == 0) {
            Toast.makeText(this, "Get sleep count first", Toast.LENGTH_SHORT).show();
            return;
        }
        MokoSupport.getInstance().sendOrder(new AllSleepIndexTask(mService));
    }

    public void getAllHeartRate(View view) {
        if (MokoSupport.getInstance().getHeartRateCount() == 0) {
            Toast.makeText(this, "Get heartrate count first", Toast.LENGTH_SHORT).show();
            return;
        }
        MokoSupport.getInstance().sendOrder(new AllHeartRateTask(mService));
    }

    public void sendMultiOrders(View view) {
        SystemTimeTask systemTimeTask = new SystemTimeTask(mService);
        LastScreenTask lastShowTask = new LastScreenTask(mService, 0);
        AutoLightenTask autoLightenTask = new AutoLightenTask(mService, 1);
        FirmwareVersionTask firmwareVersionTask = new FirmwareVersionTask(mService);
        MokoSupport.getInstance().sendOrder(systemTimeTask, lastShowTask, autoLightenTask, firmwareVersionTask);
    }

    public void shakeBand(View view) {
        MokoSupport.getInstance().sendDirectOrder(new ShakeBandTask(mService));
    }

    public void setPhoneNotify(View view) {
        OrderTask shakeBandTask = new NotifyPhoneTask(mService, "1234567", true);
        MokoSupport.getInstance().sendDirectOrder(shakeBandTask);
    }

    public void setSmsNotify(View view) {
        OrderTask shakeBandTask = new NotifySmsTask(mService, "abcdef", false);
        MokoSupport.getInstance().sendDirectOrder(shakeBandTask);
    }

    public void getLastestSteps(View view) {
        Calendar lastSyncTime = Utils.strDate2Calendar("2018-06-01 00:00", AppConstants.PATTERN_YYYY_MM_DD_HH_MM);
        OrderTask stepsTask = new LastestStepsTask(mService, lastSyncTime);
        MokoSupport.getInstance().sendOrder(stepsTask);
    }

    public void getLastestSleeps(View view) {
        Calendar lastSyncTime = Utils.strDate2Calendar("2018-06-01 00:00", AppConstants.PATTERN_YYYY_MM_DD_HH_MM);
        OrderTask sleepGeneral = new LastestSleepIndexTask(mService, lastSyncTime);
        MokoSupport.getInstance().sendOrder(sleepGeneral);
    }

    public void getLastestHeartRate(View view) {
        Calendar lastSyncTime = Utils.strDate2Calendar("2018-06-01 00:00", AppConstants.PATTERN_YYYY_MM_DD_HH_MM);
        OrderTask heartRateTask = new LastestHeartRateTask(mService, lastSyncTime);
        MokoSupport.getInstance().sendOrder(heartRateTask);
    }


    public void getFirmwareParams(View view) {
        MokoSupport.getInstance().sendOrder(new FirmwareParamTask(mService));
    }


    public void readAllAlarms(View view) {
        MokoSupport.getInstance().sendOrder(new ReadAlarmsTask(mService));
    }

    public void readSitAlert(View view) {
        MokoSupport.getInstance().sendOrder(new ReadSitAlertTask(mService));
    }

    public void readSettings(View view) {
        MokoSupport.getInstance().sendOrder(new ReadSettingTask(mService));
    }

    public void notification(View view) {
        startActivity(new Intent(this, MessageNotificationActivity.class));
    }

    private static final int REQUEST_CODE_FILE = 2;

    public void upgradeFirmware(View view) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");//设置类型，我这里是任意类型，任意后缀的可以这样写。
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
            startActivityForResult(Intent.createChooser(intent, "select file first!"), REQUEST_CODE_FILE);
        } catch (ActivityNotFoundException ex) {
            Toast.makeText(this, "install file manager app", Toast.LENGTH_SHORT).show();
        }
    }

    private ProgressDialog mDialog;

    private void upgrade(String firmwarePath) {
        mIsUpgrade = true;
        if (!isFinishing()) {
            mDialog = ProgressDialog.show(this, null, "upgrade...", false, false);
        }
        UpgradeHandler upgradeHandler = new UpgradeHandler(this);
        upgradeHandler.setFilePath(firmwarePath, deviceMacAddress, new UpgradeHandler.IUpgradeCallback() {
            @Override
            public void onUpgradeError(int errorCode) {
                if (mDialog != null && mDialog.isShowing() && !isFinishing()) {
                    mDialog.dismiss();
                }
                switch (errorCode) {
                    case UpgradeHandler.EXCEPTION_FILEPATH_IS_NULL:
                        Toast.makeText(SendOrderActivity.this, "file is not exist！", Toast.LENGTH_SHORT).show();
                        break;
                    case UpgradeHandler.EXCEPTION_DEVICE_MAC_ADDRESS_IS_NULL:
                        Toast.makeText(SendOrderActivity.this, "mac address is null！", Toast.LENGTH_SHORT).show();
                        break;
                    case UpgradeHandler.EXCEPTION_UPGRADE_FAILURE:
                        Toast.makeText(SendOrderActivity.this, "upgrade failed！", Toast.LENGTH_SHORT).show();
                        back();
                        break;
                }
                mIsUpgrade = false;
            }

            @Override
            public void onProgress(int progress) {
                if (mDialog != null && mDialog.isShowing() && !isFinishing()) {
                    mDialog.setMessage("upgrade progress:" + progress + "%");
                }
            }

            @Override
            public void onUpgradeDone() {
                if (mDialog != null && mDialog.isShowing() && !isFinishing()) {
                    mDialog.dismiss();
                }
                Toast.makeText(SendOrderActivity.this, "upgrade success", Toast.LENGTH_SHORT).show();
                SendOrderActivity.this.finish();
            }
        });
    }

    private void back() {
        if (MokoSupport.getInstance().isConnDevice(this, deviceMacAddress)) {
           mService.disConnectBle();
        }
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_FILE:
                    Uri uri = data.getData();
                    String path = FileUtils.getPath(this, uri);
                    upgrade(path);
                    break;
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            back();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }
}
