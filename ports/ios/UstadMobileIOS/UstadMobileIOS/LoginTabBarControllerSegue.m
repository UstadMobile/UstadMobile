//
//  LoginTabBarControllerSegue.m
//  UstadMobileIOS
//
//  Created by Mike Dawson on 11/08/16.
//  Copyright © 2016 UstadMobile FZ-LLC. All rights reserved.
//

#import "LoginTabBarControllerSegue.h"
#import "LoginViewTabBarController2.h"
@implementation LoginTabBarControllerSegue

- (void) perform
{
    LoginViewTabBarController2 *tabBarController = (LoginViewTabBarController2 *) self.sourceViewController;
    UIViewController *destinationController = (UIViewController *) self.destinationViewController;
    
    for (UIView *view in tabBarController.placeholderView.subviews)
    {
        [view removeFromSuperview]; // 1
    }
    // Add view to placeholder view
    tabBarController.currentViewController = destinationController;
    [tabBarController.placeholderView addSubview: destinationController.view]; // 2
    
    // Set autoresizing
    [tabBarController.placeholderView setTranslatesAutoresizingMaskIntoConstraints:NO];
    
    UIView *childview = destinationController.view;
    [childview setTranslatesAutoresizingMaskIntoConstraints: NO];
    
    // fill horizontal
    [tabBarController.placeholderView addConstraints: [NSLayoutConstraint constraintsWithVisualFormat: @"H:|[childview]|" options: 0 metrics: nil views: NSDictionaryOfVariableBindings(childview)]]; // 3
    
    // fill vertical
    [tabBarController.placeholderView addConstraints:[ NSLayoutConstraint constraintsWithVisualFormat: @"V:|-0-[childview]-0-|" options: 0 metrics: nil views: NSDictionaryOfVariableBindings(childview)]]; // 3
    
    [tabBarController.placeholderView layoutIfNeeded]; // 3
    
    // notify did move
    [destinationController didMoveToParentViewController: tabBarController]; // 4
}

@end
