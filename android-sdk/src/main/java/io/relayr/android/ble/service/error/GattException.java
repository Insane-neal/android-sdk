package io.relayr.android.ble.service.error;

public class GattException extends Exception {

    public GattException(String detailMessage) {
        super(detailMessage);
    }
}
