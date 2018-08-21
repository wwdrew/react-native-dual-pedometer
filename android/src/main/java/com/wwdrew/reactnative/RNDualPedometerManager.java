package com.wwdrew.reactnative;

import android.app.Activity;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.OnDataPointListener;
import com.google.android.gms.fitness.request.SensorRequest;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.joda.time.DateTime;

import java.util.concurrent.TimeUnit;

public class RNDualPedometerManager extends ReactContextBaseJavaModule implements LifecycleEventListener {

    public static final String PEDOMETER_UPDATE = "pedometer:update";

    private static final String TAG = "RNDualPedometer";
    private static final String REACT_MODULE = "RNGoogleFit";

    private ReactApplicationContext mReactContext;
    private GoogleSignInManager mGoogleSignInManager;
    private OnDataPointListener mListener;
    private Integer mBaseSteps;

    public RNDualPedometerManager(ReactApplicationContext context) {
        super(context);
        this.mReactContext = context;
        this.mGoogleSignInManager = new GoogleSignInManager(mReactContext);
    }

    @Override
    public void initialize() {
        super.initialize();
        getReactApplicationContext().addLifecycleEventListener(this);
    }

    @Override
    public String getName() {
        return REACT_MODULE;
    }

    @Override
    public void onHostResume() {
    }

    @Override
    public void onHostPause() {
    }

    @Override
    public void onHostDestroy() {
    }

    private FitnessOptions getFitnessOptions() {
        return FitnessOptions.builder()
                .addDataType(DataType.TYPE_STEP_COUNT_CUMULATIVE)
                .build();
    }

    private boolean hasPermissions() {
        return GoogleSignIn.hasPermissions(
                GoogleSignIn.getLastSignedInAccount(mReactContext),
                getFitnessOptions()
        );
    }

    private void authorise() {
        mGoogleSignInManager.requestPermissions(
                getCurrentActivity(),
                getFitnessOptions(),
                new GoogleSignInManager.ResultListener() {
                    @Override
                    public void onResult(int resultCode) {
                        Log.wtf(TAG, String.format("AUTH RESULT: %s", resultCode));
                        if (resultCode == Activity.RESULT_OK) {
                            Log.wtf(TAG, "AUTH SUCCESS");
                            subscribe();
                        } else if (resultCode == Activity.RESULT_CANCELED) {
                            Log.wtf(TAG, "AUTH CANCELLED");
                        }
                    }
                }
        );
    }

    private boolean isAuthorised() {
        boolean authorised = hasPermissions();
        Log.d(TAG, String.format("Is Fit API Authorised? %s", authorised));

        if (!hasPermissions()) {
            Log.d(TAG, "LOLWOT YOU DIDN'T HAVE AUTH!");
            authorise();
        }

        return hasPermissions();
    }

    public void startPedometerUpdatesFromDate(DateTime startTime) {
        Log.d(TAG, String.format("Manager Start Pedometer Updates From Date: %s", startTime));

        if (isAuthorised()) {
            Log.d(TAG, String.format("Authorised: Starting Pedometer Updates from date: %s", startTime));

            if (isSimulator()) {
                emitEvent(PEDOMETER_UPDATE, getSimulatedPayload(startTime));
            } else {
                // TODO get history value from startTime to now

                startSensorsClient(mListener = new OnDataPointListener() {
                    @Override
                    public void onDataPoint(DataPoint dataPoint) {
                        emitEvent(PEDOMETER_UPDATE, mapPedometerPayload(dataPoint));
                    }
                });
            }
        } else {
            Log.d(TAG, "NOT Authorised: Unable to start pedometer updates");
        }
    }

    public void stopPedometerUpdates() {
        if (isAuthorised()) {
            Fitness.getSensorsClient(mReactContext, GoogleSignIn.getLastSignedInAccount(mReactContext))
                    .remove(mListener);
            mBaseSteps = null;
        } else {
            Log.d(TAG, "NOT Authorised: Unable to stop pedometer updates");
        }
    }

    private void subscribe() {
        startRecordingClient();
    }

    private void startRecordingClient() {
        Fitness.getRecordingClient(mReactContext, GoogleSignIn.getLastSignedInAccount(mReactContext))
                .subscribe(DataType.TYPE_STEP_COUNT_CUMULATIVE)
                .addOnCompleteListener(
                        new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.wtf(TAG, "Successfully subscribed to TYPE STEP COUNT CUMULATIVE!");
                                } else {
                                    Log.wtf(TAG, "There was a problem subscribing.", task.getException());
                                }
                            }
                        }
                );
    }

    private void startSensorsClient(OnDataPointListener listener) {
        Fitness.getSensorsClient(mReactContext, GoogleSignIn.getLastSignedInAccount(mReactContext))
                .add(
                        new SensorRequest.Builder()
                                .setDataType(DataType.TYPE_STEP_COUNT_CUMULATIVE) // Can't be omitted.
                                .setSamplingRate(10, TimeUnit.SECONDS)
                                .build(),
                        listener)
                .addOnCompleteListener(
                        new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.i(TAG, "Listener registered!");
                                } else {
                                    Log.e(TAG, "Listener not registered.", task.getException());
                                }
                            }
                        });
    }

    private WritableMap mapPedometerPayload(DataPoint dataPoint) {
        WritableMap payload = Arguments.createMap();

        int dataPointSteps = dataPoint.getValue(Field.FIELD_STEPS).asInt();

        if (mBaseSteps == null) {
            mBaseSteps = dataPointSteps;
        }

        payload.putInt("steps", dataPointSteps - mBaseSteps);
        payload.putString("startTime", new DateTime(dataPoint.getStartTime(TimeUnit.MILLISECONDS)).toString());
        payload.putString("endTime", new DateTime(dataPoint.getEndTime(TimeUnit.MILLISECONDS)).toString());
        payload.putString("testing", dataPoint.toString());

        return payload;
    }

    private WritableMap getSimulatedPayload(DateTime dateTime) {
        WritableMap payload = Arguments.createMap();

        payload.putInt("steps", 23456);
        payload.putString("startTime", dateTime.toString());
        payload.putString("endTime", new DateTime().toString());

        return payload;
    }

    private boolean isSimulator() {
        return (
                Build.PRODUCT.toLowerCase().contains("sdk") &&
                        Build.MODEL.toLowerCase().contains("sdk")
        );
    }


    private void emitEvent(String eventName, Object payload) {
        mReactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(eventName, payload);
    }
}
