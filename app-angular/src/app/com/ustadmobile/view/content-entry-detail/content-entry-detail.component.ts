import { ActivatedRoute, Router, NavigationEnd } from '@angular/router';
import { Component, OnDestroy } from '@angular/core';
import { UmBaseComponent } from '../um-base-component';
import { UmBaseService } from '../../service/um-base.service';
import { UmAngularUtil } from '../../util/UmAngularUtil';
import core from 'UstadMobile-core';
import entities from 'UstadMobile-lib-database-entities'
import 'rxjs/add/operator/filter';

@Component({
  selector: 'app-content-entry-detail',
  templateUrl: './content-entry-detail.component.html',
  styleUrls: ['./content-entry-detail.component.css']
})
export class ContentEntryDetailComponent extends UmBaseComponent implements
core.com.ustadmobile.core.view.ContentEntryDetailView, OnDestroy,
  core.com.ustadmobile.core.networkmanager.LocalAvailabilityMonitor,
  entities.com.ustadmobile.core.networkmanager.DownloadJobItemStatusProvider {

    private presenter: core.com.ustadmobile.core.controller.ContentEntryDetailPresenter;
    private navigationSubscription;
    translations = [];
    entryLicence = "";
    class_entry_thumbnail: string;
    class_entry_summary: string; 
    class_availability_label: string; 
    contentEntry = new entities.com.ustadmobile.lib.db.entities.ContentEntry()

    constructor(umService: UmBaseService, router: Router, route: ActivatedRoute) {
      super(umService, router, route);
      this.class_entry_summary = this.umService.isLTRDirectionality() ? "right" : "left";
      this.class_availability_label = this.umService.isLTRDirectionality() ? "left" : "right";
      this.class_entry_thumbnail = this.umService.isLTRDirectionality() ? "left" : "right thumbnail-wrapper-right";

      this.navigationSubscription = this.router.events.filter(event => event instanceof NavigationEnd)
        .subscribe(_ => {
          UmAngularUtil.registerResourceReadyListener(this)
        });
    }

    onCreate() {
      super.onCreate()
      this.presenter = new core.com.ustadmobile.core.controller.ContentEntryDetailPresenter(this.context,
        UmAngularUtil.getArgumentsFromQueryParams(), this,false, this.umService.getDbInstance(),this.umService.getDbInstance(),
        this, this, this.getActiveAccount(),this.systemImpl);
      this.presenter.onCreate(null);
    }

    ngOnInit() {
      super.ngOnInit();
    }

    handleDownloadButtonClick() {
      this.presenter.handleDownloadButtonClick();
    }

    handleClickTranslatedEntry(translation) {
      this.presenter.handleClickTranslatedEntry(translation.cerejRelatedEntryUid)
    }

    setContentEntry(contentEntry) {
      this.contentEntry = contentEntry; 
      UmAngularUtil.fireTitleUpdate(this.contentEntry.title)
    }

    setContentEntryLicense(license) {
      this.entryLicence = license;
    }

    setDetailsButtonEnabled() {}

    setDownloadSize() {}

    setAvailableTranslations(translations) {
      const tempTranslations = []
      UmAngularUtil.kotlinListToJsArray(translations).forEach(translation => {
        if(translation.language){
          tempTranslations.push(translation)
        }
      })
      this.translations = tempTranslations;
    }

    findDownloadJobItemStatusByContentEntryUid() {}

    addDownloadChangeListener() {}

    showEditButton() {}

    removeDownloadChangeListener() {}

    updateDownloadProgress() {}

    setDownloadButtonVisible() {}

    setButtonTextLabel() {}

    showFileOpenWithMimeTypeError() {}

    showBaseProgressBar(show) {}

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

    startMonitoringAvailability() {}

    stopMonitoringAvailability() {}

    setMainButtonEnabled(enabled){}

    getDownloadJobItemByContentEntryUid(entryUuid){}

    ngOnDestroy() {
      super.ngOnDestroy()
      if (this.navigationSubscription) {
        this.navigationSubscription.unsubscribe();
      }
    }

  }
