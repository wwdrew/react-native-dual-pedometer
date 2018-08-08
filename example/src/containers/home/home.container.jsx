import React, { Component } from "react";
import { HomeScreen } from "../../screens";
import RNDualPedometer, { pedometerEmitter } from "react-native-dual-pedometer";

class HomeContainer extends Component {

    state = {
        currentCadence: 0,
        currentPace: 0,
        started: false,
        steps: 0
    }

    componentDidMount() {
        pedometerEmitter.addListener(
            "pedometer:update",
            (update) => {
                console.log(update);
                this.setState({
                    averageActivePace: update.averageActivePace,
                    currentCadence: update.currentCadence,
                    currentPace: update.currentPace,
                    distance: update.distance,
                    endTime: update.endTime,
                    startTime: update.startTime,
                    steps: update.steps
                });
            }
        );
    }

    onPress = async () => {
        console.log("ON PRESS");

        const { started } = this.state;

        if (!started) {
            try {
                const now = new Date();
                const result = await RNDualPedometer.startPedometerUpdatesFromDate(now);
                this.setState({ started: true });
                console.log("START RESULT: ", result);
            } catch (e) {
                console.log("Starting failed");
            }
        } else {
            try {
                const result = await RNDualPedometer.stopPedometerUpdates();
                this.setState({ started: false, steps: 0 });
                console.log("STOP RESULT: ", result);
            } catch (e) {
                console.log("Stopping failed");
            }
        }
    }

    render() {
        const {
            averageActivePace,
            currentCadence,
            currentPace,
            distance,
            endTime,
            started,
            startTime,
            steps,
        } = this.state;

        return (
            <HomeScreen
                averageActivePace={averageActivePace}
                currentCadence={currentCadence}
                currentPace={currentPace}
                distance={distance}
                endTime={endTime}
                onPress={this.onPress}
                started={started}
                startTime={startTime}
                steps={steps}
            />
        );
    }
}

export default HomeContainer;
