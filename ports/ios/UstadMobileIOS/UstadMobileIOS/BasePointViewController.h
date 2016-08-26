//
//  BasePointViewController.h
//  UstadMobileIOS
//
//  Created by Mike Dawson on 16/08/16.
//  Copyright © 2016 UstadMobile FZ-LLC. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "BasePointView.h"
#import "UstadBaseUIViewController.h"
#import "UstadViewControllerWithArgs.h"

@interface BasePointViewController : UstadBaseUIViewController<ComUstadmobileCoreViewBasePointView, UstadViewControllerWithArgs>

@end
