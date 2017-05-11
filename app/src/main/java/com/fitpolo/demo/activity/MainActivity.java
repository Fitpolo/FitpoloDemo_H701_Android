package com.fitpolo.demo.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.fitpolo.demo.R;
import com.fitpolo.demo.adapter.DeviceAdapter;
import com.fitpolo.support.bluetooth.BluetoothModule;
import com.fitpolo.support.callback.ConnStateCallback;
import com.fitpolo.support.callback.ScanDeviceCallback;
import com.fitpolo.support.entity.BleDevice;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.Bind;
import butterknife.ButterKnife;


/**
 * @Date 2017/5/11
 * @Author wenzheng.liu
 * @Description
 */

public class MainActivity extends Activity implements AdapterView.OnItemClickListener, ScanDeviceCallback {
    private static final String TAG = "MainActivity";
    @Bind(R.id.lv_device)
    ListView lvDevice;
    @Bind(R.id.btn_search)
    Button btnSearch;

    private HashMap<String, BleDevice> mMap;
    private ArrayList<BleDevice> mDatas;
    private DeviceAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);
        ButterKnife.bind(this);
        mMap = new HashMap<>();
        mDatas = new ArrayList<>();
        mAdapter = new DeviceAdapter(this);
        mAdapter.setItems(mDatas);
        lvDevice.setAdapter(mAdapter);
        lvDevice.setOnItemClickListener(this);
    }

    public void searchDevices(View view) {
        mDatas.clear();
        mAdapter.notifyDataSetChanged();
        BluetoothModule.getInstance().startScanDevice(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        BleDevice device = (BleDevice) parent.getItemAtPosition(position);
        BluetoothModule.getInstance().createBluetoothGatt(this, device.address, new ConnStateCallback() {
            @Override
            public void onConnSuccess() {
                startActivity(new Intent(MainActivity.this, SendOrderActivity.class));
            }

            @Override
            public void onConnFailure(int errorCode) {
            }

            @Override
            public void onDisconnect() {
            }
        });
    }

    @Override
    public void onStartScan() {
        Log.i(TAG, "onStartScan: ");
        btnSearch.setEnabled(false);
    }

    @Override
    public void onScanDevice(BleDevice device) {
        mMap.put(device.name, device);
    }

    @Override
    public void onStopScan() {
        Log.i(TAG, "onStopScan: ");
        btnSearch.setEnabled(true);
        mDatas.addAll(mMap.values());
        mAdapter.notifyDataSetChanged();
    }
}
