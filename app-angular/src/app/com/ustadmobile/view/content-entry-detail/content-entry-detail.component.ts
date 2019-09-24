import { ActivatedRoute, Router, NavigationEnd } from '@angular/router';
import { Component } from '@angular/core';
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
core.com.ustadmobile.core.view.ContentEntryDetailView,
  core.com.ustadmobile.core.networkmanager.LocalAvailabilityMonitor,
  entities.com.ustadmobile.core.networkmanager.DownloadJobItemStatusProvider {

    private presenter: core.com.ustadmobile.core.controller.ContentEntryDetailPresenter;
    private navigationSubscription;
    translations = [];
    entryLicence = "";
    entry_thumbnail_class: string;
    entry_summary_class: string;
    contentEntry = new entities.com.ustadmobile.lib.db.entities.ContentEntry()

    constructor(umService: UmBaseService, router: Router, route: ActivatedRoute) {
      super(umService, router, route);
      this.entry_summary_class = this.umService.isLTRDirectionality() ? "right" : "left";
  this.entry_thumbnail_class = this.umService.isLTRDirectionality() ? "left" : "right thumbnail-wrapper-right";

      this.navigationSubscription = this.router.events.filter(event => event instanceof NavigationEnd)
        .subscribe(_ => {
          UmAngularUtil.registerResourceReadyListener(this)
        });
    }

    onCreate() {
      super.onCreate()
      this.presenter = new core.com.ustadmobile.core.controller.ContentEntryDetailPresenter(this.context,
        UmAngularUtil.getArgumentsFromQueryParams(), this, this, this, this.umService.getDbInstance());
      this.presenter.onCreate(null);
    }

    ngOnInit() {
      super.ngOnInit();
    }

    openEntry() {
      this.presenter.handleDownloadButtonClick();
    }

    openTranslation(translation) {
      this.presenter.handleClickTranslatedEntry(translation.cerejRelatedEntryUid)
    }

    setContentEntry(contentEntry) {
      this.contentEntry = contentEntry;
    }

    setContentEntryLicense(license) {
      this.entryLicence = license;
    }

    setDetailsButtonEnabled() {}

    setDownloadSize() {}

    setAvailableTranslations(translations) {
      this.translations = UmAngularUtil.kotlinListToJsArray(translations) 
    }

    findDownloadJobItemStatusByContentEntryUid() {}

    addDownloadChangeListener() {}

    showEditButton() {}

    removeDownloadChangeListener() {}

    updateDownloadProgress() {}

    setDownloadButtonVisible() {}

    setButtonTextLabel() {}

    showFileOpenWithMimeTypeError() {}

    showBaseProgressBar() {}

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

    ngOnDestroy() {
      super.ngOnDestroy()
      if (this.navigationSubscription) {
        this.navigationSubscription.unsubscribe();
      }
    }

  }
