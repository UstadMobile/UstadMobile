//
//  CatalogViewController.m
//  UstadMobileIOS
//
//  Created by Mike Dawson on 16/08/16.
//  Copyright © 2016 UstadMobile FZ-LLC. All rights reserved.
//

#import "CatalogViewController.h"
#import "UstadMobileSystemImpl.h"
#import "MessageIDConstants.h"

@interface CatalogViewController ()
@property (retain, nonatomic) IBOutlet UIButton *browseButton;
@property NSArray *dummyData;
@end

@implementation CatalogViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    self.dummyData = [NSArray arrayWithObjects:@"Trumponomics", @"Bankruptcy 101", nil];
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

-(void) setBrowseButtonLabelWithNSString:(NSString *)browseButtonLabel {
    [self.browseButton.titleLabel setText:browseButtonLabel];
}



-(void)refresh {
    
}


/*
#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
}
*/

-(NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return [self.dummyData count];
}

-(UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    static NSString *simpleTableIdentifier = @"SimpleTableCell";
    
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:simpleTableIdentifier];
    if(cell == nil) {
        cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:simpleTableIdentifier];
    }
    cell.textLabel.text = [self.dummyData objectAtIndex:indexPath.row];
    return cell;
}

@end
