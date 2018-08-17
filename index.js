
import { NativeEventEmitter, NativeModules } from 'react-native';

const { RNDualPedometer } = NativeModules;
const pedometerEmitter = new NativeEventEmitter(RNDualPedometer);

export default {
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
