//
//  LoginViewTabBarController2.m
//  UstadMobileIOS
//
//  Created by Mike Dawson on 11/08/16.
//  Copyright © 2016 UstadMobile FZ-LLC. All rights reserved.
//

#import "LoginViewTabBarController2.h"
#import "LoginViewLoginTabViewController.h"
#import "LoginController.h"

@interface LoginViewTabBarController2 ()
@property jint direction;
@end

@implementation LoginViewTabBarController2

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    self.loginController = [ComUstadmobileCoreControllerLoginController makeControllerForViewWithComUstadmobileCoreViewLoginView:self];
    [self.loginController setUIStrings];
    if([self.tabBarButtons count]) {
        [self performSegueWithIdentifier: @"LoginViewLoginTabSegue"
                                  sender: self.tabBarButtons[0]];
        
    }
    
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}



- (void)setControllerWithComUstadmobileCoreControllerLoginController:(ComUstadmobileCoreControllerLoginController *)loginController{
    
}

- (void)setTitleWithNSString:(NSString *)title {
    
}


- (void)setXAPIServerURLWithNSString:(NSString *)xAPIServerURL {
    self.xapiServer = xAPIServerURL;
}


- (void)setAdvancedSettingsVisibleWithBoolean:(jboolean)visible {
    
}

- (void)setVersionLabelWithNSString:(NSString *)versionLabel {
    
}


/*
#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
 */
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
    
    NSArray *availableIdentifiers = @[@"LoginViewLoginTabSegue",
                                      @"LoginViewRegisterTabSegue"];
    
    
    
    if([availableIdentifiers containsObject: segue.identifier])
    {
        if([segue.destinationViewController isKindOfClass:[LoginViewLoginTabViewController class]]) {
            LoginViewLoginTabViewController *loginTab = (LoginViewLoginTabViewController *)segue.destinationViewController;
            loginTab.loginViewController = self;
        }
        
        
        for (UIButton *btn in self.tabBarButtons)
        {
            if(sender != nil && ![btn isEqual: sender]) {
                [btn setSelected: NO];
            } else if(sender != nil) {
                [btn setSelected: YES];
            }
        }
    }
    
}

@end
