package io.relayr.android.util;

import javax.inject.Inject;

import io.relayr.java.api.CloudApi;
import rx.Observable;
import rx.Subscriber;

public class MockReachabilityUtils extends ReachabilityUtils {

    @Inject
    MockReachabilityUtils(CloudApi api) {
        super(api);
    }

    @Override
    public Observable<Boolean> isPlatformReachable() {
        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> sub) {
                sub.onNext(true);
            }
        });
    }

    @Override

    public boolean isConnectedToInternet() {
        return true;
    }

    @Override
    public boolean isPermissionGranted(String permission) {
        return true;
    }
}
