
import { NativeEventEmitter, NativeModules } from 'react-native';

const { RNDualPedometer } = NativeModules;

export const pedometerEmitter = new NativeEventEmitter(RNDualPedometer);

export default RNDualPedometer;
