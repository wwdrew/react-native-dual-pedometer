#import "RNDualPedometer.h"
#import "RNDualPedometerEventEmitter.h"
#import <CoreMotion/CoreMotion.h>
#import "RCTConvert.h"

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

@implementation RNDualPedometer

@synthesize bridge = _bridge;

RCT_EXPORT_MODULE();

+ (BOOL)requiresMainQueueSetup
{
    return YES;
}

RCT_REMAP_METHOD(queryPedometerFromDate,
                 startTime:      (NSString *)startTime
                 endTime:        (NSString *)endTime
                 queryResolver:  (RCTPromiseResolveBlock)resolve
                 queryRejecter:  (RCTPromiseRejectBlock)reject)
{
    [self queryPedometerFromDate:startTime endTime:endTime queryResolver:resolve queryRejecter:reject];
}

RCT_REMAP_METHOD(startPedometerUpdatesFromDate,
                 startTime:       (NSString *)startTime
                 eventsResolver:  (RCTPromiseResolveBlock)resolve
                 eventsRejecter:  (RCTPromiseRejectBlock)reject)
{
    NSLog(@"RNDualPedometer - Start Pedometer Updates From Date - Start Time: %@", startTime);
    
    [self startPedometerUpdatesFromDate:startTime];
    resolve(@(YES));
}

RCT_REMAP_METHOD(stopPedometerUpdates,
                 eventsResolver:  (RCTPromiseResolveBlock)resolve
                 eventsRejecter:  (RCTPromiseRejectBlock)reject)
{
    [self stopPedometerUpdates];
    resolve(@(YES));
}

- (void) queryPedometerFromDate:(NSString *)startTime endTime:(NSString *)endTime queryResolver:(RCTPromiseResolveBlock)resolve queryRejecter:(RCTPromiseRejectBlock)reject
{
    NSLog(@"query pedometer start date: %@", startTime);
    NSLog(@"query pedometer end date: %@", endTime);
    
#if TARGET_IPHONE_SIMULATOR
    NSLog(@"Running in Simulator");
    resolve([self simulatorPedometerData:startTime endTime:endTime]);
#else
    NSLog(@"Running on device");
    
    NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
    [dateFormatter setDateFormat:@"yyyy-MM-dd'T'HH:mm:ssZ"];

    dispatch_async(dispatch_get_main_queue(), ^{
        [self.pedometer queryPedometerDataFromDate:[dateFormatter dateFromString:startTime]
                                            toDate:[dateFormatter dateFromString:endTime]
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

- (void) startPedometerUpdatesFromDate:(NSString *)startTime
{
#if TARGET_IPHONE_SIMULATOR
    NSLog(@"RNDualPedometer - Running on simulator, generating simulated results");
    [RNDualPedometerEventEmitter pedometerUpdate:[self simulatorPedometerData:startTime endTime:nil]];
#else
    NSLog(@"RNDualPedometer - Start Pedometer Updates From Date Function - Start Time: %@", startTime);

    NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
    [dateFormatter setDateFormat:@"yyyy-MM-dd'T'HH:mm:ssZ"];

    [self.pedometer startPedometerUpdatesFromDate:[dateFormatter dateFromString:startTime]
                                      withHandler:^(CMPedometerData *pedometerData, NSError *error) {
                                          [RNDualPedometerEventEmitter pedometerUpdate:[self devicePedometerData:pedometerData]];
                                      }];
#endif
}

- (void) stopPedometerUpdates
{
#if TARGET_IPHONE_SIMULATOR
    NSLog(@"RNDualPedometer - Running on simulator, no need to stop updates");
#else
    [self.pedometer stopPedometerUpdates];
#endif
}

- (NSDictionary *) simulatorPedometerData:(NSDate *)startTime endTime:(NSDate *)endTime
{
    if (endTime == nil) {
        endTime = [NSDate date];
    }

    return @{
             @"startTime": [self getISO8601FromDate:startTime],
             @"endTime": [self getISO8601FromDate:endTime],
             @"steps": @(123456),
             };
}

- (NSDictionary *) devicePedometerData:(CMPedometerData *)data
{
    return @{
             @"startTime": [self getISO8601FromDate:data.startDate],
             @"endTime": [self getISO8601FromDate:data.endDate],
             @"steps": data.numberOfSteps?:[NSNull null],
             @"distance": data.distance?:[NSNull null],
             @"averageActivePace": data.averageActivePace?:[NSNull null],
             @"currentPace": data.currentPace?:[NSNumber numberWithInt:0],
             @"currentCadence": data.currentCadence?:[NSNumber numberWithInt:0],
             };
}

#pragma mark - Private methods

- (NSString *)getISO8601FromDate:(NSDate *)date
{
    NSDateFormatter *formatter = [[NSDateFormatter alloc] init];
    [formatter setDateFormat:@"yyyy-MM-dd'T'HH:mm:ss.SSSZZZZZ"];
    [formatter setLocale:[[NSLocale alloc] initWithLocaleIdentifier:@"en_US_POSIX"]];
    [formatter setTimeZone:[[NSTimeZone alloc] initWithName:@"UTC"]];
    
    return [formatter stringFromDate:date];
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
