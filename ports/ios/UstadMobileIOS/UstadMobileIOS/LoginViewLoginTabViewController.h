//
//  LoginViewController.h
//  UstadMobileIOS
//
//  Created by Mike Dawson on 03/08/16.
//  Copyright © 2016 UstadMobile FZ-LLC. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "LoginViewTabBarController2.h"
#import "LoginPageContentViewController.h"

@interface LoginViewLoginTabViewController : LoginPageContentViewController
@property (retain, nonatomic) IBOutlet UITextField *usernameTextField;
@property (retain, nonatomic) IBOutlet UITextField *passwordTextField;
@property (retain, nonatomic) IBOutlet UIButton *loginButton;
- (IBAction)loginButtonClicked:(UIButton *)sender;
- (void)setXAPIServerURLWithNSString:(NSString *) xAPIServerURL;
@property LoginViewTabBarController2 *loginViewController;
@end
