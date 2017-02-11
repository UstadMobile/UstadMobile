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
#import "java/net/URLConnection.h"
#import "java/net/URL.h"
#import "UMLogIOS.h"
#import "AppViewIOS.h"
#import "java/lang/System.h"
#import "UstadBaseUIViewController.h"
#import "CatalogView.h"
#import "BasePointView.h"
#import "ContainerView.h"
#import <MobileCoreServices/MobileCoreServices.h>
#import "UstadMobileDownloadInfo.h"
#import "UMDownloadCompleteEvent.h"
#import "UMDownloadCompleteReceiver.h"
#import "EmbeddedHTTPD.h"
#import "UstadViewControllerWithArgs.h"
#import "NanoLrsHttpd.h"
#include "J2ObjC_source.h"



static NSString *_defaultsKeyAppPrefs;
static NSString *_defaultsKeyUserPrefix;
static NSString *_defaultsKeyActiveUser;
static NSString *_defaultsKeyActiveUserAuth;

@interface UstadMobileSystemImplIOS()
//Implementation of UMLog
@property UMLogIOS *umLogIOS;

//Holds both user and app defaults
//app preferences are held as umapp-PROPERTYNAME ; user preferences as umuser-USERNAME in a dictionary
@property NSUserDefaults *userDefaults;
@property NSString *activeUser;
@property NSString *activeUserAuth;

//Link UIViewControllers to their associated appView
@property NSMapTable *appViewTable;

//URL Session configuration for background downloading
@property NSURLSession *urlSession;

@property NSMapTable *downloadInfoTable;

@property NSMutableArray *downloadCompleteListeners;

@property ComUstadmobilePortSharedseImplHttpEmbeddedHTTPD *httpd;

@end


@implementation UstadMobileSystemImplIOS

NSString *PROTOCOL_UM_IOS_FILEURI = @"umiosappdir:///";

+(void)initialize {
    if(self == [UstadMobileSystemImplIOS class]) {
        _defaultsKeyAppPrefs = @"umapp-prefs";
        _defaultsKeyUserPrefix = @"umuser-prefs-";
        _defaultsKeyActiveUser = @"umapp-active-user";
        _defaultsKeyActiveUserAuth = @"umapp-active-user-auth";
    }
}

-(id)init {
    self = [super init];
    //[JavaLangSystem setPropertyWithNSString:@"http.keepAlive" withNSString:@"false"];
    self.umLogIOS = [[UMLogIOS alloc]init];
    self.userDefaults = [NSUserDefaults standardUserDefaults];
    self.appViewTable = [NSMapTable weakToStrongObjectsMapTable];
    
    NSURLSessionConfiguration *sessionConfig = [NSURLSessionConfiguration backgroundSessionConfigurationWithIdentifier:@"com.ustadmobile.ios"];
    sessionConfig.HTTPMaximumConnectionsPerHost = 5;
    self.urlSession = [NSURLSession sessionWithConfiguration:sessionConfig delegate:self delegateQueue:nil];
    self.downloadInfoTable = [NSMapTable strongToStrongObjectsMapTable];
    self.downloadCompleteListeners = [[NSMutableArray alloc]init];
    self.httpd = [[ComUstadmobilePortSharedseImplHttpEmbeddedHTTPD alloc]initWithInt:8071];
    [ComUstadmobileNanolrsHttpNanoLrsHttpd mountXapiEndpointsOnServerWithFiIkiElonenRouterRouterNanoHTTPD:self.httpd withId:self withNSString:@"/xapi/"];
    [self.httpd start];
    return self;
}

- (NSString *)resolveFileUriToPathWithNSString:(NSString *)fileUri {
    if([fileUri hasPrefix:PROTOCOL_UM_IOS_FILEURI]) {
        NSString *dirPath = [NSSearchPathForDirectoriesInDomains(NSLibraryDirectory, NSUserDomainMask, YES) objectAtIndex:0];
        NSString *fileUriPathComp = [fileUri substringFromIndex:[PROTOCOL_UM_IOS_FILEURI length]];
        return [dirPath stringByAppendingPathComponent:fileUriPathComp];
    }
    
    return fileUri;
}


- (ComUstadmobileCoreImplUMLog *)getLogger {
    return self.umLogIOS;
}

- (JavaIoInputStream *)openResourceInputStreamWithNSString:(NSString *)resURI
                                                    withId:(id)context {
    NSRange slashResult = [resURI rangeOfString:@"/" options:NSBackwardsSearch];
    NSString *dirPath = [resURI substringWithRange:NSMakeRange(0, slashResult.location)];
    dirPath = [@"/res/" stringByAppendingString:dirPath];
    NSString *fileName = [resURI substringFromIndex:(slashResult.location+1)];
    NSString *resPath = [[NSBundle mainBundle] pathForResource:fileName ofType:nil inDirectory:dirPath];
    return [[JavaIoFileInputStream alloc]initWithNSString:resPath];
}

