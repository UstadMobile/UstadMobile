//
//  LoginViewController.m
//  UstadMobileIOS
//
//  Created by Mike Dawson on 03/08/16.
//  Copyright © 2016 UstadMobile FZ-LLC. All rights reserved.
//

#import "LoginViewController.h"
#include "IOSClass.h"
#include "IOSObjectArray.h"
#include "IOSPrimitiveArray.h"
#include "J2ObjC_source.h"
#include "java/io/InputStream.h"
#include "UstadMobileSystemImpl.h"
#include "UstadMobileSystemImplFactoryIOS.h"

@interface LoginViewController ()
@property (nonatomic) NSObject *obj;
@end

@implementation LoginViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    UstadMobileSystemImplFactoryIOS *iosFactory = [[UstadMobileSystemImplFactoryIOS alloc]init];
    [ComUstadmobileCoreImplUstadMobileSystemImpl setSystemImplFactoryWithComUstadmobileCoreImplUstadMobileSystemImplFactory:iosFactory];
    ComUstadmobileCoreImplUstadMobileSystemImpl *impl = [ComUstadmobileCoreImplUstadMobileSystemImpl getInstance];
    JavaIoInputStream *is = [impl openResourceInputStreamWithNSString:@"/res/locale/en.properties" withId:self.obj];
    self.obj = [[NSObject alloc]init];
    NSString *filePath = [[NSBundle mainBundle] pathForResource:@"en.properties" ofType:nil];
    //JavaIoInputStream *is = [[ComUstadmobileCoreImplUstadMobileSystemImpl getInstance] openResourceInputStreamWithNSString:@"locale/en.properties" withId:self.obj];
    
    
    NSString *str = @"Hello World";
    // Do any additional setup after loading the view.
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

/*
#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
}
*/

@end
