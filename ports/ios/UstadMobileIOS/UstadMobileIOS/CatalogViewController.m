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
#import "CatalogController.h"

@interface CatalogViewController ()
@property (retain, nonatomic) IBOutlet UIButton *browseButton;
@property NSArray *dummyData;
@property ComUstadmobileCoreControllerCatalogController *catalogController;
@end

@implementation CatalogViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    self.dummyData = [NSArray arrayWithObjects:@"Trumponomics", @"Bankruptcy 101", nil];
    [self loadCatalog];
}

-(void)loadCatalog {
    
    id obj = [[self getArguments] getWithId:ComUstadmobileCoreControllerCatalogController_KEY_RESMOD];
    NSString *url = (NSString *)[[self getArguments] getWithId:ComUstadmobileCoreControllerCatalogController_KEY_URL];
    
    //NSInteger *resourceMode = (NSInteger *)[[self getArguments] getWithId:ComUstadmobileCoreControllerCatalogController_KEY_RESMOD];
    
   // [ComUstadmobileCoreControllerCatalogController makeControllerForViewWithComUstadmobileCoreViewCatalogView:self withNSString:url withInt:<#(jint)#> withInt:<#(jint)#> withNSString:<#(NSString *)#> withComUstadmobileCoreControllerControllerReadyListener:<#(id<ComUstadmobileCoreControllerControllerReadyListener>)#>
}

-(void)controllerReadyWithComUstadmobileCoreControllerUstadController:(id<ComUstadmobileCoreControllerUstadController>)controller withInt:(jint)flags {
    self.catalogController = (ComUstadmobileCoreControllerCatalogController *)controller;
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

- (void)setControllerWithComUstadmobileCoreControllerCatalogController:(ComUstadmobileCoreControllerCatalogController *)controller {
    self.catalogController = controller;
}

- (ComUstadmobileCoreControllerCatalogController *)getController {
    return self.catalogController;
}

- (void)showConfirmDialogWithNSString:(NSString *)title
                         withNSString:(NSString *)message
                         withNSString:(NSString *)positiveChoice
                         withNSString:(NSString *)negativeChoice
                              withInt:(jint)commandId {
    
}

- (void)setEntryStatusWithNSString:(NSString *)entryId
                           withInt:(jint)status {
    
}

- (void)setEntrythumbnailWithNSString:(NSString *)entryId
                         withNSString:(NSString *)iconFileURI {
    
}

- (void)updateDownloadAllProgressWithInt:(jint)loaded
                                 withInt:(jint)total {
    
}

- (void)setDownloadEntryProgressVisibleWithNSString:(NSString *)entryId
                                        withBoolean:(jboolean)visible {
    
}

- (void)updateDownloadEntryProgressWithNSString:(NSString *)entryId
                                        withInt:(jint)loaded
                                        withInt:(jint)total {
    
}

- (IOSObjectArray *)getSelectedEntries {
    return nil;
}

- (void)setSelectedEntriesWithComUstadmobileCoreOpdsUstadJSOPDSEntryArray:(IOSObjectArray *)entries {
    
}

- (void)showAddFeedDialog {
    
}

- (void)setAddFeedDialogURLWithNSString:(NSString *)url {
    
}

- (NSString *)getAddFeedDialogURL {
    return nil;
}

- (NSString *)getAddFeedDialogTitle {
    return nil;
}

- (void)setAddFeedDialogTitleWithNSString:(NSString *)title {
    
}

- (void)setBrowseButtonVisibleWithBoolean:(jboolean)buttonVisible {
    
}


- (void)setDeleteOptionAvailableWithBoolean:(jboolean)deleteOptionAvailable {
    
}

- (void)setAddOptionAvailableWithBoolean:(jboolean)addOptionAvailable {
    
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
