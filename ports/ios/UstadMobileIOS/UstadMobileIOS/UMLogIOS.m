//
//  UMLogIOS.m
//  UstadMobileIOS
//
//  Created by Mike Dawson on 04/08/16.
//  Copyright © 2016 UstadMobile FZ-LLC. All rights reserved.
//

#import "UMLogIOS.h"
#import "java/lang/Exception.h"

@implementation UMLogIOS

- (void)lWithInt:(jint)level
         withInt:(jint)code
    withNSString:(NSString *)message {
    
    [self lWithInt:level withInt:code withNSString:message withJavaLangException:nil];
    
}

- (void)lWithInt:(jint)level
         withInt:(jint)code
    withNSString:(NSString *)message
withJavaLangException:(JavaLangException *)exception {
    
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
    
    if(exception) {
        [output appendString:[exception description]];
    }
    
    NSLog(output);

}


@end
