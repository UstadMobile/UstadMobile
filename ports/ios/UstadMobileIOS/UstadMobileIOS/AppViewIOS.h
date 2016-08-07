//
//  AppViewIOS.h
//  UstadMobileIOS
//
//  Created by Mike Dawson on 07/08/16.
//  Copyright © 2016 UstadMobile FZ-LLC. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>
#import "AppView.h"

@interface AppViewIOS : NSObject <ComUstadmobileCoreViewAppView>
-(id)initWithViewController:(UIViewController *)viewController;

@end
