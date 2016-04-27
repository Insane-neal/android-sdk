package io.relayr.android.ble.service;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.util.Log;

import java.nio.ByteBuffer;

import io.relayr.android.ble.BleDevice;
import io.relayr.android.ble.BleDeviceMode;
import io.relayr.android.ble.service.error.CharacteristicNotFoundException;
import rx.Observable;
import rx.functions.Func1;

import static rx.Observable.error;

/**
 * A class representing the service associated with the NEW_ON_BOARDING mode
 * @see {@link BleDeviceMode}
 */
@Deprecated
public class OnBoardingV2Service extends BaseService {

    public enum OnBoardingStatus {
        SUCCESS, UN_CONFIGURED,
        WIFI_ERROR, TCP_ERROR, MQTT_ERROR, UNKNOWN_ERROR,
        WIFI_TRYING, TCP_TRYING, SSL_TRYING, MQTT_TRYING, CONFIG_FAILED,
        UNKNOWN
    }

    protected OnBoardingV2Service(BleDevice device, BluetoothGatt gatt,
                                  BluetoothGattReceiver receiver) {
        super(device, gatt, receiver);
    }

    public static Observable<OnBoardingV2Service> connect(final BleDevice bleDevice,
                                                          final BluetoothDevice device) {
        final BluetoothGattReceiver receiver = new BluetoothGattReceiver();
        return doConnect(device, receiver, true)
                .map(new Func1<BluetoothGatt, OnBoardingV2Service>() {
                    @Override
                    public OnBoardingV2Service call(BluetoothGatt gatt) {
                        return new OnBoardingV2Service(bleDevice, gatt, receiver);
                    }
                });
    }

    /**
     * Writes the WiFi SSID characteristic to the associated remote device.
     * See {@link BluetoothGatt#beginReliableWrite()} for details as to the actions performed in
     * the background.
     * @param ssid A number represented in Bytes to be written the remote device
     * @return Observable<BluetoothGatt>, an observable of what will be written to the
     * device
     */
    public Observable<BluetoothGatt> writeWiFiSSID(byte[] ssid) {
        return longWrite(ssid, ShortUUID.SERVICE_NEW_ON_BOARDING, ShortUUID.CHARACTERISTIC_WIFI_SSID);
    }

    /**
     * Writes the WiFi password characteristic to the associated remote device.
     * See {@link BluetoothGatt#beginReliableWrite()} for details as to the actions performed in
     * the background.
     * @param password A number represented in Bytes to be written the remote device
     * @return Observable<BluetoothGatt>, an observable of what will be written to the
     * device
     */
    public Observable<BluetoothGatt> writeWiFiPassword(byte[] password) {
        return longWrite(password, ShortUUID.SERVICE_NEW_ON_BOARDING, ShortUUID.CHARACTERISTIC_WIFI_PASSWORD);
    }

    /**
     * Writes the MQTT user characteristic to the associated remote device.
     * <p>
     * <p>See {@link BluetoothGatt#writeCharacteristic} for details as to the actions performed in
     * the background.
     * @param user A number represented in Bytes to be written the remote device
     * @return Observable<BluetoothGatt>, an observable of what will be written to the
     * device
     */
    public Observable<BluetoothGatt> writeMqttUser(byte[] user) {
        return longWrite(user, ShortUUID.SERVICE_NEW_ON_BOARDING, ShortUUID.CHARACTERISTIC_MQTT_USER);
    }

    /**
     * Writes the MQTT password characteristic to the associated remote device.
     * See {@link BluetoothGatt#beginReliableWrite()} for details as to the actions performed in
     * the background.
     * @param password A number represented in Bytes to be written the remote device
     * @return Observable<BluetoothGatt>, an observable of what will be written to the
     * device
     */
    public Observable<BluetoothGatt> writeMqttPassword(byte[] password) {
        return longWrite(password, ShortUUID.SERVICE_NEW_ON_BOARDING, ShortUUID.CHARACTERISTIC_MQTT_PASSWORD);
    }

