
export const displayDate = (dateTime) => {
    if (!dateTime) {
        return "--:--:--";
    }

    const date = new Date(dateTime);
    const hours = `${date.getHours()}`;
    const minutes = `${date.getMinutes()}`;
    const seconds = `${date.getSeconds()}`;

    return `${hours.padStart(2, "0")}:${minutes.padStart(2, "0")}:${seconds.padStart(2, "0")}`;
}

export const displayDistance = (distance) => {
    return `${distance.toFixed(2)}m`;
}

export const displayCadence = (cadence) => {
    return `${cadence.toFixed(2)} steps/second`;
}

export const displayPace = (pace) => {
    return `${pace.toFixed(2)} m/s`
}
