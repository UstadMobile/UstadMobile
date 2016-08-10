//
//  LoginTabBarController.h
//  UstadMobileIOS
//
//  Created by Mike Dawson on 10/08/16.
//  Copyright © 2016 UstadMobile FZ-LLC. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "LoginController.h"
#import "LoginView.h"

@interface LoginViewTabBarController : UITabBarController <ComUstadmobileCoreViewLoginView>
@property ComUstadmobileCoreControllerLoginController *loginController;
@property jint direction;
@end
