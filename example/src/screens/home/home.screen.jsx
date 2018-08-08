import React from "react";
import { StyleSheet, Text, View, TouchableOpacity } from "react-native";
import { displayCadence, displayDate, displayDistance, displayPace } from "./home.helpers";

const HomeScreen = ({
    averageActivePace,
    currentCadence,
    currentPace,
    distance,
    endTime,
    onPress,
    started,
    startTime,
    steps
}) => (
    <View style={styles.container}>
        <Text style={styles.welcome}>RN Dual Pedometer</Text>
        <TouchableOpacity onPress={onPress}>
            <Text style={styles.welcome}>Press to {started ? "stop" : "start"}</Text>
        </TouchableOpacity>
        {started && (
            <>
                <Text>Start Time: {displayDate(startTime)}</Text>
                <Text>End Time: {displayDate(endTime)}</Text>
                <Text>Steps: {steps}</Text>
                <Text>Current Cadence: {displayCadence(currentCadence)}</Text>
                <Text>Current Pace: {displayPace(currentPace)}</Text>
                <Text>Average Pace: {averageActivePace ? displayPace(averageActivePace) : "N/A"}</Text>
                <Text>Distance: {distance ? displayDistance(distance) : "N/A"}</Text>
            </>
        )}
    </View>
);

export default HomeScreen;

const styles = StyleSheet.create({
    container: {
        flex: 1,
        justifyContent: "center",
        alignItems: "center",
        backgroundColor: "#F5FCFF",
    },
    welcome: {
        fontSize: 20,
        textAlign: "center",
        margin: 10,
    },
    instructions: {
        textAlign: "center",
        color: "#333333",
        marginBottom: 5,
    },
});
