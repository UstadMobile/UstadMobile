//
//  BasePointViewController.m
//  UstadMobileIOS
//
//  Created by Mike Dawson on 16/08/16.
//  Copyright © 2016 UstadMobile FZ-LLC. All rights reserved.
//

#import "BasePointViewController.h"
#import "java/util/Hashtable.h"
#import "BasePointController.h"
#import "CatalogViewController.h"

@interface BasePointViewController ()
@property ComUstadmobileCoreControllerBasePointController *basePointController;
@property (retain, nonatomic) IBOutlet UIView *catalogContainerView;
@end

@implementation BasePointViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    UINavigationController *navCtrl = (UINavigationController *)self.parentViewController;
    [navCtrl setNavigationBarHidden:NO];
    [self.catalogContainerView setTranslatesAutoresizingMaskIntoConstraints:YES];
    // Do any additional setup after loading the view.
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (void)refreshCatalogWithInt:(jint)column;{
    
}

-(ComUstadmobileCoreControllerBasePointController *)getBasePointController {
    if(self.basePointController == nil) {
        self.basePointController = [ComUstadmobileCoreControllerBasePointController makeControllerForViewWithComUstadmobileCoreViewBasePointView:self withJavaUtilHashtable:[self getArguments]];
    }
    
    return self.basePointController;
}

- (void)setClassListVisibleWithBoolean:(jboolean)visible {
    //class list is not implemented in iOS
}

- (void)setAppMenuCommandsWithNSStringArray:(IOSObjectArray *)labels
                               withIntArray:(IOSIntArray *)ids {
    //not implemented yet...
}


#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    UIViewController *nextVC = segue.destinationViewController;
    if([nextVC isKindOfClass:[CatalogViewController class]]) {
        JavaUtilHashtable *catArgs = [[self getBasePointController] getCatalogOPDSArgumentsWithInt:0];
        CatalogViewController *catVC = (CatalogViewController *)nextVC;
        [catVC setArgumentsWithHashtable:catArgs];
    }
}

@end