    /**
     * Writes the MQTT topic characteristic to the associated remote device.
     * See {@link BluetoothGatt#beginReliableWrite()} for details as to the actions performed in
     * the background.
     * @param topic A number represented in Bytes to be written the remote device
     * @return Observable<BluetoothGatt>, an observable of what will be written to the
     * device
     */
    public Observable<BluetoothGatt> writeMqttTopic(byte[] topic) {
        return longWrite(topic, ShortUUID.SERVICE_NEW_ON_BOARDING, ShortUUID.CHARACTERISTIC_MQTT_TOPIC);
    }

    /**
     * Writes the transmitter characteristic to the associated remote device.
     * See {@link BluetoothGatt#beginReliableWrite()} for details as to the actions performed in
     * the background.
     * @param transmitter A number represented in Bytes to be written the remote device
     * @return Observable<BluetoothGatt>, an observable of what will be written to the
     * device
     */
    public Observable<BluetoothGatt> writeMqttHost(byte[] transmitter) {
        return longWrite(transmitter, ShortUUID.SERVICE_NEW_ON_BOARDING, ShortUUID.CHARACTERISTIC_MQTT_HOST);
    }

    /**
     * Writes the transmitter characteristic to the associated remote device.
     * See {@link BluetoothGatt#beginReliableWrite()} for details as to the actions performed in
     * the background.
     * @param transmitter A number represented in Bytes to be written the remote device
     * @return Observable<BluetoothGatt>, an observable of what will be written to the
     * device
     */
    public Observable<BluetoothGatt> writeMqttClientId(byte[] transmitter) {
        return longWrite(transmitter, ShortUUID.SERVICE_NEW_ON_BOARDING, ShortUUID.CHARACTERISTIC_MQTT_CLIENT_ID);
    }

    /**
     * Writes the transmitter characteristic to the associated remote device.
     * See {@link BluetoothGatt#writeCharacteristic} for details as to the actions performed in
     * the background.
     * @return Observable<BluetoothGatt>, an observable of what will be written to the
     * device
     */
    public Observable<BluetoothGatt> writeCommit() {
        return longWrite(new byte[1], ShortUUID.SERVICE_NEW_ON_BOARDING, ShortUUID.CHARACTERISTIC_COMMIT);
    }

    /**
     * Writes the transmitter characteristic to the associated remote device.
     * See {@link BluetoothGatt#writeCharacteristic} for details as to the actions performed in
     * the background.
     * @return Observable<BluetoothGatt>, an observable of what will be written to the
     * device
     */
    public Observable<OnBoardingStatus> getNotifications() {
        BluetoothGattCharacteristic characteristic = Utils.getCharacteristicInServices(
                mBluetoothGatt.getServices(), ShortUUID.SERVICE_NEW_ON_BOARDING, ShortUUID.CHARACTERISTIC_STATUS);
        if (characteristic == null) {
            return error(new CharacteristicNotFoundException(ShortUUID.CHARACTERISTIC_SENSOR_DATA));
        }
        BluetoothGattDescriptor descriptor = Utils.getDescriptorInCharacteristic(
                characteristic, ShortUUID.DESCRIPTOR_DATA_NOTIFICATIONS);

        return mBluetoothGattReceiver
                .subscribeToCharacteristicChanges(mBluetoothGatt, characteristic, descriptor)
                .map(new Func1<BluetoothGattCharacteristic, OnBoardingStatus>() {
                    @Override
                    public OnBoardingStatus call(BluetoothGattCharacteristic characteristic) {
                        ByteBuffer wrapped = ByteBuffer.wrap(characteristic.getValue());

                        try {
                            int status = wrapped.get(0);
                            return OnBoardingStatus.values()[status];
                        } catch (Exception e) {
                            Log.d("OnBoardingV2Service", "Failed to parse OnBoardingStatus.");
                            return OnBoardingStatus.UNKNOWN;
                        }
                    }
                });
    }
}
