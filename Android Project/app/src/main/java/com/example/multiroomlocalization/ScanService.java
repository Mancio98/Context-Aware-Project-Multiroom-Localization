package com.example.multiroomlocalization;


import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.util.List;

public class ScanService extends IntentService {

    public static final int REQUEST_CODE = 12345;
    WifiManager wifiManager;
    Context context;

    public ScanService(Context contextTemp) {
        super(null);
        context = contextTemp;
        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);;

        BroadcastReceiver wifiScanReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context c, Intent intent) {
                boolean success = intent.getBooleanExtra(
                        WifiManager.EXTRA_RESULTS_UPDATED, false);
                if (success) {
                    scanSuccess();
                } else {
                    // scan failure handling
                    scanFailure();
                }
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        context.registerReceiver(wifiScanReceiver, intentFilter);

    }

    void startScan(){
        boolean success = wifiManager.startScan();
        if (!success) {
            // scan failure handling
            scanFailure();
        }
    }

    private void scanSuccess() {
        List<ScanResult> results = wifiManager.getScanResults();
        for ( ScanResult res : results ) {
            System.out.println("SSID: " + res.SSID + " BSSID: " + res.BSSID+ " level: " + res.level);
        }
    }

    private void scanFailure() {
        // handle failure: new scan did NOT succeed
        // consider using old scan results: these are the OLD results!
        List<ScanResult> results = wifiManager.getScanResults();
        for ( ScanResult res : results ) {
            System.out.println("SSID: " + res.SSID + " BSSID: " + res.BSSID+ " level: " + res.level);
        }
        Toast.makeText(context, "Error ", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

    }
}