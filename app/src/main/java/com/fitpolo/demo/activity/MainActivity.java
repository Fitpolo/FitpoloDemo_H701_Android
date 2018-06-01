package com.fitpolo.demo.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Process;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.fitpolo.demo.DemoConstant;
import com.fitpolo.demo.R;
import com.fitpolo.demo.adapter.DeviceAdapter;
import com.fitpolo.demo.service.FitpoloService;
import com.fitpolo.support.bluetooth.BluetoothModule;
import com.fitpolo.support.entity.BleDevice;
import com.fitpolo.support.utils.Utils;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;


/**
 * @Date 2017/5/11
 * @Author wenzheng.liu
 * @Description
 */

public class MainActivity extends BaseActivity implements AdapterView.OnItemClickListener {
    private static final String TAG = "MainActivity";
    @Bind(R.id.lv_device)
    ListView lvDevice;

    private ArrayList<BleDevice> mDatas;
    private DeviceAdapter mAdapter;
    private LocalBroadcastManager mBroadcastManager;
    private ProgressDialog mDialog;
    private FitpoloService mService;
    private String deviceMacAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);
        ButterKnife.bind(this);
        mBroadcastManager = LocalBroadcastManager.getInstance(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction(DemoConstant.ACTION_START_SCAN);
        filter.addAction(DemoConstant.ACTION_STOP_SCAN);
        filter.addAction(DemoConstant.ACTION_CONN_SUCCESS);
        filter.addAction(DemoConstant.ACTION_CONN_FAILURE);
        filter.addAction(DemoConstant.ACTION_DISCONNECT);
        mBroadcastManager.registerReceiver(mReceiver, filter);
        bindService(new Intent(this, FitpoloService.class), mServiceConnection, BIND_AUTO_CREATE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!isWriteStoragePermissionOpen()) {
                showRequestPermissionDialog();
                return;
            }
        }
        initContentView();
    }

    private void initContentView() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!isLocationPermissionOpen()) {
                showRequestPermissionDialog2();
                return;
            } else {
                AppOpsManager appOpsManager = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
                int checkOp = appOpsManager.checkOp(AppOpsManager.OPSTR_COARSE_LOCATION, Process.myUid(), getPackageName());
                if (checkOp != AppOpsManager.MODE_ALLOWED) {
                    showOpenSettingsDialog2();
                    return;
                }
            }
        }
        mDialog = new ProgressDialog(this);
        mDatas = new ArrayList<>();
        mAdapter = new DeviceAdapter(this);
        mAdapter.setItems(mDatas);
        lvDevice.setAdapter(mAdapter);
        lvDevice.setOnItemClickListener(this);
    }

    public void searchDevices(View view) {
        mAdapter.notifyDataSetChanged();
        mService.startScanDevice();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mDialog.setMessage("Connect...");
        mDialog.show();
        BleDevice device = (BleDevice) parent.getItemAtPosition(position);
        mService.startConnDevice(device.address);
        deviceMacAddress = device.address;
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                if (DemoConstant.ACTION_START_SCAN.equals(action)) {
                    mDialog.setMessage("Scanning...");
                    mDialog.show();
                }
                if (DemoConstant.ACTION_STOP_SCAN.equals(action)) {
                    if (!MainActivity.this.isFinishing() && mDialog.isShowing()) {
                        mDialog.dismiss();
                    }
                    mDatas = (ArrayList<BleDevice>) intent.getSerializableExtra("devices");
                    mAdapter.setItems(mDatas);
                    mAdapter.notifyDataSetChanged();
                }
                if (DemoConstant.ACTION_CONN_SUCCESS.equals(action)) {
                    if (!MainActivity.this.isFinishing() && mDialog.isShowing()) {
                        mDialog.dismiss();
                    }
                    Toast.makeText(MainActivity.this, "Connect success", Toast.LENGTH_SHORT).show();
                    Intent orderIntent = new Intent(MainActivity.this, SendOrderActivity.class);
                    orderIntent.putExtra("deviceMacAddress", deviceMacAddress);
                    startActivity(orderIntent);
                }
                if (DemoConstant.ACTION_CONN_FAILURE.equals(action)) {
                    if (BluetoothModule.getInstance().isBluetoothOpen() && BluetoothModule.getInstance().getReconnectCount() > 0) {
                        return;
                    }
                    if (!MainActivity.this.isFinishing() && mDialog.isShowing()) {
                        mDialog.dismiss();
                    }
                    Toast.makeText(MainActivity.this, "Connect failed", Toast.LENGTH_SHORT).show();
                }
                if (DemoConstant.ACTION_DISCONNECT.equals(action)) {
                    if (BluetoothModule.getInstance().isBluetoothOpen() && BluetoothModule.getInstance().getReconnectCount() > 0) {
                        return;
                    }
                    if (!MainActivity.this.isFinishing() && mDialog.isShowing()) {
                        mDialog.dismiss();
                    }
                    Toast.makeText(MainActivity.this, "Disconnected", Toast.LENGTH_SHORT).show();
                }
            }
        }
    };


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBroadcastManager.unregisterReceiver(mReceiver);
        unbindService(mServiceConnection);
        stopService(new Intent(this, FitpoloService.class));
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = ((FitpoloService.LocalBinder) service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case DemoConstant.PERMISSION_REQUEST_CODE: {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                        // 判断用户是否 点击了不再提醒。(检测该权限是否还可以申请)
                        boolean shouldShowRequest = shouldShowRequestPermissionRationale(permissions[0]);
                        if (shouldShowRequest) {
                            if (permissions[0].equals(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                                showRequestPermissionDialog2();
                            } else {
                                showRequestPermissionDialog();
                            }
                        } else {
                            if (permissions[0].equals(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                                showOpenSettingsDialog2();
                            } else {
                                showOpenSettingsDialog();
                            }
                        }
                    } else {
                        initContentView();
                    }
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == DemoConstant.REQUEST_CODE_PERMISSION) {
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!isWriteStoragePermissionOpen()) {
                    showOpenSettingsDialog();
                } else {
                    initContentView();
                }
            }
        }
        if (requestCode == DemoConstant.REQUEST_CODE_PERMISSION_2) {
            initContentView();
        }
        if (requestCode == DemoConstant.REQUEST_CODE_LOCATION_SETTINGS) {
            if (!Utils.isLocServiceEnable(this)) {
                showOpenLocationDialog();
            } else {
                initContentView();
            }
        }
    }

    private void showOpenSettingsDialog() {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle(R.string.permission_storage_close_title)
                .setMessage(R.string.permission_storage_close_content)
                .setPositiveButton(getString(R.string.permission_open), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        // 根据包名打开对应的设置界面
                        intent.setData(Uri.parse("package:" + getPackageName()));
                        startActivityForResult(intent, DemoConstant.REQUEST_CODE_PERMISSION);
                    }
                })
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                        return;
                    }
                }).create();
        dialog.show();
    }

    private void showRequestPermissionDialog() {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle(R.string.permission_storage_need_title)
                .setMessage(R.string.permission_storage_need_content)
                .setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, DemoConstant.PERMISSION_REQUEST_CODE);
                    }
                })
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                        return;
                    }
                }).create();
        dialog.show();
    }

    private void showOpenLocationDialog() {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle(R.string.location_need_title)
                .setMessage(R.string.location_need_content)
                .setPositiveButton(getString(R.string.permission_open), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(intent, DemoConstant.REQUEST_CODE_LOCATION_SETTINGS);
                    }
                })
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                        return;
                    }
                }).create();
        dialog.show();
    }

    private void showOpenSettingsDialog2() {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle(R.string.permission_location_close_title)
                .setMessage(R.string.permission_location_close_content)
                .setPositiveButton(getString(R.string.permission_open), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        // 根据包名打开对应的设置界面
                        intent.setData(Uri.parse("package:" + getPackageName()));
                        startActivityForResult(intent, DemoConstant.REQUEST_CODE_PERMISSION_2);
                    }
                })
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                        return;
                    }
                }).create();
        dialog.show();
    }

    private void showRequestPermissionDialog2() {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle(R.string.permission_location_need_title)
                .setMessage(R.string.permission_location_need_content)
                .setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, DemoConstant.PERMISSION_REQUEST_CODE);
                    }
                })
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                        return;
                    }
                }).create();
        dialog.show();
    }
}
