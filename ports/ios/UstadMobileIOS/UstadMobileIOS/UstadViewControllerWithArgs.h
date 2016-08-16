//
//  UstadViewControllerWithArgs.h
//  UstadMobileIOS
//
//  Created by Mike Dawson on 14/08/16.
//  Copyright © 2016 UstadMobile FZ-LLC. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "java/util/Hashtable.h"

@protocol UstadViewControllerWithArgs <NSObject>
-(void) setArgsWithHashtable:(JavaUtilHashtable *)args;
@end
