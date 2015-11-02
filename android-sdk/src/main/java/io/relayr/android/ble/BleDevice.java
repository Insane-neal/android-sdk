package io.relayr.android.ble;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothDevice;
import android.os.Build;

import io.relayr.android.RelayrSdk;
import io.relayr.android.ble.service.BaseService;
import io.relayr.android.ble.service.DirectConnectionService;
import io.relayr.android.ble.service.MasterModuleService;
import io.relayr.android.ble.service.OnBoardingService;
import io.relayr.android.ble.service.OnBoardingV2Service;
import io.relayr.java.ble.BleDeviceType;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;

import static io.relayr.android.ble.BleDeviceMode.DIRECT_CONNECTION;
import static io.relayr.android.ble.BleDeviceMode.NEW_ON_BOARDING;
import static io.relayr.android.ble.BleDeviceMode.ON_BOARDING;

/**
 * A class representing a relayr BLE Device
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BleDevice {

    private final BleDeviceMode mode;
    private final BleDeviceType type;
    private final String address;
    private final String name;
    private final Observable<? extends BaseService> serviceObservable;
    private final BleDeviceManager mDeviceManager;
    private int rssi;

    BleDevice(BluetoothDevice bluetoothDevice, String name, BleDeviceMode mode, BleDeviceManager manager) {
        this(bluetoothDevice, name, mode, manager, 0);
    }

    BleDevice(BluetoothDevice bluetoothDevice, String name, BleDeviceMode mode, BleDeviceManager manager, int rssi) {
        this.mode = mode;
        this.type = BleDeviceType.getDeviceType(bluetoothDevice.getName());
        this.address = bluetoothDevice.getAddress();
        this.name = name;
        this.rssi = rssi;
        mDeviceManager = manager;
        serviceObservable =
                mode == ON_BOARDING ?
                        OnBoardingService.connect(this, bluetoothDevice).cache() :
                        mode == DIRECT_CONNECTION ?
                                DirectConnectionService.connect(this, bluetoothDevice).cache() :
                                mode == NEW_ON_BOARDING ?
                                        OnBoardingV2Service.connect(this, bluetoothDevice).cache() :
                                        MasterModuleService.connect(this, bluetoothDevice).cache();
    }

    /**
     * The name of the Device
     * @return a string containing the name of the device.
     */
    public String getName() {
        return name;
    }

    /**
     * The Id of the Device
     * @return a string containing the Id of the device.
     */
    public String getAddress() {
        return address;
    }

    /**
     * The mode in which a Device is in
     * This can be either ON_BOARDING, CONNECTED_TO_MASTER_MODULE, DIRECT_CONNECTION or UNKNOWN
     * @return mode of type {@link BleDeviceMode}
     */
    public BleDeviceMode getMode() {
        return mode;
    }

    /**
     * The type of the Device
     * Possible values are: WunderbarHTU, WunderbarGYRO, WunderbarLIGHT, WunderbarMIC,
     * WunderbarBRIDG, WunderbarIR, WunderbarApp, Unknown
     * @return type of type {@link BleDeviceType}.
     */
    public BleDeviceType getType() {
        return type;
    }

    public Observable<? extends BaseService> connect() {
        return serviceObservable;
    }

    public Observable<BleDevice> disconnect() {
        mDeviceManager.removeDevice(this);
        return serviceObservable
                .flatMap(new Func1<BaseService, Observable<BleDevice>>() {
                    @Override
                    public Observable<BleDevice> call(BaseService service) {
                        return service.disconnect();
                    }
                });
    }

    public void refreshGatt() {
        if (mode != DIRECT_CONNECTION) return;
        serviceObservable
                .flatMap(new Func1<BaseService, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> call(final BaseService service) {
                        return Observable.create(new Observable.OnSubscribe<Boolean>() {
                            @Override
                            public void call(Subscriber<? super Boolean> subscriber) {
                                service.getGatt().disconnect();
                                service.getGatt().close();
                                DeviceCompatibilityUtils.refresh(service.getGatt());
                            }
                        });
                    }
                })
                .subscribe();
    }

    @Override
    public String toString() {
        return name + " - [" + address + "] MODE: " + mode.toString();
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BleDevice bleDevice = (BleDevice) o;

        if (mode != bleDevice.mode) return false;
        if (type != bleDevice.type) return false;
        if (address != null ? !address.equals(bleDevice.address) : bleDevice.address != null)
            return false;
        return !(name != null ? !name.equals(bleDevice.name) : bleDevice.name != null);
    }

    @Override
    public int hashCode() {
        int result = mode != null ? mode.hashCode() : 0;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (address != null ? address.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }
}
