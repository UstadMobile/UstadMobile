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
    [navCtrl.view layoutIfNeeded];
    [[UIBarButtonItem appearance] setBackButtonTitlePositionAdjustment:UIOffsetMake(0, -60)
                                                         forBarMetrics:UIBarMetricsDefault];

    /*
     Sometimes [self.catalogContainerView setTranslatesAutoresizingMaskIntoConstraints:YES]
     seemed to be needed to stop insane behavior when using auto constraints: now with
     standard constraints to fill width/height it does not seem to be needed and prevents
     the view filling the whole screen on iphone 6plus etc.
     */
    // Do any additional setup after loading the view.
}


-(void)viewWillAppear:(BOOL)animated {
    UINavigationController *navCtrl = (UINavigationController *)self.parentViewController;
    [navCtrl.navigationBar setBarTintColor:[UIColor colorWithRed:(247.0f/255.0f) green:(247.0f/255.0f) blue:(247.0f/255.0f) alpha:1]];
    [navCtrl.navigationBar setTitleTextAttributes:@{NSForegroundColorAttributeName : [UIColor blackColor]}];

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
