package io.relayr.android.ble;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothDevice;
import android.os.Build;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static io.relayr.android.ble.BleDeviceMode.ON_BOARDING;
import static io.relayr.android.ble.BleDeviceMode.UNKNOWN;
import static io.relayr.android.ble.BleScannerFilter.BleFilteredScanCallback;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
@RunWith(RobolectricTestRunner.class)
public class BleScannerFilterTest {

    private byte[] data = new byte[] { 0x0C, 0x09, 0x57, 0x75, 0x6E, 0x64, 0x65, 0x72, 0x62, 0x61,
            0x72, 0x49, 0x52, 0x03, 0x19, 0x00, 0x02, 0x02, 0x01, 0x06, 0x07, 0x03, 0x00, 0x20,
            0x0A, 0x18, 0x0F, 0x18 }; // WunderbarIR

    @Mock private BluetoothDevice device;

    @Before public void initialise() {
        MockitoAnnotations.initMocks(this);
        when(device.getAddress()).thenReturn("random");
    }

    @Test public void isRelevant_shouldReturnFalse_becauseDeviceIsAlreadyDiscovered() {
        String name = "bla";
        String address = "bla";
        io.relayr.android.ble.BleDeviceManager manager = new io.relayr.android.ble.BleDeviceManager();
        when(device.getAddress()).thenReturn(address);
        io.relayr.android.ble.BleDevice bleDevice = new io.relayr.android.ble.BleDevice(device, "", ON_BOARDING, mock(io.relayr.android.ble.BleDeviceManager.class));
        manager.addDiscoveredDevice(bleDevice);
        io.relayr.android.ble.BleScannerFilter filter = new io.relayr.android.ble.BleScannerFilter(manager, null);
        when(device.getAddress()).thenReturn(address);
        Assert.assertFalse(filter.isRelevant(device, name, ON_BOARDING));
    }

    @Test public void isRelevant_shouldReturnFalse_becauseIsNotKnownDevice() {
        String name = "bla";
        io.relayr.android.ble.BleDeviceManager manager = new io.relayr.android.ble.BleDeviceManager();
        io.relayr.android.ble.BleScannerFilter filter = new io.relayr.android.ble.BleScannerFilter(manager, null);
        when(device.getName()).thenReturn(name);
        Assert.assertFalse(filter.isRelevant(device, name, ON_BOARDING));
    }

    @Test public void isRelevant_shouldReturnFalse_becauseOfAnUnknownMode() {
        String name = "WunderbarHTU";
        io.relayr.android.ble.BleDeviceManager manager = new io.relayr.android.ble.BleDeviceManager();
        io.relayr.android.ble.BleScannerFilter filter = new io.relayr.android.ble.BleScannerFilter(manager, null);

        when(device.getName()).thenReturn(name);
        Assert.assertFalse(filter.isRelevant(device, name, UNKNOWN));
    }

    @Test public void isRelevant_shouldReturnTrue() {
        String name = "WunderbarHTU";
        io.relayr.android.ble.BleDeviceManager manager = new io.relayr.android.ble.BleDeviceManager();
        io.relayr.android.ble.BleScannerFilter filter = new io.relayr.android.ble.BleScannerFilter(manager, null);

        when(device.getName()).thenReturn(name);
        Assert.assertTrue(filter.isRelevant(device, name, ON_BOARDING));
    }

    @Test public void onLeScan_successful() {
        String name = "WunderbarIR";
        io.relayr.android.ble.BleDeviceManager manager = new io.relayr.android.ble.BleDeviceManager();

        BleFilteredScanCallback callback = mock(BleFilteredScanCallback.class);
        io.relayr.android.ble.BleScannerFilter filter = new io.relayr.android.ble.BleScannerFilter(manager, callback);

        when(device.getName()).thenReturn(name);

        filter.onLeScan(device, 22, data);

        verify(callback, times(1)).onLeScan(any(io.relayr.android.ble.BleDevice.class), anyInt());
    }

    @Test public void onLeScan_unsuccessfulBecauseOfAnUnknownDeviceName() {
        byte[] data = new byte[] { 0x00, 0x19, 0x37, 0x43, 0x6E, 0x64, 0x75, 0x73, 0x42, 0x53,
                0x20, 0x49, 0x52, 0x13, 0x29, 0x00, 0x33, 0x02, 0x01, 0x06, 0x17, 0x04, 0x01, 0x22,
                0x20, 0x20, 0x1F, 0x28 }; // random name
        String name = "unknown";
        io.relayr.android.ble.BleDeviceManager manager = new io.relayr.android.ble.BleDeviceManager();

        BleFilteredScanCallback callback = mock(BleFilteredScanCallback.class);
        io.relayr.android.ble.BleScannerFilter filter = new io.relayr.android.ble.BleScannerFilter(manager, callback);

        when(device.getName()).thenReturn(name);

        filter.onLeScan(device, 22, data);

        verify(callback, never()).onLeScan(any(BleDevice.class), anyInt());
    }
}
