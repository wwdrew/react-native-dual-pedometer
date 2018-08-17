export interface PedometerResponse {
    startTime: string;
    endTime: string;
    steps: number;
    distance?: number;
    averageActivePace?: number;
    currentPace?: number;
    currentCadence?: number;
}

interface PedometerConstants {
    PEDOMETER_UPDATE: string;
}

declare const RNDualPedometer: {
    constants: PedometerConstants;
    addListener: (eventName: string, callback: any) => null;
    queryPedometerFromDate: (startTime: string, endTime?: string) => Promise<PedometerResponse>;
    startPedometerUpdatesFromDate: (startTime: string) => null;
    stopPedometerUpdates: () => null;
}

export default RNDualPedometer;
