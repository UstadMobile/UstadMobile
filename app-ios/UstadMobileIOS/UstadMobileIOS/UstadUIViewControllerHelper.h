//
//  UstadUIViewControllerHelper.h
//  UstadMobileIOS
//
//  Created by Mike Dawson on 26/09/16.
//  Copyright © 2016 UstadMobile FZ-LLC. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>
#import "java/util/Hashtable.h"
#import "UstadView.h"

@interface UstadUIViewControllerHelper : NSObject
@property UIViewController *viewController;
@property jint direction;

-(id)initWithViewController:(UIViewController *)viewController;

-(void)setArgumentsWithHashtable:(JavaUtilHashtable *)arguments;

-(JavaUtilHashtable *)getArguments;

- (id)getContext;

- (jint)getDirection;

- (void)setDirectionWithInt:(jint)dir;

- (void)setAppMenuCommandsWithNSStringArray:(IOSObjectArray *)labels
                               withIntArray:(IOSIntArray *)ids;

@end
