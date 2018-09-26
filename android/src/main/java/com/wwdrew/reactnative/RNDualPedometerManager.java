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
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.request.OnDataPointListener;
import com.google.android.gms.fitness.request.SensorRequest;
import com.google.android.gms.fitness.result.DataReadResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.joda.time.DateTime;

import java.text.DateFormat;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.text.DateFormat.getTimeInstance;

public class RNDualPedometerManager extends ReactContextBaseJavaModule implements LifecycleEventListener {

    public static final String PEDOMETER_UPDATE = "pedometer:update";

    private static final String TAG = "RNDualPedometer";
    private static final String REACT_MODULE = "RNGoogleFit";

    private ReactApplicationContext mReactContext;
    private GoogleSignInManager mGoogleSignInManager;
    private OnDataPointListener mListener;
    private Integer mBaseSteps;
    private Integer mInitialSteps = 0;

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

    public void queryPedometerFromDate(DateTime startTime, DateTime endTime) {
        Log.d(TAG, String.format("Manager queryPedometerFromDate - startTime: %s, endTime: %s", startTime, endTime));
    }

    public void startPedometerUpdatesFromDate(DateTime startTime) {
        Log.d(TAG, String.format("Manager Start Pedometer Updates From Date: %s", startTime));

        if (isAuthorised()) {
            Log.d(TAG, String.format("Authorised: Starting Pedometer Updates from date: %s", startTime));

            if (isSimulator()) {
                emitEvent(PEDOMETER_UPDATE, getSimulatedPayload(startTime));
            } else {
                final DateTime fStartTime = new DateTime(startTime);
                DateTime endTime = new DateTime();

                DataReadRequest readRequest = new DataReadRequest.Builder()
                        .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
                        .bucketByActivityType(1, TimeUnit.HOURS)
                        .setTimeRange(startTime.getMillis(), endTime.getMillis(), TimeUnit.MILLISECONDS)
                        .build();

                Fitness.getHistoryClient(mReactContext, GoogleSignIn.getLastSignedInAccount(mReactContext))
                        .readData(readRequest)
                        .addOnSuccessListener(
                                new OnSuccessListener<DataReadResponse>() {
                                    @Override
                                    public void onSuccess(DataReadResponse dataReadResponse) {

                                        mInitialSteps = sumInitialSteps(dataReadResponse);
                                        Log.i(TAG, String.format("INITIAL STEPS: %s", mInitialSteps));
//                                        printData(dataReadResponse);

                                        startSensorsClient(mListener = new OnDataPointListener() {
                                            @Override
                                            public void onDataPoint(DataPoint dataPoint) {
                                                emitEvent(PEDOMETER_UPDATE, mapPedometerPayload(dataPoint, fStartTime));
                                            }
                                        });
                                    }
                                }
                        );
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

    private static Integer sumInitialSteps(DataReadResponse dataReadResult) {
        Integer sum = 0;

        Log.i(TAG, "Number of returned buckets of DataSets is: " + dataReadResult.getBuckets().size());
        for (Bucket bucket : dataReadResult.getBuckets()) {
            List<DataSet> dataSets = bucket.getDataSets();
            for (DataSet dataSet : dataSets) {
                for (DataPoint dp : dataSet.getDataPoints()) {
                    for (Field field : dp.getDataType().getFields()) {
                        sum += dp.getValue(Field.FIELD_STEPS).asInt();
//                        Log.i(TAG, "\tField: " + field.getName() + " Value: " + dp.getValue(field));
                    }
                }
            }
        }

        return sum;
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

    private boolean isAuthorised() {
        if (!hasPermissions()) {
            Log.d(TAG, "LOLWOT YOU DIDN'T HAVE AUTH!");

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

        return hasPermissions();
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

    private WritableMap mapPedometerPayload(DataPoint dataPoint, DateTime startTime) {
        WritableMap payload = Arguments.createMap();

        int dataPointSteps = dataPoint.getValue(Field.FIELD_STEPS).asInt();

        if (mBaseSteps == null) {
            mBaseSteps = dataPointSteps;
        }

        payload.putInt("steps", mInitialSteps + dataPointSteps - mBaseSteps);
        payload.putString("startTime", startTime.toString());
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
