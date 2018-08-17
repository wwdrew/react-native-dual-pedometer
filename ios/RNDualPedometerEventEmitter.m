#import "RNDualPedometerEventEmitter.h"

// import RCTBridge
#if __has_include(<React/RCTBridge.h>)
#import <React/RCTBridge.h>
#elif __has_include(“RCTBridge.h”)
#import “RCTBridge.h”
#else
#import “React/RCTBridge.h” // Required when used as a Pod in a Swift project
#endif

// import RCTEventDispatcher
#if __has_include(<React/RCTEventDispatcher.h>)
#import <React/RCTEventDispatcher.h>
#elif __has_include(“RCTEventDispatcher.h”)
#import “RCTEventDispatcher.h”
#else
#import “React/RCTEventDispatcher.h” // Required when used as a Pod in a Swift project
#endif

// Event Names
NSString *const rnPedometerUpdate = @"pedometer:update";

@interface RNDualPedometerEventEmitter ()
@end

@implementation RNDualPedometerEventEmitter {
    bool hasListeners;
}

RCT_EXPORT_MODULE();

+ (BOOL)requiresMainQueueSetup
{
    return YES;
}

- (NSDictionary<NSString *, NSString *> *)constantsToExport {
    return @{
             @"constants": @{
                     @"PEDOMETER_UPDATE": rnPedometerUpdate,
                     }
             };
}

- (NSArray<NSString *> *)supportedEvents
{
    return @[rnPedometerUpdate];
}

+ (void)pedometerUpdate:(NSDictionary *)pedometerData
{
    [self postNotificationName:rnPedometerUpdate withPayload:pedometerData];
}

#pragma mark - Private methods

+ (void)postNotificationName:(NSString *)name withPayload:(NSDictionary *)payload {
    [[NSNotificationCenter defaultCenter] postNotificationName:name
                                                        object:self
                                                      userInfo:payload];
}

- (void)startObserving {
    for (NSString *event in [self supportedEvents]) {
        [[NSNotificationCenter defaultCenter] addObserver:self
                                                 selector:@selector(handleNotification:)
                                                     name:event
                                                   object:nil];
    }
}

- (void)stopObserving {
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}

- (void)handleNotification:(NSNotification *)notification {
    [self sendEventWithName:notification.name body:notification.userInfo];
}

@end
