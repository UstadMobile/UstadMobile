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
#import "java/lang/Integer.h"


#define UIColorFromRGB(rgbValue) [UIColor colorWithRed:((float)((rgbValue & 0xFF0000) >> 16))/255.0 green:((float)((rgbValue & 0xFF00) >> 8))/255.0 blue:((float)(rgbValue & 0xFF))/255.0 alpha:1.0];

@interface CatalogViewController ()
@property (retain, nonatomic) IBOutlet UIButton *browseButton;
@property NSArray *dummyData;
@property ComUstadmobileCoreControllerCatalogController *catalogController;
- (IBAction)browseButtonClicked:(UIButton *)sender;
@property (retain, nonatomic) IBOutlet UITableView *catalogTableView;
@property NSMapTable *idToCellMapTable;
@property NSMapTable *idToThumbnailTable;
@property NSMapTable *idToBackgroundTable;
@property UIColor *catalogTextColor;
@property UIColor *catalogBgColor;

@property UIRefreshControl *refreshControl;

@property BOOL loadInProgress;

@end

@implementation CatalogViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    self.idToCellMapTable = [NSMapTable strongToWeakObjectsMapTable];
    self.idToThumbnailTable = [NSMapTable strongToStrongObjectsMapTable];
    self.idToBackgroundTable = [NSMapTable strongToStrongObjectsMapTable];
    
    //[self.catalogTableView setTranslatesAutoresizingMaskIntoConstraints:YES];
    
    ComUstadmobileCoreImplUstadMobileSystemImpl *impl = [ComUstadmobileCoreImplUstadMobileSystemImpl getInstance];
    [self.browseButton setTitle:[impl getStringWithInt:ComUstadmobileCoreMessageIDConstants_browse_feeds] forState:UIControlStateNormal];
    self.refreshControl =[[UIRefreshControl alloc] init];
    [self.refreshControl addTarget:self action:@selector(refreshWithUIRefreshControl:) forControlEvents:UIControlEventValueChanged];
    [self.catalogTableView addSubview:self.refreshControl];
    
    //Set a color for the title bar
    UIColor *navBarColor = nil;
    if([self.parentViewController isKindOfClass:[UINavigationController class]]) {
        //catalog view
        UINavigationController *navController = (UINavigationController *)self.parentViewController;
        navBarColor = [UIColor colorWithRed:0.325 green:0.73 blue:0.894 alpha:1];
        [navController.navigationBar setTitleTextAttributes:@{NSForegroundColorAttributeName : [UIColor whiteColor]}];
        //navController.navigationBar.translucent = NO;
        [navController.navigationBar setBarTintColor:navBarColor];
        //[navController.navigationBar setTintColor:[UIColor whiteColor]];
        //[navController.navigationBar.topItem setTitle:@""];
    }else {
        //nested within base point view
    }
    
    [self loadCatalog];
}

-(void)loadCatalog {
    [self loadCatalogWithArguments:[self getArguments]];
}

-(void)loadCatalogWithArguments:(JavaUtilHashtable *)args {
    self.loadInProgress = YES;
    [ComUstadmobileCoreControllerCatalogController makeControllerForViewWithComUstadmobileCoreViewCatalogView:self withJavaUtilHashtable:args withComUstadmobileCoreControllerControllerReadyListener:self];
}

