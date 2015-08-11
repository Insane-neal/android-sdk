package io.relayr.model;

import android.bluetooth.BluetoothGattCharacteristic;

import java.io.Serializable;

import io.relayr.RelayrSdk;
import io.relayr.ble.BleDevicesCache;
import io.relayr.ble.service.BaseService;
import io.relayr.ble.service.DirectConnectionService;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.subscriptions.Subscriptions;

/**
 * The transmitter device object holds the same information as the {@link io.relayr.model.Device}
 * The difference is that the model attribute in the former is an ID rather than an object.
 */
public class TransmitterDevice extends Transmitter implements Serializable {

    private final String deviceModelId;
    private transient Subscription mReadingsSubscription = Subscriptions.empty();

    public TransmitterDevice(String id, String secret, String owner, String name,
                             String model) {
        super(id, secret, owner, name);
        this.deviceModelId = model;
    }

    public String getModelId() {
        return deviceModelId;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof TransmitterDevice && ((TransmitterDevice) o).id.equals(id) ||
                o instanceof Device && ((Device) o).getId().equals(id);
    }

    public Observable<Reading> subscribeToCloudReadings() {
        return RelayrSdk.getWebSocketClient().subscribe(toDevice());
    }

    public Observable<BaseService> getSensorForDevice(BleDevicesCache cache) {
        return cache.getSensorForDevice(this);
    }

    //TODO Remove this stupid fix ASAP
    public Device toDevice() {
        return new Device(null, false, null, null, null, null, new Model(deviceModelId), getName(), id);
    }

    /**
     * Subscribes an app to a BLE device. Enables the app to receive data from the device over
     * BLE through {@link io.relayr.ble.service.DirectConnectionService}
     */
    public Observable<Reading> subscribeToBleReadings(final BleDevicesCache cache) {
        return Observable.create(new Observable.OnSubscribe<Reading>() {
            @Override
            public void call(final Subscriber<? super Reading> subscriber) {
                getSensorForDevice(cache)
                        .subscribeOn(AndroidSchedulers.mainThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnUnsubscribe(new Action0() {
                            @Override public void call() {
                                mReadingsSubscription.unsubscribe();
                            }
                        })
                        .subscribe(new Observer<BaseService>() {
                            @Override
                            public void onCompleted() {
                            }

                            @Override
                            public void onError(Throwable e) {
                                subscriber.onError(e);
                            }

                            @Override
                            public void onNext(final BaseService baseService) {
                                if (!(baseService instanceof DirectConnectionService)) return;
                                final DirectConnectionService service = (DirectConnectionService) baseService;

                                mReadingsSubscription = service.getReadings()
                                        .subscribeOn(AndroidSchedulers.mainThread())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .doOnUnsubscribe(new Action0() {
                                            @Override
                                            public void call() {
                                                service.stopGettingReadings()
                                                        .subscribeOn(AndroidSchedulers.mainThread())
                                                        .subscribe(new Observer<BluetoothGattCharacteristic>() {
                                                            @Override
                                                            public void onCompleted() {
                                                            }

                                                            @Override
                                                            public void onError(Throwable e) {
                                                                subscriber.onError(e);
                                                            }

                                                            @Override
                                                            public void onNext(BluetoothGattCharacteristic characteristic) {
                                                            }
                                                        });
                                            }
                                        })
                                        .subscribe(new Observer<Reading>() {
                                            @Override
                                            public void onCompleted() {
                                                subscriber.onCompleted();
                                            }

                                            @Override
                                            public void onError(Throwable e) {
                                                subscriber.onError(e);
                                            }

                                            @Override
                                            public void onNext(Reading reading) {
                                                subscriber.onNext(reading);
                                            }
                                        });
                            }
                        });
            }
        });
    }

    public void unSubscribeToCloudReadings() {
        RelayrSdk.getWebSocketClient().unSubscribe(id);
    }

    /**
     * Sends a command to the this device
     */
    public Observable<Void> sendCommand(Command command) {
        return RelayrSdk.getRelayrApi().sendCommand(id, command);
    }
}
