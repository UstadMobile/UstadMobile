import { UmDbMockService } from './../../core/db/um-db-mock.service';
import { ActivatedRoute, Router, NavigationEnd } from '@angular/router';
import { Component } from '@angular/core';
import { environment } from 'src/environments/environment.prod';
import { UmBaseComponent } from '../um-base-component';
import { UmBaseService } from '../../service/um-base.service';
import { UmAngularUtil } from '../../util/UmAngularUtil';
import core from 'UstadMobile-core';
import util from 'UstadMobile-lib-util';
import 'rxjs/add/operator/filter';

@Component({
  selector: 'app-content-entry-detail',
  templateUrl: './content-entry-detail.component.html',
  styleUrls: ['./content-entry-detail.component.css']
})
export class ContentEntryDetailComponent extends UmBaseComponent implements
 core.com.ustadmobile.core.view.ContentEntryDetailView, 
 core.com.ustadmobile.core.networkmanager.LocalAvailabilityMonitor {
env = environment;
contentEntryUid = "";
entryTitle = "";
entryAuthor = "";
entryLicence = "";
entryDescription = "";
entryThumbnail = "";
private presenter: core.com.ustadmobile.core.controller.ContentEntryDetailPresenter;
translations = []
private navigationSubscription;
entry_thumbnail_class: string;
entry_summary_class: string;

constructor(umService: UmBaseService, router: Router, route: ActivatedRoute, umDb: UmDbMockService) {
  super(umService, router, route, umDb);
  this.entry_summary_class = this.umService.isLTRDirectionality() ? "right" : "left";
  this.entry_thumbnail_class = this.umService.isLTRDirectionality() ? "left" : "right thumbnail-wrapper-right";

  this.navigationSubscription = this.router.events.filter(event => event instanceof NavigationEnd)
    .subscribe(_ => {
      if (this.umDatabase.contentEntryDao) {
        this.onCreate();
      }
    });
}

onCreate() {
  if(this.umDatabase.contentEntryDao){
    this.presenter = new core.com.ustadmobile.core.controller.ContentEntryDetailPresenter(this.context,
      UmAngularUtil.queryParamsToMap(), this,this,this,this.umDatabase);
    this.presenter.onCreate(null);
  }
}

ngOnInit() {
  super.ngOnInit();
  this.subscription = this.umService.getUmObserver().subscribe(content => {
    if (content[UmAngularUtil.DISPATCH_RESOURCE]) {
      this.onCreate()
    }
  });
}

openEntry() {
  this.presenter.handleDownloadButtonClick(true, this.contentEntryUid);
}

openTranslation(translation) {
  this.presenter.handleClickTranslatedEntry(translation.cerejRelatedEntryUid)
}

setContentEntry(contentEntry) {
  this.contentEntryUid = contentEntry.contentEntryUid;
  this.entryTitle = contentEntry.title;
  this.entryAuthor = contentEntry.author;
  this.entryDescription = contentEntry.description;
  this.entryThumbnail = contentEntry.thumbnailUrl;
}

setContentEntryLicense(license) {
  this.entryLicence = license;
}

setDetailsButtonEnabled() {}

setDownloadSize() {}

setAvailableTranslations(result) {
  this.translations = util.com.ustadmobile.lib.util.UMUtil.kotlinListToJsArray(result);
}

updateDownloadProgress() {}

setDownloadButtonVisible() {}

setButtonTextLabel() {}

showFileOpenWithMimeTypeError() {}

showFileOpenError(message) {
  this.showError(message);
}

updateLocalAvailabilityViews() {}

setLocalAvailabilityStatusViewVisible() {}

setTranslationLabelVisible() {}

setFlexBoxVisible() {}

setDownloadProgressVisible() {}

setDownloadProgressLabel() {}

setDownloadButtonClickableListener() {}

showDownloadOptionsDialog() {}

startMonitoringAvailability(monitor, entryUidsToMonitor){}

stopMonitoringAvailability(monitor){}

ngOnDestroy() {
  super.ngOnDestroy()
  this.subscription.unsubscribe();
  if (this.navigationSubscription) {
    this.navigationSubscription.unsubscribe();
  }
}

}
