//
//  ContainerPageContentViewController.m
//  UstadMobileIOS
//
//  Created by Mike Dawson on 25/08/16.
//  Copyright © 2016 UstadMobile FZ-LLC. All rights reserved.
//

#import "ContainerPageContentViewController.h"
#import "java/util/Hashtable.h"
#import "EmbeddedHTTPD.h"
#import "ControllerReadyListener.h"

@interface ContainerPageContentViewController ()
@end

@implementation ContainerPageContentViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    
    // Do any additional setup after loading the view.
    self.webView.mediaPlaybackRequiresUserAction = NO;
    self.webView.allowsInlineMediaPlayback = YES;
    [self.webView loadRequest:[NSURLRequest requestWithURL:[NSURL URLWithString:self.viewURL]]];
}





- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}


/*
#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
}
*/

@end
