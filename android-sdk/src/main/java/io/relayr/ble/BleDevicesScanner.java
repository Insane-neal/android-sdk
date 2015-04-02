package io.relayr.ble;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
class BleDevicesScanner implements Runnable, BluetoothAdapter.LeScanCallback {
    private static final String TAG = BleDevicesScanner.class.getSimpleName();

    private static final long PERIOD_SCAN_ONCE = -1;

    private final BluetoothAdapter bluetoothAdapter;
    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());
    private final LeScansPoster leScansPoster;

    private long scanPeriod = PERIOD_SCAN_ONCE;
    private Thread scanThread;
    private volatile boolean isStopping = false;
    private ScheduledExecutorService stopService;

    public BleDevicesScanner(BluetoothAdapter adapter, BluetoothAdapter.LeScanCallback callback) {
        bluetoothAdapter = adapter;
        leScansPoster = new LeScansPoster(callback);
    }

    public synchronized void setScanPeriod(long scanPeriod) {
        this.scanPeriod = scanPeriod < 0 ? PERIOD_SCAN_ONCE : scanPeriod;
    }

    public boolean isScanning() {
        return scanThread != null && scanThread.isAlive();
    }

    public synchronized void start() {
        if (isScanning() || isStopping)
            return;

        if (scanThread != null) {
            scanThread.interrupt();
        }
        scanThread = new Thread(this);
        scanThread.setName(TAG);
        scanThread.start();
    }

    public synchronized void stop() {
        isStopping = true;

        if (scanThread != null) {
            scanThread.interrupt();
            scanThread = null;
        }

        bluetoothAdapter.stopLeScan(this);
        isStopping = false;
    }

    @Override
    public void run() {
        synchronized (this) {
            bluetoothAdapter.startLeScan(this);
            if (scanPeriod != PERIOD_SCAN_ONCE) {
                if (stopService != null) stopService.shutdown();
                stopService = Executors.newSingleThreadScheduledExecutor();
                stopService.schedule(new Runnable() {
                    @Override
                    public void run() {
                        stop();
                        stopService.shutdown();
                    }
                }, scanPeriod, TimeUnit.MILLISECONDS);
            }
        }
    }

    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        synchronized (leScansPoster) {
            leScansPoster.set(device, rssi, scanRecord);
            mainThreadHandler.post(leScansPoster);
        }
    }

    private static class LeScansPoster implements Runnable {
        private final BluetoothAdapter.LeScanCallback leScanCallback;

        private BluetoothDevice device;
        private int rssi;
        private byte[] scanRecord;

        private LeScansPoster(BluetoothAdapter.LeScanCallback leScanCallback) {
            this.leScanCallback = leScanCallback;
        }

        public void set(BluetoothDevice device, int rssi, byte[] scanRecord) {
            this.device = device;
            this.rssi = rssi;
            this.scanRecord = scanRecord;
        }

        @Override
        public void run() {
            leScanCallback.onLeScan(device, rssi, scanRecord);
        }
    }
}