package io.relayr.websocket;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import io.relayr.TestEnvironment;
import io.relayr.model.account.AccountType;
import io.relayr.model.MqttChannel;
import io.relayr.model.Transmitter;
import rx.Observable;
import rx.Subscriber;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class OnBoardClientTest extends TestEnvironment {

    @Mock private WebSocketFactory webSocketFactory;
    @Mock private WebSocket<Transmitter> webSocket;

    @Before
    public void init() {
        super.init();
        initSdk();
        inject();
    }

    @Test
    public void webSocketClientCreateTest() {
        final Observable<Transmitter> observable = Observable.create(new Observable.OnSubscribe<Transmitter>() {
            @Override
            public void call(Subscriber<? super Transmitter> subscriber) {
                subscriber.onNext(createTransmitterDevice());
            }
        });

        when(webSocket.createClient(any(Transmitter.class))).thenReturn(observable);
        when(webSocket.subscribe(anyString(), anyString(), any(WebSocketCallback.class))).thenReturn(true);
        when(webSocketFactory.createOnBoardingWebSocket()).thenReturn(webSocket);

        OnBoardingClient mSocketClient = new OnBoardingClient(webSocketFactory);
        mSocketClient.startOnBoarding(createTransmitterDevice()).subscribe();
        await();

        verify(webSocket, times(1)).createClient(any(Transmitter.class));
    }

    @Test
    public void webSocketClientSubscribeTest() {
        final Observable<Transmitter> observable = Observable.create(new Observable.OnSubscribe<Transmitter>() {
            @Override
            public void call(Subscriber<? super Transmitter> subscriber) {
                subscriber.onNext(createTransmitterDevice());
            }
        });

        when(webSocket.isConnected()).thenReturn(true);
        when(webSocket.createClient(any(Transmitter.class))).thenReturn(observable);
        when(webSocket.subscribe(anyString(), anyString(), any(WebSocketCallback.class))).thenReturn(true);
        when(webSocketFactory.createOnBoardingWebSocket()).thenReturn(webSocket);

        OnBoardingClient mSocketClient = new OnBoardingClient(webSocketFactory);
        mSocketClient.startOnBoarding(createTransmitterDevice()).subscribe();
        mSocketClient.getTransmitterPresence().subscribe();

        await();
        verify(webSocket, times(1)).subscribe(eq("topic/presence/connect"), anyString(), any(WebSocketCallback
                .class));
    }

    private Transmitter createTransmitterDevice() {
        final Transmitter transmitter = new Transmitter("o", "name", AccountType.WUNDERBAR_2);
        transmitter.setTopic("topic");
        transmitter.setCredentials(new MqttChannel.MqttCredentials("u", "p", "topic", "clientId"));
        return transmitter;
    }
}
