import React from "react";
import { StyleSheet, Text, View, TouchableOpacity } from "react-native";

const HomeScreen = ({ onPress }) => (
    <View style={styles.container}>
        <Text style={styles.welcome}>Welcome to React Native!</Text>
        <TouchableOpacity onPress={onPress}>
            <Text style={styles.welcome}>Press to start</Text>
        </TouchableOpacity>
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
