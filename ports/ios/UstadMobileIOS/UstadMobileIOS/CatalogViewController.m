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
#import "CatalogModel.h"
#import "UstadJSOPDSFeed.h"
#import "UstadJSOPDSEntry.h"
#import "CatalogViewControllerEntryTableViewCell.h"

@interface CatalogViewController ()
@property (retain, nonatomic) IBOutlet UIButton *browseButton;
@property NSArray *dummyData;
@property ComUstadmobileCoreControllerCatalogController *catalogController;
- (IBAction)browseButtonClicked:(UIButton *)sender;
@property (retain, nonatomic) IBOutlet UITableView *catalogTableView;
@property NSMapTable *idToCellMapTable;

@end

@implementation CatalogViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    self.idToCellMapTable = [NSMapTable strongToWeakObjectsMapTable];
    [self loadCatalog];
}

-(void)loadCatalog {
    [ComUstadmobileCoreControllerCatalogController makeControllerForViewWithComUstadmobileCoreViewCatalogView:self withJavaUtilHashtable:[self getArguments] withComUstadmobileCoreControllerControllerReadyListener:self];
}

-(void)controllerReadyWithComUstadmobileCoreControllerUstadController:(id<ComUstadmobileCoreControllerUstadController>)controller withInt:(jint)flags {
    self.catalogController = (ComUstadmobileCoreControllerCatalogController *)controller;
    NSString *title = [self.catalogController getModel]->opdsFeed_->title_;
    if([self.parentViewController isKindOfClass:[UINavigationController class]]) {
        [self.navigationItem setTitle:title];
    }else if(self.parentViewController != nil){
        [self.parentViewController.navigationItem setTitle:title];
    }
    
    [self.catalogTableView reloadData];
    //[self setTitle:title];
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
    UIAlertController *alertController = [UIAlertController alertControllerWithTitle:title message:message preferredStyle:UIAlertControllerStyleAlert];
    UIAlertAction *positiveAction= [UIAlertAction actionWithTitle:positiveChoice style:UIAlertActionStyleDefault handler:^(UIAlertAction *action) {
        [self.catalogController handleConfirmDialogClickWithBoolean:true withInt:commandId];
    }];
    [alertController addAction:positiveAction];
    UIAlertAction *negativeAction = [UIAlertAction actionWithTitle:negativeChoice style:(UIAlertActionStyleCancel) handler:^(UIAlertAction *action) {
        [self.catalogController handleConfirmDialogClickWithBoolean:false withInt:commandId];
    }];
    [alertController addAction:negativeAction];
    [self presentViewController:alertController animated:YES completion:nil];
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
    if(!buttonVisible) {
        [self.browseButton setHidden:YES];
    }else {
        [self.browseButton setHidden:NO];
    }
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
    if(self.catalogController != nil) {
        return [self.catalogController getModel]->opdsFeed_->entries_->size_;
    }else {
        return 0;
    }
}

-(UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    static NSString *simpleTableIdentifier = @"CatalogEntryCell";
    
    
    CatalogViewControllerEntryTableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:simpleTableIdentifier];
    ComUstadmobileCoreOpdsUstadJSOPDSItem *item = IOSObjectArray_Get([self.catalogController getModel]->opdsFeed_->entries_, (jint)indexPath.row);
    
    //check if this is a recycled cell
    NSDictionary *idToCellDict = [self.idToCellMapTable dictionaryRepresentation];
    for(NSString *entryId in idToCellDict) {
        if([idToCellDict objectForKey:entryId] == cell) {
            [self.idToCellMapTable removeObjectForKey:entryId];
        }
    }
    
    [self.idToCellMapTable setObject:cell forKey:item->id__];
    
    cell.titleLabel.text = item->title_;
    [cell.progressView setHidden:YES];
    return cell;
}

-(void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    [tableView deselectRowAtIndexPath:indexPath animated:NO];
    ComUstadmobileCoreOpdsUstadJSOPDSEntry *item = IOSObjectArray_Get([self.catalogController getModel]->opdsFeed_->entries_, (jint)indexPath.row);
    [self.catalogController handleClickEntryWithComUstadmobileCoreOpdsUstadJSOPDSEntry:item];
}

- (IBAction)browseButtonClicked:(UIButton *)sender {
    [self.catalogController handleClickBrowseButton];
}
@end
