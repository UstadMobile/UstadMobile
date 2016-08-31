//
//  ContainerViewController.h
//  UstadMobileIOS
//
//  Created by Mike Dawson on 25/08/16.
//  Copyright © 2016 UstadMobile FZ-LLC. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "UstadViewControllerWithArgs.h"
#import "ControllerReadyListener.h"
#import "ContainerView.h"

@interface ContainerViewController : UIPageViewController<UIPageViewControllerDataSource, UstadViewControllerWithArgs, ComUstadmobileCoreControllerControllerReadyListener, ComUstadmobileCoreViewContainerView>

@end
