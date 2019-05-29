import { UmDbMockService } from './../../core/db/um-db-mock.service';
import { UmContextWrapper } from './../../util/UmContextWrapper';
import { ActivatedRoute, Router, Params, NavigationEnd } from '@angular/router';
import { Component, OnInit } from '@angular/core';
import { com as core} from 'core';
import { environment } from 'src/environments/environment.prod';
import { UmBaseComponent } from '../um-base-component';
import { UmBaseService } from '../../service/um-base.service';
import { UmAngularUtil } from '../../util/UmAngularUtil';

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

  entryLanguages = [
    {name: "Language 1", uid: "E130B099-5C18-E0899-6817-009BCAC1111E6"},
    {name: "Language 2", uid: "E130B099-5C18-E0899-6817-009BCAC1111E6"},
    {name: "Language 3", uid: "E130B099-5C18-E0899-6817-009BCAC1111E6"},
    {name: "Language 4", uid: "E130B099-5C18-E0899-6817-009BCAC1111E6"},
    {name: "Language 5", uid: "E130B099-5C18-E0899-6817-009BCAC1111E6"},
    {name: "Language 6", uid: "E130B099-5C18-E0899-6817-009BCAC1111E6"},
    {name: "Language 7", uid: "E130B099-5C18-E0899-6817-009BCAC1111E6"},
  ]

  /* constructor(private router: Router, private route: ActivatedRoute, private dataService: UmDbMockService) {
    this.context = new UmContextWrapper(router);
    this.route.params.subscribe(val => {
      this.contentEntryUid = val.entryUid;
      const entry = this.dataService[dataService.ROOT_UID][0];
      this.entryTitle = entry.entry_name;
      this.entryDescription = entry.entry_description;
      this.entryThumbnail = entry.entry_image;
      this.entryLicence = entry.entry_licence;
    });
    this.args = this.route.snapshot.queryParams;
   } */

   private presenter: core.ustadmobile.core.controller.ContentEntryDetailPresenter;

   constructor(localeService: UmBaseService, router: Router, route: ActivatedRoute, private umDb: UmDbMockService) {
    super(localeService, router, route, umDb);
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
   
  }

  navigateToLanguage(language){
    console.log("language", language)
  }


  setContentEntryTitle(title: string){

  }

  ngOnDestroy(): void {
    super.ngOnDestroy()
  }

}
