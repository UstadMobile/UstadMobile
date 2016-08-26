//
//  UstadMobileSystemImplIOS.h
//  UstadMobileIOS
//
//  Created by Mike Dawson on 03/08/16.
//  Copyright © 2016 UstadMobile FZ-LLC. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "UstadMobileSystemImplSE.h"
#import "EmbeddedHTTPD.h"

@interface UstadMobileSystemImplIOS : ComUstadmobilePortSharedseImplUstadMobileSystemImplSE<NSURLSessionDelegate>

-(ComUstadmobilePortSharedseImplHttpEmbeddedHTTPD *)getHTTPD;

@end