- (NSString *)getSharedContentDir {
    return PROTOCOL_UM_IOS_FILEURI;
}

- (NSString *)getSystemBaseDir {
    return PROTOCOL_UM_IOS_FILEURI;
}

- (JavaNetURLConnection *)openConnectionWithJavaNetURL:(JavaNetURL *)url {
    return [url openConnection];
}


- (NSString *)getUserContentDirectoryWithNSString:(NSString *)username {
    // can't call an abstract method
    NSString *sysDir = [self getSystemBaseDir];
    NSString *userPathComp = [@"user-" stringByAppendingString:username];
    return [NSString pathWithComponents:[NSArray arrayWithObjects:sysDir, userPathComp, nil]];
}

- (NSString *)getPrefFromDict:(NSString *)dictionaryKey
                  withPrefKey:(NSString *)prefKey {
    NSDictionary *dict = [self.userDefaults dictionaryForKey:dictionaryKey];
    if(dict) {
        return dict[prefKey];
    }else {
        return nil;
    }
}

- (void)setPrefInDict:(NSString *)dictionaryKey
                withPrefKey:(NSString *)prefKey
                withValue:(NSString *)prefVal
                withSynchronize:(BOOL)autoSync{
    NSDictionary *dict = [self.userDefaults dictionaryForKey:dictionaryKey];
    NSMutableDictionary *mutableDict;
    if(dict) {
        mutableDict = [NSMutableDictionary dictionaryWithDictionary:dict];
    }else {
        mutableDict = [[NSMutableDictionary alloc]init];
    }
    
    if(prefVal != nil) {
        [mutableDict setObject:prefVal forKey:prefKey];
    }else {
        [mutableDict removeObjectForKey:prefKey];
    }
    
    [self.userDefaults setObject:[mutableDict copy] forKey:dictionaryKey];
    if(autoSync) {
        [self.userDefaults synchronize];
    }
}


- (NSString *)getAppPrefWithNSString:(NSString *)key
                              withId:(id)context {
    return [self getPrefFromDict:_defaultsKeyAppPrefs withPrefKey:key];
}

- (void)setAppPrefWithNSString:(NSString *)key
                  withNSString:(NSString *)value
                        withId:(id)context {
    [self setPrefInDict:_defaultsKeyAppPrefs withPrefKey:key withValue:value withSynchronize:YES];
}

-(void)setUserPrefWithNSString:(NSString *)key withNSString:(NSString *)value withId:(id)context {
    NSString *userPrefKey = [_defaultsKeyUserPrefix stringByAppendingString:self.activeUser];
    [self setPrefInDict:userPrefKey withPrefKey:key withValue:value withSynchronize:NO];
}

-(NSString *)getUserPrefWithNSString:(NSString *)key withId:(id)context {
    NSString *userPrefKey = [_defaultsKeyUserPrefix stringByAppendingString:self.activeUser];
    return [self getPrefFromDict:userPrefKey withPrefKey:key];
}

-(void)saveUserPrefsWithId:(id)context {
    [self.userDefaults synchronize];
}

- (IOSObjectArray *)getAppPrefKeyListWithId:(id)context {
    NSDictionary *appPrefDict = [self.userDefaults dictionaryForKey:_defaultsKeyAppPrefs];
    IOSObjectArray *arr = [IOSObjectArray arrayWithNSArray:[appPrefDict allKeys] type:NSString_class_()];
    return arr;
}

-(void)setActiveUserWithNSString:(NSString *)username withId:(id)context {
    self.activeUser = username;
    [self.userDefaults setObject:username forKey:_defaultsKeyActiveUser];
    [self.userDefaults synchronize];
    [super setActiveUserWithNSString:username withId:context];
}

-(NSString *)getActiveUserWithId:(id)context {
    return self.activeUser;
}

-(void)setActiveUserAuthWithNSString:(NSString *)password withId:(id)context {
    self.activeUserAuth = password;
    [self.userDefaults setObject:password forKey:_defaultsKeyActiveUserAuth];
    [self.userDefaults synchronize];
}

-(NSString *)getActiveUserAuthWithId:(id)context {
    return self.activeUserAuth;
}

