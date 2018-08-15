export interface PedometerResponse {
    startTime: string;
    endTime: string;
    steps: number;
    distance?: number;
    averageActivePace?: number;
    currentPace?: number;
    currentCadence?: number;
}

declare const RNDualPedometer: {
    addListener: (eventName: string, callback: any) => null;
    queryPedometerFromDate: (startTime: string) => Promise<PedometerResponse>;
    startPedometerUpdatesFromDate: (startTime: string) => null;
    stopPedometerUpdates: () => null;
}

export default RNDualPedometer;
