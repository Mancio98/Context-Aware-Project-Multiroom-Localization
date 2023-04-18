package com.example.multiroomlocalization;


import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.support.v4.os.IResultReceiver;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.io.Serializable;
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
                    try {
                        scanSuccess();
                    } catch (RemoteException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    // scan failure handling
                    scanFailure();
                }
            }
        };

        //registerReceiver(wifiScanReceiver);

    }

    public void registerReceiver(BroadcastReceiver wifiScanReceiver){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        context.registerReceiver(wifiScanReceiver, intentFilter);
    }

    public void startScan(){
        boolean success = wifiManager.startScan();
        if (!success) {
            // scan failure handling
            scanFailure();
        }
    }

    private void scanSuccess() throws RemoteException {
        List<ScanResult> results = wifiManager.getScanResults();
        for ( ScanResult res : results ) {
            System.out.println("SSID: " + res.SSID + " BSSID: " + res.BSSID+ " level: " + res.level);
        }
        System.out.println("FINE SCAN");
    }

    private void scanFailure() {
        // handle failure: new scan did NOT succeed
        // consider using old scan results: these are the OLD results!
        List<ScanResult> results = wifiManager.getScanResults();
        for ( ScanResult res : results ) {
            System.out.println("ERRORE SSID: " + res.SSID + " BSSID: " + res.BSSID+ " level: " + res.level);
        }
        Toast.makeText(context, "Error ", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

    }

    public WifiManager getWifiManager(){ return wifiManager;}

}