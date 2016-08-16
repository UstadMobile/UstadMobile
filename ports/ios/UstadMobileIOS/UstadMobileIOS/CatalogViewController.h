//
//  CatalogViewController.h
//  UstadMobileIOS
//
//  Created by Mike Dawson on 16/08/16.
//  Copyright © 2016 UstadMobile FZ-LLC. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "CatalogView.h"
#import "UstadBaseUIViewController.h"


@interface CatalogViewController : UstadBaseUIViewController<ComUstadmobileCoreViewCatalogView, UITableViewDelegate, UITableViewDataSource>

@end
