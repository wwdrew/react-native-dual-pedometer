package com.wwdrew.reactnative;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataSourcesRequest;
import com.google.android.gms.fitness.request.OnDataPointListener;
import com.google.android.gms.fitness.request.SensorRequest;
import com.google.android.gms.fitness.result.DataSourcesResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.joda.time.DateTime;

import java.util.concurrent.TimeUnit;

public class RNDualPedometerManager extends ReactContextBaseJavaModule implements LifecycleEventListener {

    public static final String PEDOMETER_UPDATE = "pedometer:update";

    private static final String TAG = "RNDualPedometer";
    private static final String REACT_MODULE = "RNGoogleFit";

    private boolean isSimulator;
    private ReactApplicationContext reactContext;
    private GoogleSignInManager mGoogleSignInManager;

    public RNDualPedometerManager(ReactApplicationContext context) {
        super(context);
        this.reactContext = context;
        this.mGoogleSignInManager = new GoogleSignInManager(this.reactContext);
        this.isSimulator = checkIsSimulator();
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
                .addDataType(DataType.TYPE_STEP_COUNT_DELTA)
                .addDataType(DataType.TYPE_STEP_COUNT_CADENCE)
                .addDataType(DataType.TYPE_SPEED)
                .addDataType(DataType.TYPE_DISTANCE_DELTA)
                .addDataType(DataType.TYPE_ACTIVITY_SEGMENT)
                .addDataType(DataType.TYPE_ACTIVITY_SAMPLES)

                .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA)
                .addDataType(DataType.AGGREGATE_SPEED_SUMMARY)
                .addDataType(DataType.AGGREGATE_DISTANCE_DELTA)
                .addDataType(DataType.AGGREGATE_ACTIVITY_SUMMARY)

                .build();
    }

    private boolean isAuthorised() {
        boolean authorised = GoogleSignIn.hasPermissions(
                GoogleSignIn.getLastSignedInAccount(reactContext),
                getFitnessOptions()
        );
        Log.d(TAG, String.format("Is Fit API Authorised? %s", authorised));

        if (!authorised) {
            Log.wtf(TAG, "YOU DIDN'T HAVE AUTH!");
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
        } else {
            Log.wtf(TAG, "YOU ALREADY HAVE AUTH");

            DataSourcesRequest dataSourceRequest = new DataSourcesRequest.Builder()
                    .setDataTypes(
                            DataType.TYPE_STEP_COUNT_DELTA,
                            DataType.TYPE_STEP_COUNT_CADENCE,
                            DataType.TYPE_SPEED,
                            DataType.TYPE_DISTANCE_DELTA,
                            DataType.TYPE_ACTIVITY_SEGMENT,
                            DataType.TYPE_ACTIVITY_SAMPLES,

                            DataType.AGGREGATE_STEP_COUNT_DELTA,
                            DataType.AGGREGATE_SPEED_SUMMARY,
                            DataType.AGGREGATE_DISTANCE_DELTA,
                            DataType.AGGREGATE_ACTIVITY_SUMMARY
                    )
                    .setDataSourceTypes(DataSource.TYPE_DERIVED)
                    .build();

            ResultCallback<DataSourcesResult> dataSourcesResultCallback = new ResultCallback<DataSourcesResult>() {
                @Override
                public void onResult(DataSourcesResult dataSourcesResult) {
                    for (DataSource dataSource : dataSourcesResult.getDataSources()) {
                        DataType type = dataSource.getDataType();
                        Log.i(TAG, "Register Fitness Listener: " + type);
                    }
                }
            };

            GoogleApiClient client = new GoogleApiClient.Builder(reactContext)
                    .addApi(Fitness.SENSORS_API)
                    .build();

            client.connect();

            Fitness.SensorsApi
                    .findDataSources(
                            client,
                            dataSourceRequest
                    )
                    .setResultCallback(dataSourcesResultCallback);
        }

        return authorised;
    }

    public void startPedometerUpdatesFromDate(DateTime dateTime) {
        Log.d(TAG, String.format("Manager Start Pedometer Updates From Date: %s", dateTime));

        if (isAuthorised()) {
            Log.d(TAG, String.format("Authorised: Starting Pedometer Updates from date: %s", dateTime));

            if (isSimulator) {
                emitEvent(PEDOMETER_UPDATE, getSimulatedPayload(dateTime));
            } else {
                GoogleSignInAccount gsa = GoogleSignIn.getAccountForExtension(reactContext, getFitnessOptions());

                Fitness.getSensorsClient(reactContext, gsa)
                        .add(new SensorRequest.Builder()
                                        .setDataType(DataType.TYPE_STEP_COUNT_CUMULATIVE)
                                        .setSamplingRate(10, TimeUnit.SECONDS)  // sample once per minute
                                        .build(),
                                new OnDataPointListener() {
                                    @Override
                                    public void onDataPoint(DataPoint dataPoint) {
                                        Log.i(TAG, String.format("DATA POINT: %s", dataPoint));
                                        emitEvent(PEDOMETER_UPDATE, mapPedometerPayload(dataPoint));
                                    }
                                }
                        );

            }
        } else {
            Log.d(TAG, "NOT Authorised: Unable to start pedometer updates");
        }
    }

    private void subscribe() {
        Fitness.getRecordingClient(reactContext, GoogleSignIn.getLastSignedInAccount(reactContext))
                .subscribe(DataType.TYPE_STEP_COUNT_CUMULATIVE)
                .addOnCompleteListener(
                        new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.wtf(TAG, "Successfully subscribed!");
                                } else {
                                    Log.wtf(TAG, "There was a problem subscribing.", task.getException());
                                }
                            }
                        }
                );
    }

    // TODO hook this up to pedometer
    private WritableMap mapPedometerPayload(DataPoint dataPoint) {
        WritableMap payload = Arguments.createMap();

        payload.putInt("steps", dataPoint.getValue(Field.FIELD_STEPS).asInt());
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

    private boolean checkIsSimulator() {
        return (
                Build.PRODUCT.toLowerCase().contains("sdk") &&
                        Build.MODEL.toLowerCase().contains("sdk")
        );
    }


    private void emitEvent(String eventName, Object payload) {
        this.reactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(eventName, payload);
    }
}
