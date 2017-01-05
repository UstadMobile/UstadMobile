//
//  CatalogViewControllerEntryCellTableViewCell.m
//  UstadMobileIOS
//
//  Created by Mike Dawson on 21/08/16.
//  Copyright © 2016 UstadMobile FZ-LLC. All rights reserved.
//

#import "CatalogViewControllerEntryTableViewCell.h"

@interface CatalogViewControllerEntryTableViewCell ()

@property (nonatomic) BOOL dividerMode;

@end

@implementation CatalogViewControllerEntryTableViewCell

- (void)awakeFromNib {
    [super awakeFromNib];
    // Initialization code
    self.dividerMode = NO;
}

- (void)setSelected:(BOOL)selected animated:(BOOL)animated {
    [super setSelected:selected animated:animated];

    // Configure the view for the selected state
}

- (void)useAsDivider:(BOOL)dividerMode {
    self.dividerMode = dividerMode;
    
    //hide these if we are in divider mode
    [self.titleLabel setHidden:dividerMode];
    [self.thumbnailImageView setHidden:dividerMode];
    [self.dividerTitleLabel setHidden:!dividerMode];
}

@end
