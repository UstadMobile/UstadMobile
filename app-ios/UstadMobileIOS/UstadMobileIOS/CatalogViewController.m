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
#import "CatalogEntryInfo.h"
#import "AppView.h"
#import "java/lang/Integer.h"
#import "java/util/Vector.h"


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
@property NSMapTable *idToStatusIconTable;
@property UIColor *catalogTextColor;
@property UIColor *catalogBgColor;
@property UILongPressGestureRecognizer *longPressGestureRecognizer;
@property UIRefreshControl *refreshControl;
@property UIAlertController *actionSheetAlertController;
@property (retain, nonatomic) IBOutlet UIImageView *backgroundImageVIew;

@property ComUstadmobileCoreOpdsUstadJSOPDSEntry *entrySelectedByLongPress;

@property BOOL loadInProgress;

@end

@implementation CatalogViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    self.idToCellMapTable = [NSMapTable strongToWeakObjectsMapTable];
    self.idToThumbnailTable = [NSMapTable strongToStrongObjectsMapTable];
    self.idToBackgroundTable = [NSMapTable strongToStrongObjectsMapTable];
    self.idToStatusIconTable = [NSMapTable strongToStrongObjectsMapTable];
    
    //[self.catalogTableView setTranslatesAutoresizingMaskIntoConstraints:YES];
    
    ComUstadmobileCoreImplUstadMobileSystemImpl *impl = [ComUstadmobileCoreImplUstadMobileSystemImpl getInstance];
    [self.browseButton setTitle:[impl getStringWithInt:ComUstadmobileCoreMessageIDConstants_browse_feeds] forState:UIControlStateNormal];
    self.refreshControl =[[UIRefreshControl alloc] init];
    [self.refreshControl addTarget:self action:@selector(refreshWithUIRefreshControl:) forControlEvents:UIControlEventValueChanged];
    [self.catalogTableView addSubview:self.refreshControl];
    
    self.longPressGestureRecognizer = [[UILongPressGestureRecognizer alloc] initWithTarget:self action:@selector(handleLongPress:)];
    self.longPressGestureRecognizer.minimumPressDuration = 2.0;
    [self.catalogTableView addGestureRecognizer:self.longPressGestureRecognizer];
    [self loadCatalog];
}


-(void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    [self setNavigationBarBackgroundColor];
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
        
        if(self.catalogController != nil) {
            ComUstadmobileCoreOpdsUstadJSOPDSFeed *feed = [self.catalogController getModel]->opdsFeed_;
            
            NSString *title = feed->title_;
            if([self.parentViewController isKindOfClass:[UINavigationController class]]) {
                [self.navigationItem setTitle:title];
            }else if(self.parentViewController != nil){
                [self.parentViewController.navigationItem setTitle:title];
            }
            
            //see if we should show a "custom lesson" button
            if([[self.catalogController getModel]->opdsFeed_ isAcquisitionFeed]) {
                UIBarButtonItem *customButton = [[UIBarButtonItem alloc] initWithTitle:@"Custom Lesson" style:UIBarButtonItemStylePlain target:self action:@selector(goCustomLesson)];
                self.navigationItem.rightBarButtonItem = customButton;
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
            [self.catalogTableView layoutSubviews];
        }else {
            ComUstadmobileCoreImplUstadMobileSystemImpl *impl =[ComUstadmobileCoreImplUstadMobileSystemImpl getInstance];
            [[impl getAppViewWithId:self] showAlertDialogWithNSString:[impl getStringWithInt:ComUstadmobileCoreMessageIDConstants_error] withNSString:[impl getStringWithInt:ComUstadmobileCoreMessageIDConstants_error_loading_catalog]];
        }
        
        
    });
    [self.catalogController loadThumbnails];
}

- (void)goCustomLesson {
    [self.catalogController handleCatalogSelectedWithNSString:@"http://www.ustadmobile.com/files/sapienza/opds/german-custom.opds"];
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
    NSString *imgName = nil;
    if(status == ComUstadmobileCoreControllerCatalogEntryInfo_ACQUISITION_STATUS_ACQUIRED) {
        imgName = @"ic_check_white_18pt";
    }
    
    if(imgName != nil) {
        [self.idToStatusIconTable setObject:imgName forKey:entryId];
    }else {
        [self.idToStatusIconTable removeObjectForKey:entryId];
    }
    
    [self updateEntryStatusIcon:entryId];
}

