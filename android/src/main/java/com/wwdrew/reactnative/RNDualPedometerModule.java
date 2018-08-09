
package com.wwdrew.reactnative;

import android.util.Log;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

public class RNDualPedometerModule extends ReactContextBaseJavaModule {

    private static final String REACT_CLASS = "RNDualPedometer";
    private static final String TAG = "RNDualPedometer";
    private final ReactApplicationContext reactContext;
    private RNDualPedometerManager rnDualPedometerManager;

    public RNDualPedometerModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    @Override
    public String getName() {
        return REACT_CLASS;
    }

    @ReactMethod
    public void startPedometerUpdatesFromDate(String date, Promise promise) {
        Log.d(TAG, String.format("startPedometerUpdatesFromDate: %s", date));

        if (rnDualPedometerManager == null) {
            rnDualPedometerManager = new RNDualPedometerManager(reactContext);
        }

        promise.resolve(rnDualPedometerManager.startPedometerUpdatesFromDate(date));
    }

    @ReactMethod
    public void stopPedometerUpdates(Promise promise) {
        Log.d(TAG, "stopPedometerUpdates");
        promise.resolve(true);
    }
}
