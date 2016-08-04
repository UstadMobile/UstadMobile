//
//  UMLogIOS.m
//  UstadMobileIOS
//
//  Created by Mike Dawson on 04/08/16.
//  Copyright © 2016 UstadMobile FZ-LLC. All rights reserved.
//

#import "UMLogIOS.h"

@implementation UMLogIOS

- (void)lWithInt:(jint)level
         withInt:(jint)code
    withNSString:(NSString *)message {
    NSMutableString *output = [[NSMutableString alloc]init];
    switch(level) {
        case ComUstadmobileCoreImplUMLog_INFO:
            [output appendString:@"[INFO] "];
            break;
        case ComUstadmobileCoreImplUMLog_WARN:
            [output appendString:@"[WARN] "];
            break;
        case ComUstadmobileCoreImplUMLog_DEBUG:
            [output appendString:@"[DEBUG] "];
            break;
        case ComUstadmobileCoreImplUMLog_VERBOSE:
            [output appendString:@"[VERBOSE]"];
            break;
        case ComUstadmobileCoreImplUMLog_CRITICAL:
            [output appendString:@"[CRITICAL] "];
            break;
    }
    [output appendString:@"Code: "];
    [output appendString:[NSString stringWithFormat:@"%d", code]];
    if(message) {
        [output appendString:message];
    }
    [output appendString:@" "];
    NSLog(output);
}

- (void)lWithInt:(jint)level
         withInt:(jint)code
    withNSString:(NSString *)message
withJavaLangException:(JavaLangException *)exception {
    
}


@end
