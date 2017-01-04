//
//  LoginViewForgotPasswordViewController.m
//  UstadMobileIOS
//
//  Created by Mike Dawson on 10/10/16.
//  Copyright © 2016 UstadMobile FZ-LLC. All rights reserved.
//

#import "LoginViewForgotPasswordViewController.h"
#import "LoginPageViewController.h"

@interface LoginViewForgotPasswordViewController ()
@property (retain, nonatomic) IBOutlet UIButton *forgotPasswordBackButton;
- (IBAction)forgotPasswordBackButtonClicked:(UIButton *)sender;

@end

@implementation LoginViewForgotPasswordViewController

- (void)viewDidLoad {
    [super viewDidLoad];
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

- (IBAction)forgotPasswordBackButtonClicked:(UIButton *)sender {
    LoginPageViewController *loginViewCtrl = (LoginPageViewController *)self.parentViewController;
    [loginViewCtrl showViewControllerAtIndex:0 animation:YES];
}
@end
