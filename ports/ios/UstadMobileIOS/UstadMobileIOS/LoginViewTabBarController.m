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
@property NSString *xAPIServerURL;
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

- (void)setAppMenuCommandsWithNSStringArray:(IOSObjectArray *)labels
                               withIntArray:(IOSIntArray *)ids {
    //not implemented yet...
}


- (void)setControllerWithComUstadmobileCoreControllerLoginController:(ComUstadmobileCoreControllerLoginController *)loginController{
    
}

- (void)setTitleWithNSString:(NSString *)title {
    
}

- (void)setUsernameHintWithNSString:(NSString *)loginHint {
    self.loginTab.usernameTextField.placeholder = loginHint;
}

- (void)setPasswordHintWithNSString:(NSString *)passwordHint {
    self.loginTab.passwordTextField.placeholder = passwordHint;
}

- (void)setButtonTextWithNSString:(NSString *)buttonText {
    [self.loginTab.loginButton setTitle:buttonText forState:UIControlStateNormal];
}

- (void)setRegisterPhoneNumberHintWithNSString:(NSString *)phoneNumberHint {
    
}

- (void)setRegisterNameHintWithNSString:(NSString *)nameHint {
    
}

- (void)setRegisterUsernameHintWithNSString:(NSString *)usernameHint {
    
}

- (void)setRegisterPasswordHintWithNSString:(NSString *)passwordHint {
    
}

- (void)setRegisterEmailHintWithNSString:(NSString *)registerEmailHint {
    
}

- (void)setRegisterRegcodeHintWithNSString:(NSString *)registerRegcodHint {
    
}

- (void)setRegisterGenderMaleLabelWithNSString:(NSString *)maleLabel {
    
}

- (void)setRegisterGenderFemaleLabelWithNSString:(NSString *)femaleLabel {
    
}

- (void)setRegisterButtonTextWithNSString:(NSString *)registerButtonText {
    
}

- (void)setServerLabelWithNSString:(NSString *)serverLabel {
    
}

- (void)setXAPIServerURLWithNSString:(NSString *)xAPIServerURL {
    self.xAPIServerURL = xAPIServerURL;
}

- (void)setAdvancedLabelWithNSString:(NSString *)advancedLabel {
    
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
