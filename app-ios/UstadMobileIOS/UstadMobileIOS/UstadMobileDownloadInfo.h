//
//  UstadMobileDownloadInfo.h
//  UstadMobileIOS
//
//  Created by Mike Dawson on 24/08/16.
//  Copyright © 2016 UstadMobile FZ-LLC. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface UstadMobileDownloadInfo : NSObject
@property unsigned long taskIdentifier;
@property NSString *destinationURI;
@property NSURLSessionDownloadTask *downloadTask;

@property int64_t totalBytesWritten;
@property int64_t totalBytesExpected;

-(id)initWithDestURI:(NSString *)destinationURI;

@end
