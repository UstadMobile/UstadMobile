import { UmContextWrapper } from './../../util/UmContextWrapper';
import { dataSample } from './../../util/UmDataSample';
import { ActivatedRoute, Router, Params } from '@angular/router';
import { Component, OnInit } from '@angular/core';
import { environment } from 'src/environments/environment.prod';

@Component({
  selector: 'app-content-entry-detail',
  templateUrl: './content-entry-detail.component.html',
  styleUrls: ['./content-entry-detail.component.css']
})
export class ContentEntryDetailComponent implements OnInit {

  env = environment;
  contentEntryUid = "";
  entryTitle = "";
  entryLicence = "";
  entryDescription = "";
  entryThumbnail = "";
  args : Params = null;

  context : UmContextWrapper;


  entryLanguages = [
    {name: "Language 1", uid: "E130B099-5C18-E0899-6817-009BCAC1111E6"},
    {name: "Language 2", uid: "E130B099-5C18-E0899-6817-009BCAC1111E6"},
    {name: "Language 3", uid: "E130B099-5C18-E0899-6817-009BCAC1111E6"},
    {name: "Language 4", uid: "E130B099-5C18-E0899-6817-009BCAC1111E6"},
    {name: "Language 5", uid: "E130B099-5C18-E0899-6817-009BCAC1111E6"},
    {name: "Language 6", uid: "E130B099-5C18-E0899-6817-009BCAC1111E6"},
    {name: "Language 7", uid: "E130B099-5C18-E0899-6817-009BCAC1111E6"},
  ]

  constructor(private router: Router, private route: ActivatedRoute) {
    this.context = new UmContextWrapper(router);
    this.route.params.subscribe(val => {
      this.contentEntryUid = val.entryUid;
      const entry = dataSample["E130B099-5C18-E0899-6817-009BCAC1111E6"][0];
      this.entryTitle = entry.entry_name;
      this.entryDescription = entry.entry_description;
      this.entryThumbnail = entry.entry_image;
      this.entryLicence = entry.entry_licence;
    });
    this.args = this.route.snapshot.queryParams;
   }

  ngOnInit() {
    //this.presenter = ContentEntryDetailPresenter(this.context, );
  }

  navigateToLanguage(language){
    console.log("language", language)
  }


  setContentEntryTitle(title: string){

  }

  ngOnDestroy(): void {}

}
