package com.fitpolo.demo.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.fitpolo.demo.DemoConstant;
import com.fitpolo.demo.R;
import com.fitpolo.demo.adapter.DeviceAdapter;
import com.fitpolo.demo.service.FitpoloService;
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
        BleDevice device = (BleDevice) parent.getItemAtPosition(position);
        mService.startConnDevice(device.address);
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                if (DemoConstant.ACTION_START_SCAN.equals(action)) {
                    mDialog.setMessage("正在扫描设备...");
                    mDialog.show();
                }
                if (DemoConstant.ACTION_STOP_SCAN.equals(action)) {
                    mDialog.dismiss();
                    mDatas = (ArrayList<BleDevice>) intent.getSerializableExtra("devices");
                    mAdapter.setItems(mDatas);
                    mAdapter.notifyDataSetChanged();
                }
                if (DemoConstant.ACTION_CONN_SUCCESS.equals(action)) {
                    Toast.makeText(MainActivity.this, "连接成功", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(MainActivity.this, SendOrderActivity.class));
                }
                if (DemoConstant.ACTION_CONN_FAILURE.equals(action)) {
                    Toast.makeText(MainActivity.this, "连接失败", Toast.LENGTH_SHORT).show();
                }
                if (DemoConstant.ACTION_DISCONNECT.equals(action)) {
                    Toast.makeText(MainActivity.this, "断开连接", Toast.LENGTH_SHORT).show();
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
