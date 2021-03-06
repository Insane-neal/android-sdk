package io.relayr.android.ble.service;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.os.Build;

import com.google.gson.Gson;

import java.util.UUID;

import io.relayr.android.ble.BleDevice;
import io.relayr.android.ble.service.error.CharacteristicNotFoundException;
import io.relayr.java.model.DataPackage;
import io.relayr.java.model.action.Reading;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;

import static io.relayr.java.ble.parser.BleDataParser.getFormattedValue;
import static rx.Observable.error;

/**
 * A class representing the Direct Connection BLE Service.
 * The functionality and characteristics available when a device is in DIRECT_CONNECTION mode.
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class DirectConnectionService extends BaseService {

    DirectConnectionService(BleDevice device, BluetoothGatt gatt, BluetoothGattReceiver receiver) {
        super(device, gatt, receiver);
    }

    public static Observable<DirectConnectionService> connect(final BleDevice bleDevice,
                                                              final BluetoothDevice device) {
        final BluetoothGattReceiver receiver = new BluetoothGattReceiver();
        return doConnect(device, receiver, false)
                .flatMap(new BondingReceiver.BondingFunc1())
                .map(new Func1<BluetoothGatt, DirectConnectionService>() {
                    @Override
                    public DirectConnectionService call(BluetoothGatt gatt) {
                        return new DirectConnectionService(bleDevice, gatt, receiver);
                    }
                });
    }

    public Observable<Reading> getReadings() {
        BluetoothGattCharacteristic characteristic = Utils.getCharacteristicInServices(
                mBluetoothGatt.getServices(), ShortUUID.SERVICE_DIRECT_CONNECTION, ShortUUID.CHARACTERISTIC_SENSOR_DATA);
        if (characteristic == null) {
            return error(new CharacteristicNotFoundException(ShortUUID.CHARACTERISTIC_SENSOR_DATA));
        }
        BluetoothGattDescriptor descriptor = Utils.getDescriptorInCharacteristic(
                characteristic, ShortUUID.DESCRIPTOR_DATA_NOTIFICATIONS);
        return mBluetoothGattReceiver
                .subscribeToCharacteristicChanges(mBluetoothGatt, characteristic, descriptor)
                .map(new Func1<BluetoothGattCharacteristic, String>() {
                    @Override
                    public String call(BluetoothGattCharacteristic characteristic) {
                        return getFormattedValue(mBleDevice.getType(), characteristic.getValue());
                    }
                })
                .flatMap(new Func1<String, Observable<Reading>>() {
                    @Override
                    public Observable<Reading> call(final String s) {
                        return Observable.create(new Observable.OnSubscribe<Reading>() {
                            @Override
                            public void call(Subscriber<? super Reading> subscriber) {
                                DataPackage data = new Gson().fromJson(s, DataPackage.class);
                                for (DataPackage.Data dataPoint : data.readings) {
                                    subscriber.onNext(new Reading(data.received, dataPoint.recorded,
                                            dataPoint.meaning, dataPoint.path, dataPoint.value));
                                }
                            }
                        });
                    }
                });
    }

    public Observable<BluetoothGattCharacteristic> stopGettingReadings() {
        BluetoothGattCharacteristic characteristic = Utils.getCharacteristicInServices(
                mBluetoothGatt.getServices(), ShortUUID.SERVICE_DIRECT_CONNECTION, ShortUUID.CHARACTERISTIC_SENSOR_DATA);
        if (characteristic == null) {
            return error(new CharacteristicNotFoundException(ShortUUID.CHARACTERISTIC_SENSOR_DATA));
        }
        BluetoothGattDescriptor descriptor = Utils.getDescriptorInCharacteristic(
                characteristic, ShortUUID.DESCRIPTOR_DATA_NOTIFICATIONS);
        return mBluetoothGattReceiver
                .unsubscribeToCharacteristicChanges(mBluetoothGatt, characteristic, descriptor);
    }

    /**
     * Returns an observable of the Sensor Id characteristic.
     * See {@link android.bluetooth.BluetoothGatt#readCharacteristic} for details as to the actions performed in
     * the background.
     * @return an observable of the Sensor Id characteristic
     */
    public Observable<UUID> getSensorId() {
        final String text = "Sensor Id";
        return readUuidCharacteristic(ShortUUID.SERVICE_DIRECT_CONNECTION, ShortUUID.CHARACTERISTIC_SENSOR_ID, text);
    }

    /**
     * Indicates the sensorFrequency characteristic to the associated remote device. This is the time
     * elapsing between sending one BLE event and the next.
     * See {@link android.bluetooth.BluetoothGatt#readCharacteristic} for details as to the actions performed in
     * the background.
     * @return Observable, an observable of the sensor frequency value
     */
    public Observable<Integer> getSensorFrequency() {
        final String text = "Sensor Frequency";
        return readIntegerCharacteristic(
                ShortUUID.SERVICE_DIRECT_CONNECTION, ShortUUID.CHARACTERISTIC_SENSOR_FREQUENCY, text);
    }

    /**
     * Indicates the SensorThreshold characteristic to the associated remote device. This is the
     * value that must be exceeded for a sensor to register a change.
     * See {@link android.bluetooth.BluetoothGatt#readCharacteristic} for details as to the actions performed in
     * the background
     * @return Observable<BluetoothGattCharacteristic>, an observable of the sensor characteristic.
     * In order to get the value from the characteristic call
     * {@link android.bluetooth.BluetoothGattCharacteristic#getValue()}
     */
    public Observable<BluetoothGattCharacteristic> getSensorThreshold() {
        final String text = "Sensor Threshold";
        return readCharacteristic(ShortUUID.SERVICE_DIRECT_CONNECTION, ShortUUID.CHARACTERISTIC_SENSOR_THRESHOLD, text);
    }

    /**
     * Writes the sensorFrequency characteristic to the associated remote device. This is the time
     * elapsing between sending one BLE event and the next.
     * See {@link android.bluetooth.BluetoothGatt#writeCharacteristic} for details as to the actions performed in
     * the background.
     * @param sensorFrequency A number represented in Bytes to be written the remote device
     * @return Observable<BluetoothGattCharacteristic>, an observable of what will be written to the
     * remote device
     */
    public Observable<BluetoothGattCharacteristic> writeSensorFrequency(byte[] sensorFrequency) {
        return write(sensorFrequency, ShortUUID.SERVICE_DIRECT_CONNECTION, ShortUUID.CHARACTERISTIC_SENSOR_FREQUENCY);
    }

    /**
     * Writes the sensorLedState characteristic to the associated remote device. It will turn the
     * LED on if the operation is carried out successfully.
     * See {@link android.bluetooth.BluetoothGatt#writeCharacteristic} for details as to the actions performed in
     * the background.
     * @return Observable<BluetoothGattCharacteristic>, an observable of what will be written to the
     * device
     */
    public Observable<BluetoothGattCharacteristic> turnLedOn() {
        return write(new byte[]{0x01}, ShortUUID.SERVICE_DIRECT_CONNECTION, ShortUUID.CHARACTERISTIC_SENSOR_LED_STATE);
    }

    /**
     * Writes the command characteristic to the associated remote device. It will send the command
     * if the operation is carried out successfully.
     * See {@link android.bluetooth.BluetoothGatt#writeCharacteristic} for details as to the actions performed in
     * the background.
     * @return Observable<BluetoothGattCharacteristic>, an observable of what will be written to the
     * device
     */
    public Observable<BluetoothGattCharacteristic> sendCommand(byte[] bytes) {
        return write(bytes, ShortUUID.SERVICE_DIRECT_CONNECTION, ShortUUID.CHARACTERISTIC_SENSOR_SEND_COMMAND);
    }

    /**
     * Writes the sensorThreshold characteristic to the associated remote device. This is the
     * value that must be exceeded for a sensor to register a change.
     * See {@link android.bluetooth.BluetoothGatt#writeCharacteristic} for details as to the actions performed in
     * the background
     * @param sensorThreshold A number represented in Bytes to be written the remote device
     * @return Observable<BluetoothGattCharacteristic>, an observable of what will be written to the
     * device
     */
    public Observable<BluetoothGattCharacteristic> writeSensorThreshold(byte[] sensorThreshold) {
        return write(sensorThreshold, ShortUUID.SERVICE_DIRECT_CONNECTION, ShortUUID.CHARACTERISTIC_SENSOR_THRESHOLD);
    }

    /**
     * Writes the sensorConfig characteristic to the associated remote device.
     * See {@link android.bluetooth.BluetoothGatt#writeCharacteristic} for details as to the actions performed in
     * the background.
     * @param configuration A number represented in Bytes to be written the remote device
     * @return Observable<BluetoothGattCharacteristic>, an observable of what will be written to the
     * device
     */
    public Observable<BluetoothGattCharacteristic> writeSensorConfig(byte[] configuration) {
        return write(configuration, ShortUUID.SERVICE_DIRECT_CONNECTION, ShortUUID.CHARACTERISTIC_SENSOR_CONFIGURATION);
    }

}
