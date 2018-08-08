import React from "react";
import { StyleSheet, Text, View, TouchableOpacity } from "react-native";

const displayDate = (dateTime) => {
    if (!dateTime) {
        return "--:--:--";
    }

    const date = new Date(dateTime);

    return `${date.getHours()}:${date.getMinutes()}:${date.getSeconds()}`;
}

const HomeScreen = ({ onPress, started, startTime, endTime, steps }) => (
    <View style={styles.container}>
        <Text style={styles.welcome}>Welcome to React Native!</Text>
        <TouchableOpacity onPress={onPress}>
            <Text style={styles.welcome}>Press to {started ? "stop" : "start"}</Text>
        </TouchableOpacity>
        {started && (
            <>
                <Text>Steps: {steps}</Text>
                <Text>Start Time: {displayDate(startTime)}</Text>
                <Text>End Time: {displayDate(endTime)}</Text>
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
