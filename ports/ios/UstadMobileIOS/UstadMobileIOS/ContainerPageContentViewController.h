//
//  ContainerPageContentViewController.h
//  UstadMobileIOS
//
//  Created by Mike Dawson on 25/08/16.
//  Copyright © 2016 UstadMobile FZ-LLC. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "ContainerView.h"

@interface ContainerPageContentViewController : UIViewController<UIWebViewDelegate>
@property (retain, nonatomic) IBOutlet UIWebView *webView;
@property NSString *viewURL;
@property NSUInteger pageIndex;
@end
