
package com.wwdrew.reactnative;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;

public class RNDualPedometerModule extends ReactContextBaseJavaModule {

  private final ReactApplicationContext reactContext;

  public RNDualPedometerModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;
  }

  @Override
  public String getName() {
    return "RNDualPedometer";
  }
}