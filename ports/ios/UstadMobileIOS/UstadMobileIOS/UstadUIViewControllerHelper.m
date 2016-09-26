//
//  UstadUIViewControllerHelper.m
//  UstadMobileIOS
//
//  Created by Mike Dawson on 26/09/16.
//  Copyright © 2016 UstadMobile FZ-LLC. All rights reserved.
//

#import "UstadUIViewControllerHelper.h"
#import "UstadView.h"

@interface UstadUIViewControllerHelper()
@property JavaUtilHashtable *arguments;
@end

@implementation UstadUIViewControllerHelper

-(id)initWithViewController:(UIViewController *) viewController{
    self = [super init];
    self.viewController = viewController;
    return self;
}

-(void)setArgumentsWithHashtable:(JavaUtilHashtable *)arguments {
    self.arguments = arguments;
}

-(JavaUtilHashtable *)getArguments {
    return self.arguments;
}

- (id)getContext {
    return self.viewController;
}

- (jint)getDirection {
    return self.direction;
}

- (void)setDirectionWithInt:(jint)dir {
    self.direction = dir;
}


- (void)setAppMenuCommandsWithNSStringArray:(IOSObjectArray *)labels
                               withIntArray:(IOSIntArray *)ids {
    //not implemented yet...
}


@end
