
# react-native-dual-pedometer

[![CodeFactor](https://www.codefactor.io/repository/github/poppiestar/react-native-dual-pedometer/badge)](https://www.codefactor.io/repository/github/poppiestar/react-native-dual-pedometer) [![BCH compliance](https://bettercodehub.com/edge/badge/poppiestar/react-native-dual-pedometer?branch=master)](https://bettercodehub.com/)

Another React Native pedometer bridge? Yes, another React Native pedometer bridge. However, this one is a little different. This one works with iOS and Android using the same API. In the case of iOS, it uses the CMPedometer module.
For Android, it uses Google Fit. The API itself is based on the
iOS CMPedometer module, so you're able to start tracking steps from a specified time in the past and receive live updates as the number of steps taken increases.

## Getting started

`$ npm install react-native-dual-pedometer --save`

### Mostly automatic installation

`$ react-native link react-native-dual-pedometer`

For iOS, see *Additional iOS Installation* instructions below

### Manual installation

#### iOS

1. In XCode, in the project navigator, right click `Libraries` ➜ `Add Files to [your project's name]`
2. Go to `node_modules` ➜ `react-native-dual-pedometer` and add `RNDualPedometer.xcodeproj`
3. In XCode, in the project navigator, select your project. Add `libRNDualPedometer.a` to your project's `Build Phases` ➜ `Link Binary With Libraries`
4. Run your project (`Cmd+R`)

### Additional iOS Installation

Add an entry to your `Info.plist`, either using XCode or editing the file manually.

#### Using XCode

Select your `Info.plist` file from your application files and add a new entry:

- Property: `Privacy - Motion Usage Description`
- Value: (reason for requiring Pedometer permissions)

#### Editing Info.plist manually

Add the following to your `Info.plist` file:

```
<dict>
    ....
    <key>NSMotionUsageDescription</key>
    <string>(reason for requiring Pedometer permissions)</string>
    ....
</dict>
```

#### Android

1. Open up `android/app/src/main/java/[...]/MainActivity.java`
  - Add `import com.wwdrew.reactnative.RNDualPedometerPackage;` to the imports at the top of the file
  - Add `new RNDualPedometerPackage()` to the list returned by the `getPackages()` method
2. Append the following lines to `android/settings.gradle`:
  	```
  	include ':react-native-dual-pedometer'
  	project(':react-native-dual-pedometer').projectDir = new File(rootProject.projectDir, 	'../node_modules/react-native-dual-pedometer/android')
  	```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
  	```
      compile project(':react-native-dual-pedometer')
  	```


## Usage
```javascript
import RNDualPedometer from 'react-native-dual-pedometer';
```

There are three functions available:

```queryPedometerFromDate(startDate, endDate)```

This function takes two parameters, ```startDate``` and ```endDate```, both ISO8601 date strings, and returns a promise that resolves the following type:

```
{
    startTime: ISO8601String
    endTime: ISO8601String
    steps: Integer
    distance?: Float
    averageActivePace?: Float
    currentPace?: Float
    currentCadence?: Float
}
```

The last four optional fields are only provided by iOS, if the device being used supports them. At some point I'd like to support these in Android as well, but that's for some point in the future.

```startPedometerUpdatesFromDate(startDate)```

Takes a single parameter, ```startDate``` as a ISO8601 date string, and 

## Acknowledgments

This module would not have been possible without the various other React Native Healthkit and Google Fit bridges available. In particular:

- [react-native-pedometer](https://github.com/mathieudutour/react-native-pedometer) by [mathieudutour](https://github.com/mathieudutour)

   For the initial inspiration that made me think creating my own pedometer bridge was possible.

- [react-native-google-fitness](https://github.com/YBRAIN/react-native-google-fitness/) by [YBRAIN Inc.](https://github.com/YBRAIN)

   In particular, the GoogleSignInManager. The cleanest and easiest to understand method I've seen for signing in to Google services. Thank you!