-(void)updateEntryStatusIcon:(NSString *)entryId {
    CatalogViewControllerEntryTableViewCell *cell = [self.idToCellMapTable objectForKey:entryId];
    NSString *iconName = [self.idToStatusIconTable objectForKey:entryId];
    if(cell != nil) {
        dispatch_async(dispatch_get_main_queue(), ^{
            if(iconName != nil) {
                [cell.statusIcon setImage:[UIImage imageNamed:iconName]];
            }else {
                [cell.statusIcon setImage:nil];
            }
        });
    }
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

- (void)setCatalogBackgroundWithNSString:(NSString *)backgroundFileURI {
    if(backgroundFileURI != nil) {
        NSString *filePath = [[ComUstadmobileCoreImplUstadMobileSystemImpl getInstance] resolveFileUriToPathWithNSString:backgroundFileURI];
        [self.backgroundImageVIew setImage:[UIImage imageWithContentsOfFile:filePath]];
    }
}


-(void)updateEntryThumbnail:(NSString *)entryId {
    CatalogViewControllerEntryTableViewCell *cell = [self.idToCellMapTable objectForKey:entryId];
    NSString *iconFileURI = [self.idToThumbnailTable objectForKey:entryId];
    if(cell != nil && iconFileURI != nil) {
        dispatch_async(dispatch_get_main_queue(), ^{
            NSString *filePath = [[ComUstadmobileCoreImplUstadMobileSystemImpl getInstance] resolveFileUriToPathWithNSString:iconFileURI];
            [cell.thumbnailImageView setImage:[UIImage imageWithContentsOfFile:filePath]];
        });
    }
}

-(void)updateEntryBackground:(NSString *)entryId {
    NSString *bgURI = [self.idToBackgroundTable objectForKey:entryId];
    CatalogViewControllerEntryTableViewCell *cell = [self.idToCellMapTable objectForKey:entryId];
    if(cell != nil && bgURI != nil) {
        dispatch_async(dispatch_get_main_queue(), ^{
            NSString *filePath = [[ComUstadmobileCoreImplUstadMobileSystemImpl getInstance] resolveFileUriToPathWithNSString:bgURI];
            [cell.backgroundImageView setImage:[UIImage imageWithContentsOfFile:filePath]];
            [cell.titleLabel setTextColor:[UIColor whiteColor]];
            [cell.rightProgressIcon setHidden:YES];
        });
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

-(void)handleLongPress:(UILongPressGestureRecognizer *)recognizer {
    if (recognizer.state == UIGestureRecognizerStateEnded) {
        CGPoint point = [recognizer locationInView:self.catalogTableView];
        NSIndexPath *indexPath = [self.catalogTableView indexPathForRowAtPoint:point];
        if(indexPath != nil) {
            self.entrySelectedByLongPress = IOSObjectArray_Get([self.catalogController getModel]->opdsFeed_->entries_, (jint)indexPath.row);
            self.actionSheetAlertController = [UIAlertController alertControllerWithTitle:self.entrySelectedByLongPress->title_ message:nil preferredStyle:UIAlertControllerStyleActionSheet];
            
            [self.actionSheetAlertController addAction:[UIAlertAction actionWithTitle:@"Cancel" style:UIAlertActionStyleCancel handler:^(UIAlertAction *action) {
                [self dismissViewControllerAnimated:NO completion:nil];
            }]];
            
            [self.actionSheetAlertController addAction:[UIAlertAction actionWithTitle:@"Delete" style:UIAlertActionStyleDestructive handler:^(UIAlertAction *action) {
                [self dismissViewControllerAnimated:NO completion:nil];
                
                IOSObjectArray *entryArr = [IOSObjectArray arrayWithObjects:(id[]){self.entrySelectedByLongPress} count:1 type:ComUstadmobileCoreOpdsUstadJSOPDSEntry_class_()];
                [self.catalogController handleClickDeleteEntriesWithComUstadmobileCoreOpdsUstadJSOPDSEntryArray:entryArr];
            }]];
            
            [self presentViewController:self.actionSheetAlertController animated:NO completion:nil];
        }
    }
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
        [cell.dividerTitleLabel setTextColor:self.catalogTextColor];
    }
    
    cell.titleLabel.text = item->title_;
    [self updateEntryThumbnail:item->id__];
    [self updateEntryBackground:item->id__];
    [self updateEntryStatusIcon:item->id__];
    
    [cell.progressView setHidden:YES];
    
    
    if( [[item getLinks]size] == 0) {
        cell.dividerTitleLabel.text = item->title_;
        [cell useAsDivider:YES];
    }else {
        [cell useAsDivider:NO];
    }
    
    if(indexPath.row >= 2) {
        [cell.rightProgressIcon setImage:[UIImage imageNamed:@"phases-progress-icon-empty"]];
    }
    
    return cell;
}

-(void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    [tableView deselectRowAtIndexPath:indexPath animated:NO];
    ComUstadmobileCoreOpdsUstadJSOPDSEntry *item = IOSObjectArray_Get([self.catalogController getModel]->opdsFeed_->entries_, (jint)indexPath.row);
    if([[item getLinks]size] > 0) {
        [self.catalogController handleClickEntryWithComUstadmobileCoreOpdsUstadJSOPDSEntry:item];
    }
}

- (IBAction)browseButtonClicked:(UIButton *)sender {
    [self.catalogController handleClickBrowseButton];
}
@end
