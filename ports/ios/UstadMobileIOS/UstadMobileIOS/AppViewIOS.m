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

//used to popup text alerts
@property UIAlertController *alertController;

//used to show progress spinner for loading etc
@property UIAlertController *progressAlertController;
@end

@implementation AppViewIOS

-(id)initWithViewController:(UIViewController *)viewController {
    self.uiViewController = viewController;
    return self;
}


- (void)showProgressDialogWithNSString:(NSString *) title {
    [self dismissProgressDialog];
    dispatch_async(dispatch_get_main_queue(), ^{
        self.progressAlertController = [UIAlertController alertControllerWithTitle:nil
                                                                         message:[title stringByAppendingString:@"\n\n"]
                                                                  preferredStyle:UIAlertControllerStyleAlert];
        UIActivityIndicatorView* indicator = [[UIActivityIndicatorView alloc] initWithActivityIndicatorStyle:UIActivityIndicatorViewStyleWhiteLarge];
        indicator.color = [UIColor blackColor];
        indicator.translatesAutoresizingMaskIntoConstraints=NO;
        [self.progressAlertController.view addSubview:indicator];
        NSDictionary * views = @{@"pending" : self.progressAlertController.view, @"indicator" : indicator};
        
        NSArray * constraintsVertical = [NSLayoutConstraint constraintsWithVisualFormat:@"V:[indicator]-(20)-|" options:0 metrics:nil views:views];
        NSArray * constraintsHorizontal = [NSLayoutConstraint constraintsWithVisualFormat:@"H:|[indicator]|" options:0 metrics:nil views:views];
        NSArray * constraints = [constraintsVertical arrayByAddingObjectsFromArray:constraintsHorizontal];
        [self.progressAlertController.view addConstraints:constraints];
        [indicator setUserInteractionEnabled:NO];
        [indicator startAnimating];
        [self.uiViewController presentViewController:self.progressAlertController animated:YES completion:nil];
    });
}

- (jboolean)dismissProgressDialog {
    if([NSThread isMainThread]) {
        if(self.progressAlertController) {
            [self.uiViewController dismissViewControllerAnimated:YES completion:nil];
            self.progressAlertController = nil;
            return true;
        }else {
            return false;
        }
    }else {
        [self performSelectorOnMainThread:@selector(dismissProgressDialog) withObject:self waitUntilDone:YES];
    }
    return false;
    
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
