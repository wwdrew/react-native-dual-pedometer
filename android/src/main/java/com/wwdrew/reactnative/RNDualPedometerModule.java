
package com.wwdrew.reactnative;

import android.util.Log;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

public class RNDualPedometerModule extends ReactContextBaseJavaModule {

    private final static String TAG = "RNDualPedometer";
    private final ReactApplicationContext reactContext;

    public RNDualPedometerModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    @Override
    public String getName() {
        return "RNDualPedometer";
    }

    @ReactMethod
    public void startPedometerUpdatesFromDate(String date, Promise promise) {
        Log.d(TAG, "startPedometerUpdatesFromDate");
        promise.resolve(true);
    }

    @ReactMethod
    public void stopPedometerUpdates(Promise promise) {
        Log.d(TAG, "stopPedometerUpdates");
        promise.resolve(true);
    }
}