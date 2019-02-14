import { DeviceEventEmitter, NativeEventEmitter, NativeModules, Platform } from "react-native";

const { RNDualPedometer, RNDualPedometerEventEmitter } = NativeModules;

const pedometerEmitter = Platform.select({
    android: DeviceEventEmitter,
    ios: new NativeEventEmitter(RNDualPedometerEventEmitter)
})

const pedometerConstants = Platform.select({
    android: RNDualPedometer,
    ios: RNDualPedometerEventEmitter
});

export default {
    constants: pedometerConstants.constants,
    addListener(event, callback) {
        return pedometerEmitter.addListener(event, callback);
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
