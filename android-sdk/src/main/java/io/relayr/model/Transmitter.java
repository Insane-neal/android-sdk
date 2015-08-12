package io.relayr.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

import io.relayr.RelayrSdk;
import io.relayr.model.account.AccountType;
import rx.Observable;

/**
 * The Transmitter class is a representation of the Transmitter entity.
 * A Transmitter is another basic entity on the relayr platform.
 * A transmitter contrary to a device does not gather data but is only used to relay the data
 * from the devices to the relayr platform.
 * The transmitter is also used to authenticate the different devices that transmit data via it.
 */
public class Transmitter implements Serializable {

    private String id;
    private String secret;
    private String owner;
    private String topic;
    private String name;
    private String clientId;
    private MqttChannel.MqttCredentials credentials;
    @SerializedName("integrationType") private String accountType;

    public Transmitter(String owner, String name, AccountType type) {
        this.owner = owner;
        this.name = name;
        this.accountType = type.getName();
    }

    /**
     * Specific for WUNDERBAR v1
     */
    public Transmitter(String id, String secret, String owner, String name) {
        this.id = id;
        this.secret = secret;
        this.owner = owner;
        this.name = name;
        accountType = AccountType.WUNDERBAR_1.getName();
    }

    /**
     * @return an {@link rx.Observable} with a list of devices that belong to the specific
     * transmitter.
     */
    public Observable<List<TransmitterDevice>> getDevices() {
        return RelayrSdk.getRelayrApi().getTransmitterDevices(id);
    }

    /**
     * Updates a transmitter.
     * @return an {@link rx.Observable} to the updated Transmitter
     */
    public Observable<Transmitter> updateTransmitter() {
        return RelayrSdk.getRelayrApi().updateTransmitter(this, id);
    }

    public String getId() {
        return id;
    }

    public String getSecret() {
        return secret;
    }

    public String getOwner() {
        return owner;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getTopic() {
        return topic;
    }

    public AccountType getAccountType() {
        return AccountType.getByName(accountType);
    }

    public String getClientId() {
        return clientId;
    }

    public MqttChannel.MqttCredentials getCredentials() {
        return credentials;
    }

    @Override public String toString() {
        return "Transmitter{" +
                "id='" + id + '\'' +
                ", secret='" + secret + '\'' +
                ", owner='" + owner + '\'' +
                ", topic='" + topic + '\'' +
                ", accountType='" + accountType + '\'' +
                ", name='" + name + '\'' +
                ", clientId='" + clientId + '\'' +
                ", credentials=" + credentials +
                '}';
    }
}
