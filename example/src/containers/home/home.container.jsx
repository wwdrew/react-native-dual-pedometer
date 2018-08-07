import React, { Component } from "react";
import { HomeScreen } from "../../screens";
import RNDualPedometer, { pedometerEmitter } from "react-native-dual-pedometer";

class HomeContainer extends Component {
    componentDidMount() {
        pedometerEmitter.addListener(
            "pedometer:update",
            (update) => console.log(update)
        );
    }

    onPress = async () => {
        console.log("ON PRESS");

        try {
            const now = new Date();
            const result = await RNDualPedometer.startPedometerUpdatesFromDate(now);
            console.log("RESULT: ", result);
        } catch (e) {
            console.log("Starting failed");
        }
    }

    render() {
        return (
            <HomeScreen
                onPress={this.onPress}
            />
        );
    }
}

export default HomeContainer;
