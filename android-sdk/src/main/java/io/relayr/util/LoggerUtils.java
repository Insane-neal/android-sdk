package io.relayr.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.relayr.api.CloudApi;
import io.relayr.model.LogEvent;
import io.relayr.storage.DataStorage;
import rx.Subscriber;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

@Singleton
public class LoggerUtils {

    private static final int AUTO_FLUSH = 5;

    private static CloudApi sApi;
    private static ReachabilityUtils sReachUtils;

    private static ConcurrentLinkedQueue<LogEvent> sEvents;
    private static boolean loggingData = false;

    @Inject
    LoggerUtils(CloudApi api, ReachabilityUtils reachUtils) {
        sApi = api;

        sReachUtils = reachUtils;
        sEvents = new ConcurrentLinkedQueue<>();
    }

    public boolean logMessage(String message) {
        if (DataStorage.getUserToken().isEmpty()) return false;

        sEvents.add(new LogEvent(message == null ? "null" : message));

        if (sEvents.size() >= AUTO_FLUSH && !loggingData) {
            loggingData = true;
            sReachUtils.isPlatformReachable()
                    .observeOn(Schedulers.newThread())
                    .subscribeOn(Schedulers.newThread())
                    .subscribe(new Action1<Boolean>() {
                        @Override
                        public void call(Boolean status) {
                            if (status != null && status) logToPlatform(pollElements(AUTO_FLUSH));
                            else loggingData = false;
                        }
                    });
        }

        return sReachUtils.isConnectedToInternet();
    }

    public boolean flushLoggedMessages() {
        if (sEvents.isEmpty() || !sReachUtils.isConnectedToInternet()) return false;

        final int eventsToFlush = sEvents.size();

        loggingData = true;

        sReachUtils.isPlatformAvailable()
                .observeOn(Schedulers.newThread())
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Subscriber<Boolean>() {
<<<<<<< HEAD
                    @Override
                    public void onCompleted() {
                        loggingData = false;
                    }

                    @Override
=======
                    @Override
                    public void onCompleted() {
                        loggingData = false;
                    }

                    @Override
>>>>>>> 152f9986aadad9bbc04e1afda46aaef511f4a0c9
                    public void onError(Throwable e) {
                        loggingData = false;
                    }

                    @Override
                    public void onNext(Boolean status) {
<<<<<<< HEAD
                        if (status) logToPlatform(pollElements(eventsToFlush));
=======
                        if (status) logToPlatform(pollElements(sEvents.size()));
>>>>>>> 152f9986aadad9bbc04e1afda46aaef511f4a0c9
                        else loggingData = false;
                    }
                });

        return true;
    }

    private void logToPlatform(final List<LogEvent> events) {
        if(events.isEmpty()) return;

        sApi.logMessage(events)
                .observeOn(Schedulers.newThread())
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Subscriber<Void>() {
                    @Override
                    public void onCompleted() {
                        loggingData = false;
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        sEvents.addAll(events);
                        loggingData = false;
                    }

                    @Override
                    public void onNext(Void aVoid) {
                        loggingData = false;
                    }
                });
    }

    private List<LogEvent> pollElements(int total) {
        synchronized (new Object()) {
            int elements = sEvents.size() < total ? sEvents.size() : total;

            List<LogEvent> events = new ArrayList<>(elements);
            for (int i = 0; i < elements; i++) {
                events.add(sEvents.poll());
            }

            return events;
        }
    }
}

