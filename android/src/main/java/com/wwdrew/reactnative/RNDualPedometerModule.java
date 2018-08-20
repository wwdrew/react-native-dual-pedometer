
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
    public void startPedometerUpdatesFromDate(String date) {
        Log.d(TAG, String.format("startPedometerUpdatesFromDate: %s", date));
        DateTime dateTime = new DateTime(date);

        rnDualPedometerManager.startPedometerUpdatesFromDate(dateTime);
    }

    @ReactMethod
    public void stopPedometerUpdates(Promise promise) {
        Log.d(TAG, "stopPedometerUpdates");
        promise.resolve(true);
    }
}
