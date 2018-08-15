
# react-native-dual-pedometer

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

#### Additional iOS Installation

Add an entry to your `Info.plist`:

### Using XCode

Select your `Info.plist` file from your application files and add a new entry:

- Property: `Privacy - Motion Usage Description`
- Value: (reason for requiring Pedometer permissions)

### Editing Info.plist manually

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

4. 

## Usage
```javascript
import RNDualPedometer from 'react-native-dual-pedometer';

// TODO: What to do with the module?
RNDualPedometer;
```
  