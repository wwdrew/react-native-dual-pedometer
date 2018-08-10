
import { NativeEventEmitter, NativeModules } from 'react-native';

const { RNDualPedometer } = NativeModules;

export const pedometerEmitter = new NativeEventEmitter(RNDualPedometer);

export default {
    addListener: pedometerEmitter.addListener,
    queryPedometerFromDate: RNDualPedometer.queryPedometerFromDate,
    startPedometerUpdatesFromDate: RNDualPedometer.startPedometerUpdatesFromDate,
    stopPedometerUpdates: RNDualPedometer.stopPedometerUpdates
};