-(jboolean)loadActiveUserInfoWithId:(id)context {
    self.activeUser = [self.userDefaults objectForKey:_defaultsKeyActiveUser];
    self.activeUserAuth = [self.userDefaults objectForKey:_defaultsKeyActiveUserAuth];
    return true;
}
- (jlong)getBuildTime {
    return 0;
}

- (NSString *)getVersionWithId:(id)context {
    return [NSString stringWithFormat:@"Version %@",[[NSBundle mainBundle] objectForInfoDictionaryKey:@"CFBundleShortVersionString"]];
}

- (id<ComUstadmobileCoreViewAppView>)getAppViewWithId:(id)context {
    AppViewIOS *appView = [self.appViewTable objectForKey:context];
    if(!appView) {
        appView = [[AppViewIOS alloc]initWithViewController:context];
        [self.appViewTable setObject:appView forKey:context];
    }
    
    return appView;
}

- (void)goWithIOSClass:(IOSClass *)cls
 withJavaUtilHashtable:(JavaUtilHashtable *)args
                withId:(id)context {
    
    UIViewController *nextVC = nil;
    UIViewController *currentVC = (UIViewController *)context;
    UIStoryboard *sb = [UIStoryboard storyboardWithName:@"Main" bundle:nil];
    if ([cls isEqual:ComUstadmobileCoreViewCatalogView_class_()]) {
        nextVC = [sb instantiateViewControllerWithIdentifier:@"CatalogViewController"];
    }else if([cls isEqual:ComUstadmobileCoreViewBasePointView_class_()]) {
        nextVC = [sb instantiateViewControllerWithIdentifier:@"BasePointViewController"];
    }else if([cls isEqual:ComUstadmobileCoreViewContainerView_class_()]) {
        nextVC = [sb instantiateViewControllerWithIdentifier:@"ContainerViewController"];
    }
    
    UIViewController *parentVC = currentVC.parentViewController;
    while(!([parentVC isKindOfClass:[UINavigationController class]]) && parentVC != nil) {
        parentVC = parentVC.parentViewController;
    }
    
    
    
    if(nextVC != nil && [nextVC conformsToProtocol:@protocol(UstadViewControllerWithArgs)]) {
        id<UstadViewControllerWithArgs> baseVC = (id<UstadViewControllerWithArgs>)nextVC;
        [baseVC setArgumentsWithHashtable:args];
    }
    
    dispatch_async(dispatch_get_main_queue(), ^{
        if(nextVC) {
            UINavigationController *navCtrl = (UINavigationController *)parentVC;
            [navCtrl pushViewController:nextVC animated:YES];
        }
    });
}

- (NSString *)getExtensionFromMimeTypeWithNSString:(NSString *)mimeType {
    CFStringRef cfMimeType = (__bridge CFStringRef)mimeType;
    CFStringRef uti = UTTypeCreatePreferredIdentifierForTag(kUTTagClassMIMEType, cfMimeType, NULL);
    CFStringRef ext = UTTypeCopyPreferredTagWithClass(uti, kUTTagClassFilenameExtension);
    return (__bridge NSString*)ext;
}

- (NSString *)getUMProfileName {
    return @"iOS";
}

-(ComUstadmobilePortSharedseImplHttpEmbeddedHTTPD *)getHTTPD {
    return self.httpd;
}


- (NSString *)queueFileDownloadWithNSString:(NSString *)url
                               withNSString:(NSString *)fileURI
                      withJavaUtilHashtable:(JavaUtilHashtable *)headers
                                     withId:(id)context {
    url = [url stringByReplacingOccurrencesOfString:@" " withString:@"%20"];
    UstadMobileDownloadInfo *downloadInfo = [[UstadMobileDownloadInfo alloc] initWithDestURI:fileURI];
    downloadInfo.downloadTask = [self.urlSession downloadTaskWithURL:[NSURL URLWithString:url]];
    [self.downloadInfoTable setObject:downloadInfo forKey:[NSNumber numberWithUnsignedLong:downloadInfo.downloadTask.taskIdentifier]];
    [downloadInfo.downloadTask resume];
    return [NSString stringWithFormat:@"%@", @(downloadInfo.downloadTask.taskIdentifier)];
}

