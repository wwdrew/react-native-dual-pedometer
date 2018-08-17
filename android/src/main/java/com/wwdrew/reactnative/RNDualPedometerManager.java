package com.wwdrew.reactnative;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataType;

import org.joda.time.DateTime;

public class RNDualPedometerManager implements ActivityEventListener {

    public static final String PEDOMETER_UPDATE = "pedometer:update";

    private static final String TAG = "RNDualPedometer";

    private ReactApplicationContext reactContext;

    public RNDualPedometerManager(ReactApplicationContext context) {
        this.reactContext = context;
    }

    private FitnessOptions getFitnessOptions() {
        return FitnessOptions.builder()
                .addDataType(DataType.TYPE_STEP_COUNT_DELTA)
                .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA)
                .addDataType(DataType.TYPE_ACTIVITY_SAMPLES)
                .addDataType(DataType.AGGREGATE_ACTIVITY_SUMMARY)
                .build();
    }

    public boolean isAuthorised() {
        boolean authorised = GoogleSignIn.hasPermissions(
                GoogleSignIn.getLastSignedInAccount(reactContext),
                getFitnessOptions()
        );
        Log.d(TAG, String.format("Is Fit API Authorised? %s", authorised));

        return authorised;
    }

    public void startPedometerUpdatesFromDate(DateTime dateTime) {
        Log.d(TAG, String.format("Manager Start Pedometer Updates From Date: %s", dateTime));

        WritableMap payload = Arguments.createMap();

        payload.putInt("steps", 23456);
        payload.putString("startTime", dateTime.toString());
        payload.putString("endTime", new DateTime().toString());

        this.reactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(PEDOMETER_UPDATE, payload);
//        if (isAuthorised()) {
//            Log.d(TAG, String.format("Authorised: Starting Pedometer Updates from date: %s", date));
//            return true;
//        } else {
//            Log.d(TAG, "NOT Authorised: Unable to start pedometer updates");
//            return false;
//        }
    }

    @Override
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {

    }

    @Override
    public void onNewIntent(Intent intent) {

    }
}
