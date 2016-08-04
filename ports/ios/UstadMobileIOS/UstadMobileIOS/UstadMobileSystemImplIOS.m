//
//  UstadMobileSystemImplIOS.m
//  UstadMobileIOS
//
//  Created by Mike Dawson on 03/08/16.
//  Copyright © 2016 UstadMobile FZ-LLC. All rights reserved.
//

#import "UstadMobileSystemImplIOS.h"
#import "java/io/InputStream.h"
#import "java/io/FileInputStream.h"

@implementation UstadMobileSystemImplIOS

- (JavaIoInputStream *)openResourceInputStreamWithNSString:(NSString *)resURI
                                                    withId:(id)context {
    NSRange slashResult = [resURI rangeOfString:@"/" options:NSBackwardsSearch];
    NSString *dirPath = [resURI substringWithRange:NSMakeRange(0, slashResult.location)];
    NSString *fileName = [resURI substringFromIndex:(slashResult.location+1)];
    NSString *resPath = [[NSBundle mainBundle] pathForResource:fileName ofType:nil inDirectory:dirPath];
    return [[JavaIoFileInputStream alloc]initWithNSString:resPath];
}

@end