-(void)controllerReadyWithComUstadmobileCoreControllerUstadController:(id<ComUstadmobileCoreControllerUstadController>)controller withInt:(jint)flags {
    self.catalogController = (ComUstadmobileCoreControllerCatalogController *)controller;
    dispatch_async(dispatch_get_main_queue(), ^{
        self.loadInProgress = NO;
        if(self.refreshControl.refreshing) {
            [self.refreshControl endRefreshing];
        }
        
        
        ComUstadmobileCoreOpdsUstadJSOPDSFeed *feed = [self.catalogController getModel]->opdsFeed_;
        
        NSString *title = feed->title_;
        if([self.parentViewController isKindOfClass:[UINavigationController class]]) {
            [self.navigationItem setTitle:title];
        }else if(self.parentViewController != nil){
            [self.parentViewController.navigationItem setTitle:title];
        }
        
        jint bgColor = [feed getBgColor];
        if(bgColor >= 0) {
            self.catalogBgColor =UIColorFromRGB(bgColor);
            [self.view setBackgroundColor:self.catalogBgColor];
        }
        
        jint textColor = [feed getTextColor];
        if(textColor >= 0) {
            self.catalogTextColor = UIColorFromRGB(textColor);
        }
        
        [self.catalogTableView setDataSource:self];
        [self.catalogTableView setDelegate:self];
        //[self.view layoutSubviews];
        //[self.catalogTableView layoutIfNeeded];
        [self.catalogTableView layoutSubviews];
        
        //[self.catalogTableView reloadData];
    });
    [self.catalogController loadThumbnails];
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

-(void) setBrowseButtonLabelWithNSString:(NSString *)browseButtonLabel {
    [self.browseButton setTitle:browseButtonLabel forState:UIControlStateNormal];
}

-(void)refreshWithUIRefreshControl:(UIRefreshControl *)refreshControl {
    if(!self.loadInProgress) {
        JavaUtilHashtable *newArgs = (JavaUtilHashtable *)[[self getArguments] clone];
        JavaLangInteger *flagArgs = [[JavaLangInteger alloc]initWithInt:0];
        if([newArgs containsKeyWithId:ComUstadmobileCoreControllerCatalogController_KEY_FLAGS]) {
            flagArgs =[newArgs getWithId:ComUstadmobileCoreControllerCatalogController_KEY_FLAGS];
        }
        flagArgs = [[JavaLangInteger alloc]initWithInt:([flagArgs intValue] | ComUstadmobileCoreControllerCatalogController_CACHE_DISABLED)];
        [newArgs putWithId:ComUstadmobileCoreControllerCatalogController_KEY_FLAGS withId:flagArgs];
        
        [self loadCatalogWithArguments:newArgs];
    }
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
    [self presentViewController:alertController animated:NO completion:nil];
}

- (void)setEntryStatusWithNSString:(NSString *)entryId
                           withInt:(jint)status {
    
}

- (void)setEntrythumbnailWithNSString:(NSString *)entryId
                         withNSString:(NSString *)iconFileURI {
    
    [self.idToThumbnailTable setObject:iconFileURI forKey:entryId];
    [self updateEntryThumbnail:entryId];
}

- (void)setEntryBackgroundWithNSString:(NSString *)entryId
                          withNSString:(NSString *)backgroundFileURI {
    [self.idToBackgroundTable setObject:backgroundFileURI forKey:entryId];
    [self updateEntryBackground:entryId];
}


-(void)updateEntryThumbnail:(NSString *)entryId {
    CatalogViewControllerEntryTableViewCell *cell = [self.idToCellMapTable objectForKey:entryId];
    NSString *iconFileURI = [self.idToThumbnailTable objectForKey:entryId];
    if(cell != nil && iconFileURI != nil) {
        [cell.thumbnailImageView setImage:[UIImage imageWithContentsOfFile:iconFileURI]];
    }
}

-(void)updateEntryBackground:(NSString *)entryId {
    NSString *bgURI = [self.idToBackgroundTable objectForKey:entryId];
    CatalogViewControllerEntryTableViewCell *cell = [self.idToCellMapTable objectForKey:entryId];
    if(cell != nil && bgURI != nil) {
        [cell.backgroundImageView setImage:[UIImage imageWithContentsOfFile:bgURI]];
        [cell.titleLabel setTextColor:[UIColor whiteColor]];
        [cell.rightProgressIcon setHidden:YES];
    }
}


- (void)updateDownloadAllProgressWithInt:(jint)loaded
                                 withInt:(jint)total {
    
}

- (void)setDownloadEntryProgressVisibleWithNSString:(NSString *)entryId
                                        withBoolean:(jboolean)visible {
    CatalogViewControllerEntryTableViewCell *cell = [self.idToCellMapTable objectForKey:entryId];
    if(cell != nil) {
        dispatch_async(dispatch_get_main_queue(), ^{
            [cell.progressView setHidden:!visible];
        });
    }
}

- (void)updateDownloadEntryProgressWithNSString:(NSString *)entryId
                                        withInt:(jint)loaded
                                        withInt:(jint)total {
    CatalogViewControllerEntryTableViewCell *cell = [self.idToCellMapTable objectForKey:entryId];
    NSLog([NSString stringWithFormat:@"updateDownloadEntryProgress: %d / %d", loaded, total]);
    if(cell != nil) {
        dispatch_async(dispatch_get_main_queue(), ^{
            if(total > 0) {
                [cell.progressView setProgress:(float)loaded/(float)total];
            }else {
                [cell.progressView setProgress:0];
            }
        });
    }
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
    NSInteger count;
    if(self.catalogController != nil) {
        count = [self.catalogController getModel]->opdsFeed_->entries_->size_;
    }else {
        count = 1;
    }
    
    return count;
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
    
    if(self.catalogTextColor != nil) {
        [cell.titleLabel setTextColor:self.catalogTextColor];
    }
    
    cell.titleLabel.text = item->title_;
    [cell.progressView setHidden:YES];
    [self updateEntryThumbnail:item->id__];
    [self updateEntryBackground:item->id__];
    
    if(indexPath.row >= 2) {
        [cell.rightProgressIcon setImage:[UIImage imageNamed:@"phases-progress-icon-empty"]];
    }
    
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
