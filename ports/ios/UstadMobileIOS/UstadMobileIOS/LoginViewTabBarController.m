//
//  LoginTabBarController.m
//  UstadMobileIOS
//
//  Created by Mike Dawson on 10/08/16.
//  Copyright © 2016 UstadMobile FZ-LLC. All rights reserved.
//

#import "LoginViewTabBarController.h"
#import <UIKit/UIKit.h>
#import "LoginViewLoginTabViewController.h"

@interface LoginViewTabBarController ()
@property LoginViewLoginTabViewController *loginTab;
@end

@implementation LoginViewTabBarController

- (void)viewDidLoad {
    [super viewDidLoad];
    self.loginController = [ComUstadmobileCoreControllerLoginController makeControllerForViewWithComUstadmobileCoreViewLoginView:self];
    [self.loginController setUIStrings];
    self.loginTab = self.viewControllers[0];
    // Do any additional setup after loading the view.
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (id)getContext {
    return self;
}

- (jint)getDirection {
    return self.direction;
}

- (void)setDirectionWithInt:(jint)dir {
    self.direction = dir;
}

- (void)setUIStrings {
    //right now there's no non tab components here with localizable ui strings
}

- (void)setAppMenuCommandsWithNSStringArray:(IOSObjectArray *)labels
                               withIntArray:(IOSIntArray *)ids {
    //not implemented yet...
}


- (void)setControllerWithComUstadmobileCoreControllerLoginController:(ComUstadmobileCoreControllerLoginController *)loginController{
    
}

- (void)setTitleWithNSString:(NSString *)title {
    
}


- (void)setXAPIServerURLWithNSString:(NSString *)xAPIServerURL {
    LoginViewLoginTabViewController *loginTab = (LoginViewLoginTabViewController *)self.viewControllers[0];
    [loginTab setXAPIServerURLWithNSString:xAPIServerURL];
}


- (void)setAdvancedSettingsVisibleWithBoolean:(jboolean)visible {
    
}

- (void)setVersionLabelWithNSString:(NSString *)versionLabel {
    
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
