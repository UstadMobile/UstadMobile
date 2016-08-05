//
//  UstadMobileIOSTests.m
//  UstadMobileIOSTests
//
//  Created by Mike Dawson on 03/08/16.
//  Copyright © 2016 UstadMobile FZ-LLC. All rights reserved.
//

#import <XCTest/XCTest.h>
#include "UstadMobileSystemImpl.h"
#include "MessageIDConstants.h"

@interface UstadMobileIOSTests : XCTestCase
@property ComUstadmobileCoreImplUstadMobileSystemImpl *impl;
@end

@implementation UstadMobileIOSTests

- (void)setUp {
    [super setUp];
    self.impl = [ComUstadmobileCoreImplUstadMobileSystemImpl getInstance];
}

- (void)tearDown {
    // Put teardown code here. This method is called after the invocation of each test method in the class.
    [super tearDown];
}

-(void)testImplPreferences {
    NSString *key1 = @"HiIOS";
    NSString *val1 = @"Val1";
    NSObject *context = self;
    
    NSString *testUsername = @"testuser";
    
    [self.impl setAppPrefWithNSString:key1 withNSString:val1 withId:self];
    
    NSString *retVal = [self.impl getAppPrefWithNSString:key1 withId:self];
    XCTAssertTrue([val1 isEqualToString:retVal]);
    
    [self.impl setActiveUserWithNSString:testUsername withId:self];
    
    [self.impl setUserPrefWithNSString:key1 withNSString:val1 withId:context];
    NSString *userRetVal = [self.impl getUserPrefWithNSString:key1 withId:context];
    XCTAssertTrue([val1 isEqualToString:userRetVal]);
    
    [self.impl setActiveUserWithNSString:[testUsername stringByAppendingString:@"other"] withId:context];
    NSString *otherUserVal = [self.impl getUserPrefWithNSString:key1 withId:context];
    XCTAssertNil(otherUserVal);
    
    jboolean userLoaded = [self.impl loadActiveUserInfoWithId:context];
    XCTAssertTrue(userLoaded);
    
}

- (void)testmplCanInit {
    [self.impl init__WithId:self];
    NSString *loginText = [self.impl getStringWithInt:ComUstadmobileCoreMessageIDConstants_login];
    XCTAssertNotNil(loginText);
}

- (void)testExample {
    // This is an example of a functional test case.
    // Use XCTAssert and related functions to verify your tests produce the correct results.
}

- (void)testPerformanceExample {
    // This is an example of a performance test case.
    [self measureBlock:^{
        // Put the code you want to measure the time of here.
    }];
}

@end
