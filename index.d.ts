declare module "react-native-dual-pedometer" {

    export interface PedometerResponse {
        startTime: number;
        endTime: number;
        steps: number;
        distance?: number;
        averageActivePace?: number;
        currentPace?: number;
        currentCadence?: number;
    }

    interface RNDualPedometer {
        addListener: (eventName: string, callback: () => null) => null;
        queryPedometerFromDate: () => Promise<PedometerResponse>;
        startPedometerUpdatesFromDate: () => null;
        stopPedometerUpdates: () => null;
    }

    export default RNDualPedometer;
}
