import { UmDbMockService } from './../../core/db/um-db-mock.service';
import { ActivatedRoute, Router, NavigationEnd, NavigationStart } from '@angular/router';
import { Component } from '@angular/core';
import { environment } from 'src/environments/environment.prod';
import { UmBaseComponent } from '../um-base-component';
import { UmBaseService } from '../../service/um-base.service';
import { UmAngularUtil } from '../../util/UmAngularUtil';
import { Subscription } from 'rxjs';
import core from 'UstadMobile-core';
import util from 'UstadMobile-lib-util';
import 'rxjs/add/operator/filter';

@Component({
  selector: 'app-content-entry-detail',
  templateUrl: './content-entry-detail.component.html',
  styleUrls: ['./content-entry-detail.component.css']
})
export class ContentEntryDetailComponent extends UmBaseComponent implements
 core.com.ustadmobile.core.view.ContentEntryDetailView {

  env = environment;
  contentEntryUid = "";
  entryTitle = "";
  entryAuthor = "";
  entryLicence = "";
  entryDescription = "";
  entryThumbnail = "";
  private presenter: core.com.ustadmobile.core.controller.ContentEntryDetailPresenter;
  private subscription: Subscription;
  translations = []
  private navigationSubscription;

   constructor(umService: UmBaseService, router: Router, route: ActivatedRoute, umDb: UmDbMockService) {
    super(umService, router, route, umDb);
    
    this.navigationSubscription = this.router.events.filter(event => event instanceof NavigationEnd)
    .subscribe((event:NavigationEnd) => {
      this.presenter = new core.com.ustadmobile.core.controller.ContentEntryDetailPresenter(this.context,
        UmAngularUtil.queryParamsToMap(), this);
      this.umService.setPresenterInstance(this.presenter);
      this.presenter.onCreate(null);
    });
  }

  ngOnInit() {
    super.ngOnInit();
    this.subscription = this.umService.getUmObserver().subscribe(() =>{
      //do something
    });
  }


  openTranslation(translation){
    this.presenter.handleClickTranslatedEntry(translation.cerejRelatedEntryUid)
  }

  setContentEntry(contentEntry){
    this.contentEntryUid = contentEntry.title;
    this.entryTitle = contentEntry.title;
    this.entryAuthor = contentEntry.author;
    this.entryDescription = contentEntry.description;
    this.entryThumbnail = contentEntry.thumbnailUrl;
  }
  
  setContentEntryLicense(license){
    this.entryLicence = license;
  }

  setDetailsButtonEnabled(){}

  setDownloadSize(){}

  setAvailableTranslations(result){
    this.translations = util.ustadmobile.lib.util.UMUtil.kotlinListToJsArray(result);
  }

  updateDownloadProgress(){}

  setDownloadButtonVisible(){}

  setButtonTextLabel(){}

  showFileOpenWithMimeTypeError(){}

  showFileOpenError(message){
    this.showError(message);
  }

  updateLocalAvailabilityViews(){}

  setLocalAvailabilityStatusViewVisible(){}

  setTranslationLabelVisible(){}

  setFlexBoxVisible(){}

  setDownloadProgressVisible(){}

  setDownloadProgressLabel(){}

  setDownloadButtonClickableListener(){}

  showDownloadOptionsDialog(){}

  ngOnDestroy() {
    super.ngOnDestroy()
    this.subscription.unsubscribe();
    if (this.navigationSubscription) {  
      this.navigationSubscription.unsubscribe();
    }
  }

}