- (IOSIntArray *)getFileDownloadStatusWithNSString:(NSString *)downloadID
                                            withId:(id)context {
    NSNumberFormatter *formatter = [[NSNumberFormatter alloc]init];
    formatter.numberStyle = NSNumberFormatterDecimalStyle;
    NSNumber *downloadIdNum = [formatter numberFromString:downloadID];
    
    UstadMobileDownloadInfo *info = [self.downloadInfoTable objectForKey:downloadIdNum];
    IOSIntArray *result;
    if(info != nil) {
        result = [IOSIntArray arrayWithLength:3];
        *IOSIntArray_GetRef(result, ComUstadmobileCoreImplUstadMobileSystemImpl_IDX_DOWNLOADED_SO_FAR) = (int)info.totalBytesWritten;
        if(info.totalBytesExpected != NSURLSessionTransferSizeUnknown) {
            *IOSIntArray_GetRef(result, ComUstadmobileCoreImplUstadMobileSystemImpl_IDX_BYTES_TOTAL) = (int)info.totalBytesExpected;
        }else {
            *IOSIntArray_GetRef(result, ComUstadmobileCoreImplUstadMobileSystemImpl_IDX_BYTES_TOTAL) = -1;
        }
        
        *IOSIntArray_GetRef(result, ComUstadmobileCoreImplUstadMobileSystemImpl_IDX_STATUS) = ComUstadmobileCoreImplUstadMobileSystemImpl_DLSTATUS_RUNNING;
    }else {
        result = [IOSIntArray arrayWithInts:(jint[]){0, -1, 0} count:3];
    }
    
    return result;
}

- (void)registerDownloadCompleteReceiverWithComUstadmobileCoreImplUMDownloadCompleteReceiver:(id<ComUstadmobileCoreImplUMDownloadCompleteReceiver>)receiver
                                                                                      withId:(id)context {
    [self.downloadCompleteListeners addObject:receiver];
}

- (void)unregisterDownloadCompleteReceiverWithComUstadmobileCoreImplUMDownloadCompleteReceiver:(id<ComUstadmobileCoreImplUMDownloadCompleteReceiver>)receiver
                                                                                        withId:(id)context {
    [self.downloadCompleteListeners removeObject:receiver];
}

- (void)URLSessionDidFinishEventsForBackgroundURLSession:(NSURLSession *)session {
    
}

-(void)URLSession:(NSURLSession *)session downloadTask:(NSURLSessionDownloadTask *)downloadTask didFinishDownloadingToURL:(NSURL *)location{
    NSNumber *taskId = [NSNumber numberWithUnsignedLong:downloadTask.taskIdentifier];
    UstadMobileDownloadInfo *info = [self.downloadInfoTable objectForKey:taskId];
    if(info != nil) {
        //move me to the destination file
        NSFileManager *fileManager = [NSFileManager defaultManager];
        NSURL *destURL = [NSURL fileURLWithPath:[self resolveFileUriToPathWithNSString:info.destinationURI]];
        BOOL success = [fileManager copyItemAtURL:location toURL:destURL error:nil];
        IOSIntArray *statusArr = [IOSIntArray arrayWithInts:(jint[]){(jint)downloadTask.countOfBytesReceived,
            (jint)downloadTask.countOfBytesExpectedToReceive, ComUstadmobileCoreImplUstadMobileSystemImpl_DLSTATUS_SUCCESSFUL
        } count:3];
        ComUstadmobileCoreImplUMDownloadCompleteEvent *evt = [[ComUstadmobileCoreImplUMDownloadCompleteEvent alloc]initWithNSString:[NSString stringWithFormat:@"%@", taskId] withIntArray:statusArr];
        for(id listener in self.downloadCompleteListeners) {
            [listener downloadStatusUpdatedWithComUstadmobileCoreImplUMDownloadCompleteEvent:evt];
        }
    }
}


-(void)URLSession:(NSURLSession *)session downloadTask:(nonnull NSURLSessionDownloadTask *)downloadTask didWriteData:(int64_t)bytesWritten totalBytesWritten:(int64_t)totalBytesWritten totalBytesExpectedToWrite:(int64_t)totalBytesExpectedToWrite {
    NSNumber *taskId = [NSNumber numberWithUnsignedLong:downloadTask.taskIdentifier];
    UstadMobileDownloadInfo *downloadInfo = [self.downloadInfoTable objectForKey:taskId];
    if(downloadInfo != nil) {
        downloadInfo.totalBytesExpected = totalBytesExpectedToWrite;
        downloadInfo.totalBytesWritten = totalBytesWritten;
    }
    
    
    if(totalBytesExpectedToWrite == NSURLSessionTransferSizeUnknown) {
        NSLog(@"Transfer size not yet known");
    }else {
        NSLog([NSString stringWithFormat:@"Downloaded %d / %d", (int)totalBytesWritten, (int)totalBytesExpectedToWrite]);
    }
    
}

- (NSString *)getBasePointDefaultCatalogURL {
    return @"http://www.ustadmobile.com/files/sapienza/opds/root.opds";
}

- (NSString *)getBasePointBrowseURL {
    return nil;
}


@end
