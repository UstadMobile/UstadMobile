//
//  ContainerViewController.m
//  UstadMobileIOS
//
//  Created by Mike Dawson on 25/08/16.
//  Copyright © 2016 UstadMobile FZ-LLC. All rights reserved.
//

#import "ContainerViewController.h"
#import "ContainerPageContentViewController.h"
#import "java/util/Hashtable.h"
#import "UstadMobileSystemImpl.h"
#import "UstadMobileSystemImplIOS.h"
#import "ContainerController.h"
#import "UstadJSOPF.h"


@interface ContainerViewController ()
@property JavaUtilHashtable *arguments;
@property NSString *containerURI;
@property IOSObjectArray *linearSpineURLs;
@property jint direction;
@property ComUstadmobileCoreControllerContainerController *containerController;
@property NSString *mountedPath;
@end

@implementation ContainerViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    self.containerURI = (NSString *)[self.arguments getWithId:ComUstadmobileCoreControllerContainerController_ARG_CONTAINERURI];
    [self initContent];
}

-(void)setArgumentsWithHashtable:(JavaUtilHashtable *)arguments {
    self.arguments = arguments;
}

-(void)initContent {
    UstadMobileSystemImplIOS *impl = (UstadMobileSystemImplIOS *)[ComUstadmobileCoreImplUstadMobileSystemImpl getInstance];
    ComUstadmobilePortSharedseImplHttpEmbeddedHTTPD *httpd = [impl getHTTPD];
    
    self.mountedPath = [httpd mountZipWithNSString:self.containerURI withNSString:nil withJavaUtilHashMap:nil];
    NSString *mountAppend = self.mountedPath;
    if([[mountAppend substringToIndex:1] isEqualToString:@"/"]) {
        mountAppend = [mountAppend substringFromIndex:1];
    }
    
    NSString *mountURI = [[httpd getLocalURL] stringByAppendingString:mountAppend];
    [self.arguments putWithId:ComUstadmobileCoreControllerContainerController_ARG_OPENPATH withId:mountURI];
    [ComUstadmobileCoreControllerContainerController makeControllerForViewWithComUstadmobileCoreViewContainerView:self withJavaUtilHashtable:self.arguments withComUstadmobileCoreControllerControllerReadyListener:self];
    
}

-(void)controllerReadyWithComUstadmobileCoreControllerUstadController:(id<ComUstadmobileCoreControllerUstadController>)controller withInt:(jint)flags {
    self.containerController = (ComUstadmobileCoreControllerContainerController *)controller;
    dispatch_async(dispatch_get_main_queue(), ^{
        NSString *title = [self.containerController getActiveOPF]->title_;
        if([self.parentViewController isKindOfClass:[UINavigationController class]]) {
            [self.navigationItem setTitle:title];
        }
        
        self.linearSpineURLs = [self.containerController getSpineURLsWithBoolean:false];
        self.dataSource = self;
        
        ContainerPageContentViewController *startingController = [self viewControllerAtIndex:0];
        NSArray *viewControllers = @[startingController];
        [self setViewControllers:viewControllers direction:UIPageViewControllerNavigationDirectionForward animated:NO completion:nil];
    });
}

- (void)viewDidDisappear:(BOOL)animated {
    UstadMobileSystemImplIOS *impl = (UstadMobileSystemImplIOS *)[ComUstadmobileCoreImplUstadMobileSystemImpl getInstance];
    [[impl getHTTPD] unmountZipWithNSString:self.mountedPath];
}

- (void)setControllerWithComUstadmobileCoreControllerContainerController:(ComUstadmobileCoreControllerContainerController *)controller;{
    
}

- (void)setContainerTitleWithNSString:(NSString *)containerTitle {
    
}
- (void)showPDF {
    
}
- (void)showEPUB {
    
}
- (jboolean)refreshURLs {
    return false;
}


- (id)getContext {
    return self;
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

- (void)setUIStrings {
    //right now there's no non tab components here with localizable ui strings
}


- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}


-(ContainerPageContentViewController *)viewControllerAtIndex:(NSUInteger)index {
    ContainerPageContentViewController *pageViewController = [self.storyboard instantiateViewControllerWithIdentifier:@"ContainerPageContentViewController"];
    pageViewController.viewURL = IOSObjectArray_Get(self.linearSpineURLs,(jint)index);
    pageViewController.pageIndex = index;
    return pageViewController;
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
    NSUInteger index = ((ContainerPageContentViewController *)viewController).pageIndex;
    if((index == 0) || (index == NSNotFound)) {
        return nil;
    }
    index--;
    
    return [self viewControllerAtIndex:index];
}

-(UIViewController *)pageViewController:(UIPageViewController *)pageViewController viewControllerAfterViewController:(UIViewController *)viewController {
    NSUInteger index = ((ContainerPageContentViewController *)viewController).pageIndex;
    if(index == NSNotFound) {
        return nil;
    }
    
    index++;
    if(index == [self.linearSpineURLs length]) {
        return nil;
    }
    
    return [self viewControllerAtIndex:index];
}


-(NSInteger)presentationCountForPageViewController:(UIPageViewController *)pageViewController {
    return [self.linearSpineURLs length];
}

-(NSInteger)presentationIndexForPageViewController:(UIPageViewController *)pageViewController {
    return 0;
}


@end
