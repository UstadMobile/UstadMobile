//
//  UstadMobileDownloadInfo.m
//  UstadMobileIOS
//
//  Created by Mike Dawson on 24/08/16.
//  Copyright © 2016 UstadMobile FZ-LLC. All rights reserved.
//

#import "UstadMobileDownloadInfo.h"

@implementation UstadMobileDownloadInfo

-(id)initWithDestURI:(NSString *)destinationURI {
    self = [super init];
    self.destinationURI = destinationURI;
    self.totalBytesWritten = 0;
    self.totalBytesExpected = NSURLSessionTransferSizeUnknown;
    
    return self;
}

@end
