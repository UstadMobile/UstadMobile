//
//  UstadMobileSystemImplFactoryIOS.h
//  TranspileCompileTest
//
//  Created by Mike Dawson on 03/08/16.
//  Copyright © 2016 Mike Dawson. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "UstadMobileSystemImplFactory.h"
#import "UstadMobileSystemImpl.h"

@interface UstadMobileSystemImplFactoryIOS : ComUstadmobileCoreImplUstadMobileSystemImplFactory

- (instancetype)init;

- (ComUstadmobileCoreImplUstadMobileSystemImpl *)makeUstadSystemImpl;

@end
