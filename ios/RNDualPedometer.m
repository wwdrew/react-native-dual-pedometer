#import "RNDualPedometer.h"
#import <CoreMotion/CoreMotion.h>

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

@interface RNDualPedometer ()
@property (nonatomic, readonly) CMPedometer *pedometer;
@end

@implementation RNDualPedometer {
    bool hasListeners;
}

@synthesize bridge = _bridge;

RCT_EXPORT_MODULE();

RCT_REMAP_METHOD(queryPedometerFromDate,
                 startTime:      (NSDate *)startTime
                 endTime:        (NSDate *)endTime
                 queryResolver:  (RCTPromiseResolveBlock)resolve
                 queryRejecter:  (RCTPromiseRejectBlock)reject)
{
    [self queryPedometerFromDate:startTime endTime:endTime queryResolver:resolve queryRejecter:reject];
}

RCT_REMAP_METHOD(startPedometerUpdatesFromDate,
                 startTime:       (NSDate *)startTime
                 eventsResolver:  (RCTPromiseResolveBlock)resolve
                 eventsRejecter:  (RCTPromiseRejectBlock)reject)
{
    [self startPedometerUpdatesFromDate:startTime];
    resolve(@(YES));
}

- (NSArray<NSString *> *)supportedEvents
{
    return @[@"pedometer:update"];
}

- (void) queryPedometerFromDate:(NSDate *)startTime endTime:(NSDate *)endTime queryResolver:(RCTPromiseResolveBlock)resolve queryRejecter:(RCTPromiseRejectBlock)reject
{
    NSLog(@"query pedometer start date: %@", startTime);
    NSLog(@"query pedometer end date: %@", endTime);
    
#if TARGET_IPHONE_SIMULATOR
    NSLog(@"Running in Simulator");
    resolve([self simulatorPedometerData:startTime endTime:endTime]);
#else
    NSLog(@"Running on device");
    dispatch_async(dispatch_get_main_queue(), ^{
        [self.pedometer queryPedometerDataFromDate:startTime
                                            toDate:endTime
                                       withHandler:^(CMPedometerData *pedometerData, NSError *error) {
                                           if (!error) {
                                               resolve([self devicePedometerData:pedometerData]);
                                           } else {
                                               reject(@"failure", @"There was a failure", error);
                                           }
                                       }];
    });
#endif
}

- (void) startPedometerUpdatesFromDate:(NSDate *)startTime
{
    dispatch_async(dispatch_get_main_queue(), ^{
        [self.pedometer startPedometerUpdatesFromDate:startTime
                                          withHandler:^(CMPedometerData *pedometerData, NSError *error) {
                                              dispatch_async(dispatch_get_main_queue(), ^{
                                                  [self emitMessageToRN:@"pedometer:update" :[self devicePedometerData:pedometerData]];
                                              });
                                          }];
    });
}

- (NSDictionary *) simulatorPedometerData:(NSDate *)startTime endTime:(NSDate *)endTime {
    
    return @{
             @"startTime": @([startTime timeIntervalSince1970]),
             @"endTime": @([endTime timeIntervalSince1970]),
             @"value": @(123456),
             };
}

- (NSDictionary *) devicePedometerData:(CMPedometerData *)data {
    
    return @{
             @"startTime": @([data.startDate timeIntervalSince1970]),
             @"endTime": @([data.endDate timeIntervalSince1970]),
             @"value": data.numberOfSteps?:[NSNull null],
             };
}

#pragma mark - Private methods

// Will be called when this module's first listener is added.
- (void) startObserving {
    hasListeners = YES;
}

// Will be called when this module's last listener is removed, or on dealloc.
- (void) stopObserving {
    hasListeners = NO;
}

- (instancetype) init
{
    self = [super init];
    if (self == nil) {
        return nil;
    }
    
    _pedometer = [[CMPedometer alloc] init];
    
    return self;
}

@end
