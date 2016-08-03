//
//  ViewController.m
//  UstadMobileIOS
//
//  Created by Mike Dawson on 03/08/16.
//  Copyright © 2016 UstadMobile FZ-LLC. All rights reserved.
//

#import "ViewController.h"
#include "UMFileUtil.h"
#include "IOSClass.h"
#include "IOSObjectArray.h"
#include "IOSPrimitiveArray.h"
#include "J2ObjC_source.h"
#include "UstadMobileSystemImpl.h"
#include "UstadMobileSystemImplFactoryIOS.h"
#include "LoginController.h"

@interface ViewController ()

@end

@implementation ViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    UstadMobileSystemImplFactoryIOS *iosFactory = [[UstadMobileSystemImplFactoryIOS alloc]init];
    [ComUstadmobileCoreImplUstadMobileSystemImpl setSystemImplFactoryWithComUstadmobileCoreImplUstadMobileSystemImplFactory:iosFactory];
    jint result = [ComUstadmobileCoreControllerLoginController authenticateWithNSString:@"miketestecop" withNSString:@"letsLearnEcop" withNSString:@"http://umcloud1.ustadmobile.com/umlrs/"];
    NSString *xapiServer = @"http://yahoo.com";
    NSString *stmtURL = ComUstadmobileCoreUtilUMFileUtil_joinPathsWithNSStringArray_([IOSObjectArray arrayWithObjects:(id[]){ xapiServer, @"statements" } count:2 type:NSString_class_()]);

    // Do any additional setup after loading the view, typically from a nib.
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

@end
