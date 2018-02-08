package com.fitpolo.demo.activity;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;


/**
 * @Date 2017/5/11
 * @Author wenzheng.liu
 * @Description
 */

public class MainActivity extends Activity implements AdapterView.OnItemClickListener {
    private static final String TAG = "MainActivity";
    @Bind(R.id.lv_device)
    ListView lvDevice;

    private ArrayList<BleDevice> mDatas;
    private DeviceAdapter mAdapter;
    private LocalBroadcastManager mBroadcastManager;
    private ProgressDialog mDialog;
    private FitpoloService mService;
    private static final int PERMISSION_REQUEST_CODE = 1;
    private String deviceMacAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this, new String[]{
                                Manifest.permission.ACCESS_COARSE_LOCATION
                                , Manifest.permission.WRITE_EXTERNAL_STORAGE}
                        , PERMISSION_REQUEST_CODE);
                return;
            }
        }
        initContentView();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE: {
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(MainActivity.this, "This app needs these permissions!", Toast.LENGTH_SHORT).show();
                        MainActivity.this.finish();
                        return;
                    }
                }
                initContentView();
            }
        }
    }

    private void initContentView() {
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
}
