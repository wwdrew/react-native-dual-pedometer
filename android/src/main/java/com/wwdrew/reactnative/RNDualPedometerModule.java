
package com.wwdrew.reactnative;

import android.util.Log;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

import org.joda.time.DateTime;

import java.util.HashMap;
import java.util.Map;

public class RNDualPedometerModule extends ReactContextBaseJavaModule {

    private static final String REACT_CLASS = "RNDualPedometer";
    private static final String TAG = "RNDualPedometer";
    private final ReactApplicationContext reactContext;
    private RNDualPedometerManager rnDualPedometerManager;

    public RNDualPedometerModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
        rnDualPedometerManager = new RNDualPedometerManager(this.reactContext);
    }

    @Override
    public String getName() {
        return REACT_CLASS;
    }

    @Override
    public Map<String, Object> getConstants() {
        HashMap<String, Object> pedometerConstants = new HashMap<>();
        pedometerConstants.put("PEDOMETER_UPDATE", RNDualPedometerManager.PEDOMETER_UPDATE);

        final Map<String, Object> constants = new HashMap<>();
        constants.put("constants", pedometerConstants);
        return constants;
    }

    @ReactMethod
    public void queryPedometerFromDate(String startTime, String endTime, Promise promise) {
        Log.d(TAG, String.format("queryPedometerFromDate - startTime: %s, endTime: %s", startTime, endTime));

        rnDualPedometerManager.queryPedometerFromDate(new DateTime(startTime), new DateTime(endTime));
    }

    @ReactMethod
    public void startPedometerUpdatesFromDate(String date) {
        Log.d(TAG, String.format("startPedometerUpdatesFromDate: %s", date));

        rnDualPedometerManager.startPedometerUpdatesFromDate(new DateTime(date));
    }

    @ReactMethod
    public void stopPedometerUpdates() {
        Log.d(TAG, "stopPedometerUpdates");

        rnDualPedometerManager.stopPedometerUpdates();
    }
}
