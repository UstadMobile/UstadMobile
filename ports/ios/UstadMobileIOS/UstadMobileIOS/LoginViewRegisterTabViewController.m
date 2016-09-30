//
//  LoginRegisterTabViewController.m
//  UstadMobileIOS
//
//  Created by Mike Dawson on 10/08/16.
//  Copyright © 2016 UstadMobile FZ-LLC. All rights reserved.
//

#import "LoginViewRegisterTabViewController.h"
#import "UstadMobileSystemImpl.h"
#import "MessageIDConstants.h"
#import "java/util/Hashtable.h"
#import "LoginController.h"
#import "LoginPageViewController.h"

@interface LoginViewRegisterTabViewController ()
@property (retain, nonatomic) IBOutlet UITextField *registerNameField;
@property (retain, nonatomic) IBOutlet UITextField *registerEmailField;
@property (retain, nonatomic) IBOutlet UITextField *registerPasswordField;
@property (retain, nonatomic) IBOutlet UITextField *registerConfirmPasswordField;
@property (retain, nonatomic) IBOutlet UIButton *registerButton;
@property (retain, nonatomic) IBOutlet UIButton *alreadyHaveAccountButton;
@property (retain, nonatomic) IBOutlet UITextField *registerPhoneNumberField;
- (IBAction)clickAlreadyHaveAccountButton:(UIButton *)sender;

@end

@implementation LoginViewRegisterTabViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    ComUstadmobileCoreImplUstadMobileSystemImpl *impl = [ComUstadmobileCoreImplUstadMobileSystemImpl getInstance];
    self.registerNameField.placeholder = [impl getStringWithInt:ComUstadmobileCoreMessageIDConstants_name];
    self.registerEmailField.placeholder = [impl getStringWithInt:ComUstadmobileCoreMessageIDConstants_email];
    self.registerPasswordField.placeholder = [impl getStringWithInt:ComUstadmobileCoreMessageIDConstants_password];
    self.registerConfirmPasswordField.placeholder = [impl getStringWithInt:ComUstadmobileCoreMessageIDConstants_password];
    self.registerPhoneNumberField.placeholder = [impl getStringWithInt:ComUstadmobileCoreMessageIDConstants_phone_number];
    
    // Do any additional setup after loading the view.
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}
- (IBAction)registerButtonClicked:(UIButton *)sender {
    LoginPageViewController *loginViewCtrl = (LoginPageViewController *)self.parentViewController;
    
    JavaUtilHashtable *userVals = [[JavaUtilHashtable alloc]init];
    [userVals putWithId:ComUstadmobileCoreControllerLoginController_REGISTER_NAME withId:self.registerNameField.text];
    [userVals putWithId:ComUstadmobileCoreControllerLoginController_REGISTER_EMAIL withId:self.registerEmailField.text];
    [userVals putWithId:ComUstadmobileCoreControllerLoginController_REGISTER_PASSWORD withId:self.registerPasswordField.text];
    [userVals putWithId:ComUstadmobileCoreControllerLoginController_REGISTER_PHONENUM withId:self.registerPhoneNumberField.text];
    [userVals putWithId:ComUstadmobileCoreControllerLoginController_REGISTER_USERNAME withId:self.registerEmailField.text];
    [userVals putWithId:ComUstadmobileCoreImplUstadMobileSystemImpl_PREFKEY_XAPISERVER withId:loginViewCtrl.xapiServer];
    [userVals putWithId:ComUstadmobileCoreControllerLoginController_REGISTER_COUNTRY withId:@"49"];
        
    //NSString *registerName = [userVals getWithId:ComUstadmobileCoreControllerLoginController_REGISTER_NAME];
    
    
    [loginViewCtrl.loginController handleClickRegisterWithJavaUtilHashtable:userVals];
}

- (IBAction)clickAlreadyHaveAccountButton:(UIButton *)sender {
    LoginPageViewController *loginViewCtrl = (LoginPageViewController *)self.parentViewController;
    [loginViewCtrl showViewControllerAtIndex:0 animation:YES];
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
