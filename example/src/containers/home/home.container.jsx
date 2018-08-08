import React, { Component } from "react";
import { HomeScreen } from "../../screens";
import RNDualPedometer, { pedometerEmitter } from "react-native-dual-pedometer";

class HomeContainer extends Component {

    state = {
        started: false,
        steps: 0
    }

    componentDidMount() {
        pedometerEmitter.addListener(
            "pedometer:update",
            (update) => {
                console.log(update);
                this.setState({
                    steps: update.steps,
                    startTime: update.startTime,
                    endTime: update.endTime
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
        const { started, startTime, endTime, steps } = this.state;

        return (
            <HomeScreen
                onPress={this.onPress}
                started={started}
                startTime={startTime}
                endTime={endTime}
                steps={steps}
            />
        );
    }
}

export default HomeContainer;
