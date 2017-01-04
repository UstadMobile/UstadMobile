//
//  UstadBaseUIViewController.h
//  UstadMobileIOS
//
//  Created by Mike Dawson on 16/08/16.
//  Copyright © 2016 UstadMobile FZ-LLC. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "UstadView.h"
#import "java/util/Hashtable.h"

@interface UstadBaseUIViewController : UIViewController<ComUstadmobileCoreViewUstadView>

//sets the main arguments for this view
-(void)setArgumentsWithHashtable:(JavaUtilHashtable *)arguments;

//sets the navigation bar title background color
-(void)setNavigationBarBackgroundColor;


//gets the main arguments for this view
-(JavaUtilHashtable *)getArguments;
@end
