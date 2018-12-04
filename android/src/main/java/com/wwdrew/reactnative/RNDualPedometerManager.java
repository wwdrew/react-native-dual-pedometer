package com.wwdrew.reactnative;

import android.app.Activity;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.SensorsClient;
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
    private SensorsClient mSensorClient;
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

    public void queryPedometerFromDate(DateTime startTime, DateTime endTime, final Promise promise) {
        Log.d(TAG, String.format("Manager queryPedometerFromDate - startTime: %s, endTime: %s", startTime, endTime));

        if (isAuthorised()) {
            DateTime startDateTime = new DateTime(startTime);
            DateTime endDateTime = new DateTime(endTime);

            DataReadRequest readRequest = new DataReadRequest.Builder()
                    .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
                    .bucketByActivityType(1, TimeUnit.SECONDS)
                    .setTimeRange(startDateTime.getMillis(), endDateTime.getMillis(), TimeUnit.MILLISECONDS)
                    .build();

            Fitness.getHistoryClient(mReactContext, GoogleSignIn.getLastSignedInAccount(mReactContext))
                    .readData(readRequest)
                    .addOnSuccessListener(
                            new OnSuccessListener<DataReadResponse>() {
                                @Override
                                public void onSuccess(DataReadResponse dataReadResponse) {
                                    promise.resolve(mapDataReadResponse(dataReadResponse));
                                }
                            }
                    );
        } else {
            promise.reject("RNDualPedometer", "Not authorised to query pedometer");
        }
    }

    public void startPedometerUpdatesFromDate(final DateTime startTime) {
        Log.d(TAG, String.format("Manager Start Pedometer Updates From Date: %s", startTime));

        if (isAuthorised()) {
            Log.d(TAG, String.format("Authorised: Starting Pedometer Updates from date: %s", startTime));

            if (isSimulator()) {
                emitEvent(PEDOMETER_UPDATE, getSimulatedPayload(startTime));
            } else {
                Fitness.getHistoryClient(mReactContext, GoogleSignIn.getLastSignedInAccount(mReactContext))
                        .readDailyTotal(DataType.TYPE_STEP_COUNT_DELTA)
                        .addOnSuccessListener(
                                new OnSuccessListener<DataSet>() {
                                    @Override
                                    public void onSuccess(DataSet dataSet) {

                                        mInitialSteps = dataSet.isEmpty()
                                                ? 0
                                                : dataSet.getDataPoints().get(0).getValue(Field.FIELD_STEPS).asInt();

                                        Log.i(TAG, String.format("INITIAL STEPS: %s", mInitialSteps));

                                        startSensorsClient(mListener = new OnDataPointListener() {
                                            @Override
                                            public void onDataPoint(DataPoint dataPoint) {
                                                WritableMap test = mapPedometerPayload(dataPoint, new DateTime(startTime));
                                                test.putInt("HISTORY_STEPS", mInitialSteps);
                                                emitEvent(PEDOMETER_UPDATE, test);
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
            mBaseSteps = null;
            mSensorClient.remove(mListener);
        } else {
            Log.d(TAG, "NOT Authorised: Unable to stop pedometer updates");
        }
    }

    private static Integer sumDataBuckets(DataReadResponse dataReadResult) {
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

        Log.i(TAG, "Initial steps: " + sum);

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
        mSensorClient = Fitness.getSensorsClient(mReactContext, GoogleSignIn.getLastSignedInAccount(mReactContext));
        mSensorClient
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
            Log.i(TAG, String.format("INITIAL BASE STEPS: %s", mBaseSteps));
        }

        int stepsSinceStart = mInitialSteps + dataPointSteps - mBaseSteps;
        payload.putInt("steps", stepsSinceStart);
        payload.putString("startTime", startTime.toString());
        payload.putString("endTime", new DateTime(dataPoint.getEndTime(TimeUnit.MILLISECONDS)).toString());
        payload.putString("rawResponse", dataPoint.toString());
        payload.putInt("baseSteps", mBaseSteps);
        payload.putInt("dataPointSteps", dataPointSteps);

        Log.i(TAG, String.format("PEDOMETER UPDATE: %s", dataPoint));
        Log.i(TAG, String.format("PEDOMETER VALUE: %s", dataPointSteps));
        Log.i(TAG, String.format("STEPS SINCE START: %s", stepsSinceStart));

        return payload;
    }

    private WritableMap mapDataReadResponse(DataReadResponse response) {
        Log.i(TAG, String.format("Number of buckets in response: %d", response.getBuckets().size()));
        WritableMap results = Arguments.createMap();

        for (Bucket bucket : response.getBuckets()) {
            Log.i(TAG, String.format("Bucket type is : %s", bucket.getBucketType()));
            Log.i(TAG, String.format("Bucket activity is : %s", bucket.getActivity()));
            Log.i(TAG, String.format("Bucket startTime : %s", bucket.getStartTime(TimeUnit.SECONDS)));
            Log.i(TAG, String.format("Bucket endTime is : %s", bucket.getEndTime(TimeUnit.SECONDS)));

            results.putString("startTime", new DateTime(bucket.getStartTime(TimeUnit.MILLISECONDS)).toString());
            results.putString("endTime", new DateTime(bucket.getEndTime(TimeUnit.MILLISECONDS)).toString());
            results.putInt("steps", sumDataBuckets(response));
        }

        results.putString("rawResponse", response.toString());
        Log.i(TAG, String.format("results: %s", results));

        return results;
    }

    private WritableMap getSimulatedPayload(DateTime dateTime) {
        WritableMap payload = Arguments.createMap();

        payload.putInt("steps", 1234);
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
