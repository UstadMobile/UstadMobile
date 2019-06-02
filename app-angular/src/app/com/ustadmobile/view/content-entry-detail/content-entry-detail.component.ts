import { UmDbMockService } from './../../core/db/um-db-mock.service';
import { UmContextWrapper } from './../../util/UmContextWrapper';
import { ActivatedRoute, Router, Params, NavigationEnd } from '@angular/router';
import { Component, OnInit } from '@angular/core';
import { com as core} from 'core';
import { environment } from 'src/environments/environment.prod';
import { UmBaseComponent } from '../um-base-component';
import { UmBaseService } from '../../service/um-base.service';
import { UmAngularUtil } from '../../util/UmAngularUtil';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-content-entry-detail',
  templateUrl: './content-entry-detail.component.html',
  styleUrls: ['./content-entry-detail.component.css']
})
export class ContentEntryDetailComponent extends UmBaseComponent implements
 core.ustadmobile.core.view.ContentEntryDetailView {

  env = environment;
  contentEntryUid = "";
  entryTitle = "";
  entryLicence = "";
  entryDescription = "";
  entryThumbnail = "";
  args : Params = null;
  label_author : string = "";
  label_description : string = "";
  label_license : string = "";
  label_language_option : string = "";
  private presenter: core.ustadmobile.core.controller.ContentEntryDetailPresenter;
   private subscription: Subscription;

  entryLanguages = [
    {name: "Language 1", uid: "E130B099-5C18-E0899-6817-009BCAC1111E6"},
    {name: "Language 2", uid: "E130B099-5C18-E0899-6817-009BCAC1111E6"},
    {name: "Language 3", uid: "E130B099-5C18-E0899-6817-009BCAC1111E6"},
    {name: "Language 4", uid: "E130B099-5C18-E0899-6817-009BCAC1111E6"},
    {name: "Language 5", uid: "E130B099-5C18-E0899-6817-009BCAC1111E6"},
    {name: "Language 6", uid: "E130B099-5C18-E0899-6817-009BCAC1111E6"},
    {name: "Language 7", uid: "E130B099-5C18-E0899-6817-009BCAC1111E6"},
  ]


   

   constructor(umService: UmBaseService, router: Router, route: ActivatedRoute, umDb: UmDbMockService) {
    super(umService, router, route, umDb);
    this.args = route.snapshot.queryParams;
    
    this.router.events.subscribe((e: any) => {
      if (e instanceof NavigationEnd) {
        this.presenter = new core.ustadmobile.core.controller.ContentEntryDetailPresenter(this.context,
          UmAngularUtil.queryParamsToMap(), this);
        this.presenter.onCreate(null);
      }
    });
  }

  ngOnInit() {
    super.ngOnInit();
    this.subscription = this.umService.getUmObserver().subscribe(content =>{
      if(content[UmAngularUtil.DISPATCH_RESOURCE]){
        this.label_author = this.getString(this.MessageID.entry_details_author); 
        this.label_description = this.getString(this.MessageID.entry_details_description); 
        this.label_language_option = this.getString(this.MessageID.also_available_in); 
        this.label_license = this.getString(this.MessageID.entry_details_license); 
      }
    });
  }


  navigateToLanguage(language){
    console.log("language", language)
  }

  
  setContentEntryTitle(title: String){

  }

  setContentEntryDesc(desc: String){

  }

  setContentEntryLicense(license: String){

  }

  setContentEntryAuthor(author: String){
  
  }

  setDetailsButtonEnabled(enabled: Boolean){

  }

  setDownloadSize(fileSize: number){

  }

  loadEntryDetailsThumbnail(thumbnailUrl: String){

  }

  setAvailableTranslations(result: any, entryUuid: number){

  }

  updateDownloadProgress(progressValue: number){

  }

  setDownloadButtonVisible(visible: Boolean){

  }

  setButtonTextLabel(textLabel: String){

  }

  showFileOpenWithMimeTypeError(message: String, actionMessageId: number, mimeType: String){
    
  }

  showFileOpenError(message: String){

  }

  updateLocalAvailabilityViews(icon: number, status: String){

  }

  setLocalAvailabilityStatusViewVisible(visible: Boolean){

  }

  setTranslationLabelVisible(visible: Boolean){

  }

  setFlexBoxVisible(visible: Boolean){

  }

  setDownloadProgressVisible(visible: Boolean){

  }

  setDownloadProgressLabel(progressLabel: String){

  }

  setDownloadButtonClickableListener(isDownloadComplete: Boolean){

  }

  showDownloadOptionsDialog(hashtable: any){}

  ngOnDestroy(): void {
    super.ngOnDestroy()
    this.subscription.unsubscribe();
  }

}
