//
//  ContainerViewController.m
//  UstadMobileIOS
//
//  Created by Mike Dawson on 25/08/16.
//  Copyright © 2016 UstadMobile FZ-LLC. All rights reserved.
//

#import "ContainerViewController.h"
#import "ContainerPageContentViewController.h"

@interface ContainerViewController ()

@end

@implementation ContainerViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    
    self.dataSource = self;
    ContainerPageContentViewController *startingController = [self viewControllerAtIndex:0];
    NSArray *viewControllers = @[startingController];
    [self setViewControllers:viewControllers direction:UIPageViewControllerNavigationDirectionForward animated:NO completion:nil];
    
}

-(void)initContent {
    
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}


-(ContainerPageContentViewController *)viewControllerAtIndex:(NSUInteger)index {
    ContainerPageContentViewController *pageViewController = [self.storyboard instantiateViewControllerWithIdentifier:@"ContainerPageContentViewController"];
    pageViewController.viewURL = @"http://localhost:8071/";
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
    if(index == 3) {
        return nil;
    }
    
    return [self viewControllerAtIndex:index];
}


-(NSInteger)presentationCountForPageViewController:(UIPageViewController *)pageViewController {
    return 3;
}

-(NSInteger)presentationIndexForPageViewController:(UIPageViewController *)pageViewController {
    return 0;
}


@end
