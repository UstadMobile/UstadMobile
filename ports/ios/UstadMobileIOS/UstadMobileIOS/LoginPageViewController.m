//
//  LoginPageViewController.m
//  UstadMobileIOS
//
//  Created by Mike Dawson on 25/09/16.
//  Copyright © 2016 UstadMobile FZ-LLC. All rights reserved.
//

#import "LoginPageViewController.h"
#import "UstadUIViewControllerHelper.h"

@interface LoginPageViewController ()
@property NSMapTable *pageTable;
@property NSArray *storyboardIdsByPageIndex;
@property UstadUIViewControllerHelper *ustadVcHelper;
@end

@implementation LoginPageViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    self.ustadVcHelper = [[UstadUIViewControllerHelper alloc]initWithViewController:self];
    
    self.loginController = [ComUstadmobileCoreControllerLoginController makeControllerForViewWithComUstadmobileCoreViewLoginView:self];
    [self.loginController setUIStrings];
    
    self.storyboardIdsByPageIndex = @[@"loginViewLoginPage", @"loginViewRegisterPage"];
    self.pageTable = [NSMapTable strongToStrongObjectsMapTable];
    self.dataSource = self;
    LoginPageContentViewController *startingController = [self viewControllerAtIndex:0];
    NSArray *viewControllers = @[startingController];
    [self setViewControllers:viewControllers direction:UIPageViewControllerNavigationDirectionForward animated:NO completion:nil];
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

//Start Ustad View core methods (delegated to ViewController Helper)
-(void)setArgumentsWithHashtable:(JavaUtilHashtable *)arguments {
    [self.ustadVcHelper setArgumentsWithHashtable:arguments];
}

-(JavaUtilHashtable *)getArguments {
    return [self.ustadVcHelper getArguments];
}


- (id)getContext {
    return [self.ustadVcHelper getContext];
}

- (jint)getDirection {
    return [self.ustadVcHelper getDirection];
}

- (void)setDirectionWithInt:(jint)dir {
    [self.ustadVcHelper setDirectionWithInt:dir];
}

- (void)setAppMenuCommandsWithNSStringArray:(IOSObjectArray *)labels
                               withIntArray:(IOSIntArray *)ids {
    //not implemented yet...
}

- (void)setUIStrings {
    //right now there's no non tab components here with localizable ui strings
}




-(LoginPageContentViewController *)viewControllerAtIndex:(NSUInteger)index {
    LoginPageContentViewController *viewController = nil;
    if(index <= 2) {
        NSString *indexStr = [NSString stringWithFormat:@"%i", (int)index];
        viewController = [self.pageTable objectForKey:indexStr];
        if(viewController == nil) {
            viewController = [self.storyboard instantiateViewControllerWithIdentifier:[self.storyboardIdsByPageIndex objectAtIndex:index]];
            viewController.index = index;
            [self.pageTable setObject:viewController forKey:indexStr];
        }
    }
    
    return viewController;
}


- (void)setTitleWithNSString:(NSString *)title {
    
}

- (void)setXAPIServerURLWithNSString:(NSString *)xAPIServerURL {
    self.xapiServer = xAPIServerURL;
}

- (void)setAdvancedSettingsVisibleWithBoolean:(jboolean)visible {
    
}

- (void)setVersionLabelWithNSString:(NSString *)versionLabel {
    
}



/*
#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
}
*/



-(UIViewController *)pageViewController:(UIPageViewController *)pageViewController viewControllerBeforeViewController:(UIViewController *)viewController {
    NSUInteger index = ((LoginPageContentViewController *)viewController).index;
    if(index == 0 || index == NSNotFound) {
        return nil;
    }else {
        return [self viewControllerAtIndex:(index - 1)];
    }
}

-(UIViewController *)pageViewController:(UIPageViewController *)pageViewController viewControllerAfterViewController:(UIViewController *)viewController {
    NSUInteger index = ((LoginPageContentViewController *)viewController).index;
    if(index == NSNotFound || index >= LOGINPAGEVIEWCONTROLLER_NUMPAGES) {
        return nil;
    }else {
        return [self viewControllerAtIndex:(index + 1)];
    }
}

- (void)setControllerWithComUstadmobileCoreControllerLoginController:(ComUstadmobileCoreControllerLoginController *)loginController{
 //not really used
}

@end
