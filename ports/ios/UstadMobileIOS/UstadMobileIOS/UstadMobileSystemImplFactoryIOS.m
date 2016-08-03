//
//  UstadMobileSystemImplFactoryIOS.m
//  TranspileCompileTest
//
//  Created by Mike Dawson on 03/08/16.
//  Copyright © 2016 Mike Dawson. All rights reserved.
//

#import "UstadMobileSystemImplFactoryIOS.h"
#import "UstadMobileSystemImpl.h"
#import "UstadMobileSystemImplIOS.h"

@implementation UstadMobileSystemImplFactoryIOS
- (ComUstadmobileCoreImplUstadMobileSystemImpl *)makeUstadSystemImpl {
    return [[UstadMobileSystemImplIOS alloc] init];
}
@end
