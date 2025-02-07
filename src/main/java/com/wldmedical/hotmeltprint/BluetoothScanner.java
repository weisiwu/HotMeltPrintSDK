package com.wldmedical.hotmeltprint;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BluetoothScanner {

    private static final String TAG = "BluetoothScanner";
    private BluetoothAdapter bluetoothAdapter;
    private Context context;
    private boolean isScanning = false;
    private String foundMacAddress = null;

    public BluetoothScanner(Context context) {
        this.context = context;
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Log.e(TAG, "设备不支持蓝牙");
            // 在实际应用中，这里应该进行相应的错误处理，例如提示用户设备不支持蓝牙
        }
    }

    public String scanForGPDevice() {
        if (bluetoothAdapter == null) {
            return null; // 蓝牙适配器不存在
        }

        if (!bluetoothAdapter.isEnabled()) {
            Log.w(TAG, "蓝牙未启用，请先启用蓝牙");
            return null; // 蓝牙未启用
        }

        // 检查已配对设备
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                if (matchesGPRegex(device.getName())) {
                    foundMacAddress = device.getAddress();
                    Log.i(TAG, "已配对设备中找到匹配设备: " + foundMacAddress);
                    return foundMacAddress;
                }
            }
        }

        // 开始扫描
        if (!isScanning) {
            foundMacAddress = null; // 重置找到的MAC地址
            isScanning = true;

            // 注册广播接收器
            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            context.registerReceiver(discoveryReceiver, filter);

            bluetoothAdapter.startDiscovery();
            Log.i(TAG, "开始扫描...");
            return null; // 扫描中，稍后通过广播接收器返回结果
        } else {
            Log.w(TAG, "已经在扫描中");
            return null;
        }
    }

    public void stopScan() {
        if (bluetoothAdapter != null && bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
            context.unregisterReceiver(discoveryReceiver);
            isScanning = false;
            Log.i(TAG, "停止扫描");
        }
    }

    private boolean matchesGPRegex(String deviceName) {
        if (deviceName == null || deviceName.isEmpty()) {
            return false;
        }
        Pattern pattern = Pattern.compile("^GP-.*"); // 正则表达式：以 GP- 开头
        Matcher matcher = pattern.matcher(deviceName);
        return matcher.matches();
    }

    private final BroadcastReceiver discoveryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (matchesGPRegex(device.getName())) {
                    foundMacAddress = device.getAddress();
                    Log.i(TAG, "找到匹配设备: " + foundMacAddress);
                    stopScan(); // 找到第一个匹配设备后停止扫描
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Log.i(TAG, "扫描完成");
                isScanning = false;
                context.unregisterReceiver(discoveryReceiver); // 扫描完成后取消注册
                // 在这里可以发送一个广播或回调，通知扫描完成，即使没有找到匹配的设备
            }
        }
    };

    public String getFoundMacAddress(){
        return foundMacAddress;
    }
}