//
//  CatalogViewControllerEntryCellTableViewCell.h
//  UstadMobileIOS
//
//  Created by Mike Dawson on 21/08/16.
//  Copyright © 2016 UstadMobile FZ-LLC. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface CatalogViewControllerEntryTableViewCell : UITableViewCell

@property (nonatomic, weak) IBOutlet UILabel *titleLabel;
@property (nonatomic, weak) IBOutlet UIProgressView *progressView;
@property (retain, nonatomic) IBOutlet UIImageView *thumbnailImageView;
@property (retain, nonatomic) IBOutlet UIImageView *backgroundImageView;
@property (retain, nonatomic) IBOutlet UIImageView *rightProgressIcon;
@property (retain, nonatomic) IBOutlet UIImageView *statusIcon;
@property (retain, nonatomic) IBOutlet UILabel *dividerTitleLabel;

- (void)useAsDivider:(BOOL)dividerMode;

@end
