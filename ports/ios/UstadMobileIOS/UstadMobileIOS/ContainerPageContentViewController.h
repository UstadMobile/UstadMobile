//
//  ContainerPageContentViewController.h
//  UstadMobileIOS
//
//  Created by Mike Dawson on 25/08/16.
//  Copyright © 2016 UstadMobile FZ-LLC. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface ContainerPageContentViewController : UIViewController
@property (retain, nonatomic) IBOutlet UIWebView *webView;
@property NSString *viewURL;
@property NSUInteger pageIndex;
@end
