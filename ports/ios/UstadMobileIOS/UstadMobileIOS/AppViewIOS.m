//
//  AppViewIOS.m
//  UstadMobileIOS
//
//  Created by Mike Dawson on 07/08/16.
//  Copyright © 2016 UstadMobile FZ-LLC. All rights reserved.
//

#import "AppViewIOS.h"
#import "AppView.h"
#import "UIKit/UIKit.h"
#import "UstadMobileSystemImpl.h"
#import "MessageIDConstants.h"

@interface AppViewIOS()
@property UIViewController *uiViewController;
@property UIAlertController *alertController;
@property UIActivityIndicatorView *activityIndicatorView;
@end

@implementation AppViewIOS

-(id)initWithViewController:(UIViewController *)viewController {
    self.uiViewController = viewController;
    return self;
}


- (void)showProgressDialogWithNSString:(NSString *) title {
    [self dismissProgressDialog];
    dispatch_async(dispatch_get_main_queue(), ^{
        self.activityIndicatorView = [[UIActivityIndicatorView alloc]initWithActivityIndicatorStyle:UIActivityIndicatorViewStyleGray];
        [self.activityIndicatorView setHidesWhenStopped:YES];
        
        //This needs centered
        //CGRect viewBounds = self.uiViewController.view.bounds;
        
        self.activityIndicatorView.frame = CGRectMake(0.0, 0.0, 40.0, 40.0);
        [self.uiViewController.view addSubview:self.activityIndicatorView];
        //might need something like this
        // http://stackoverflow.com/questions/10399156/uinavigationcontroller-toolbar-adding-status-text-with-uiactivityindicatorview
        
        [self.activityIndicatorView startAnimating];
    });
}

- (jboolean)dismissProgressDialog {
    if(self.activityIndicatorView) {
        if([NSThread isMainThread]) {
            [self.activityIndicatorView stopAnimating];
            self.activityIndicatorView = nil;
        }else {
            [self performSelectorOnMainThread:@selector(dismissProgressDialog) withObject:self waitUntilDone:YES];
        }
        
        return true;
    }else {
        return false;
    }
}

- (void)showAlertDialogWithNSString:(NSString *)title
                       withNSString:(NSString *)text {
    dispatch_async(dispatch_get_main_queue(), ^{
        self.alertController = [UIAlertController alertControllerWithTitle:title message:text preferredStyle:UIAlertControllerStyleAlert];
        NSString *okStr = [[ComUstadmobileCoreImplUstadMobileSystemImpl getInstance] getStringWithInt:ComUstadmobileCoreMessageIDConstants_ok];
        UIAlertAction *defaultAction = [UIAlertAction actionWithTitle:okStr style:UIAlertActionStyleDefault
                                                              handler:^(UIAlertAction * action) {}];
        [self.alertController addAction:defaultAction];
        [self.uiViewController presentViewController:self.alertController animated:YES completion:nil];
    });
    
}

- (void)dismissAlertDialog {
    if(self.alertController) {
        if([NSThread isMainThread]) {
            [self.alertController dismissViewControllerAnimated:YES completion:nil];
            self.alertController = nil;
        }else {
            [self performSelectorOnMainThread:@selector(dismissAlertDialog) withObject:self waitUntilDone:YES];
        }
    }
}

- (void)showNotificationWithNSString:(NSString *)text
                             withInt:(jint)length{
    
}

- (void)showChoiceDialogWithNSString:(NSString *)title
                   withNSStringArray:(IOSObjectArray *)choices
                             withInt:(jint)commandId
withComUstadmobileCoreViewAppViewChoiceListener:(id<ComUstadmobileCoreViewAppViewChoiceListener>)listener {
    
}

- (void)dismissChoiceDialog {
    
}


@end
