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
#include "LoginController.h"

@interface LoginViewController ()
//@property (nonatomic) NSObject *obj;
@property NSString *xAPIServerURL;
@property ComUstadmobileCoreControllerLoginController *loginController;
@property (retain, nonatomic) IBOutlet UITextField *usernameTextField;
@property (retain, nonatomic) IBOutlet UITextField *passwordTextField;
@property (retain, nonatomic) IBOutlet UIButton *loginButton;
- (IBAction)loginButtonClicked:(UIButton *)sender;

@end

@implementation LoginViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    self.loginController = [ComUstadmobileCoreControllerLoginController makeControllerForViewWithComUstadmobileCoreViewLoginView:self];
    [self.loginController setUIStrings];
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (void)setControllerWithComUstadmobileCoreControllerLoginController:(ComUstadmobileCoreControllerLoginController *)loginController{
    
}

- (void)setTitleWithNSString:(NSString *)title {
    
}

- (void)setUsernameHintWithNSString:(NSString *)loginHint {
    self.usernameTextField.placeholder = loginHint;
}

- (void)setPasswordHintWithNSString:(NSString *)passwordHint {
    self.passwordTextField.placeholder = passwordHint;
}

- (void)setButtonTextWithNSString:(NSString *)buttonText {
    [self.loginButton setTitle:buttonText forState:UIControlStateNormal];
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

- (IBAction)loginButtonClicked:(UIButton *)sender {
    [self.loginController handleClickLoginWithNSString:self.usernameTextField.text withNSString:self.passwordTextField.text withNSString:self.xAPIServerURL];
}

/*
#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
}
*/

- (void)dealloc {
    [_passwordTextField release];
    [_loginButton release];
    [_usernameTextField release];
    [super dealloc];
}

@end
