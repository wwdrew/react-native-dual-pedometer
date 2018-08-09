package com.wwdrew.reactnative;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.ReactApplicationContext;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataType;

public class RNDualPedometerManager implements ActivityEventListener {

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

    public boolean startPedometerUpdatesFromDate(String date) {
        Log.d(TAG, "Manager Start Pedometer Updates From Date");

        if (isAuthorised()) {
            Log.d(TAG, String.format("Authorised: Starting Pedometer Updates from date: %s", date));
            return true;
        } else {
            Log.d(TAG, "NOT Authorised: Unable to start pedometer updates");
            return false;
        }
    }

    @Override
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {

    }

    @Override
    public void onNewIntent(Intent intent) {

    }
}
