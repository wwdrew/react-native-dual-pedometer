import { DeviceEventEmitter, NativeEventEmitter, NativeModules, Platform } from "react-native";

const { RNDualPedometer, RNDualPedometerEventEmitter } = NativeModules;

const pedometerEmitter = Platform.select({
    android: DeviceEventEmitter,
    ios: new NativeEventEmitter(RNDualPedometerEventEmitter)
})

const pedometerConstants = Platform.select({
    android: RNDualPedometer.constants,
    ios: pedometerEmitter.constants
});

export default {
    constants: pedometerConstants,
    addListener(event, callback) {
        pedometerEmitter.addListener(event, callback);
    },
    queryPedometerFromDate(startTime, endTime) {
        return RNDualPedometer.queryPedometerFromDate(startTime, endTime);
    },
    startPedometerUpdatesFromDate(startTime) {
        RNDualPedometer.startPedometerUpdatesFromDate(startTime);
    },
    stopPedometerUpdates() {
        RNDualPedometer.stopPedometerUpdates();
    }
};
