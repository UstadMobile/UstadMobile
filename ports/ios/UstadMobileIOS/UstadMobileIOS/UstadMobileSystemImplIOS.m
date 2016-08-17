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
#import "UMLogIOS.h"
#import "AppViewIOS.h"
#import "java/lang/System.h"
#import "UstadBaseUIViewController.h"
#import "CatalogView.h"
#import "BasePointView.h"
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
@end


@implementation UstadMobileSystemImplIOS

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
    
    return self;
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
    NSString *dirPath = [NSSearchPathForDirectoriesInDomains(NSLibraryDirectory, NSUserDomainMask, YES) objectAtIndex:0];
    return dirPath;
}

- (NSString *)getSystemBaseDir {
    NSString *dirPath = [NSSearchPathForDirectoriesInDomains(NSLibraryDirectory, NSUserDomainMask, YES) objectAtIndex:0];
    return dirPath;
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
    
    [mutableDict setObject:prefVal forKey:prefKey];
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
    //NSString *nextVCClassName = [cls isEqual:ComUstadmobileCoreViewCatalogView_class_()];
    UIStoryboard *sb = [UIStoryboard storyboardWithName:@"Main" bundle:nil];
    if ([cls isEqual:ComUstadmobileCoreViewCatalogView_class_()]) {
        nextVC = [sb instantiateViewControllerWithIdentifier:@"CatalogViewController"];
    }else if([cls isEqual:ComUstadmobileCoreViewBasePointView_class_()]) {
        nextVC = [sb instantiateViewControllerWithIdentifier:@"BasePointViewController"];
    }
    
    UIViewController *parentVC = currentVC.parentViewController;
    while(!([parentVC isKindOfClass:[UINavigationController class]]) && parentVC != nil) {
        parentVC = parentVC.parentViewController;
    }
    
    
    
    if(nextVC != nil && [nextVC isKindOfClass:[UstadBaseUIViewController class]]) {
        UstadBaseUIViewController *baseVC = (UstadBaseUIViewController *)nextVC;
        [baseVC setArgumentsWithHashtable:args];
    }
    
    dispatch_async(dispatch_get_main_queue(), ^{
        if(nextVC) {
            UINavigationController *navCtrl = (UINavigationController *)parentVC;
            [navCtrl pushViewController:nextVC animated:YES];
        }
    });
    
    
}

@end
