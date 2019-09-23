import { UmAngularUtil } from './../../util/UmAngularUtil';
import {Component} from '@angular/core';
import {environment} from 'src/environments/environment.prod';
import { Router, ActivatedRoute, NavigationEnd } from '@angular/router';
import core from 'UstadMobile-core';
import db from 'UstadMobile-lib-database';
import { UmBaseComponent } from '../um-base-component';
import { UmBaseService } from '../../service/um-base.service';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';

@Component({
  selector: 'app-content-entry-list',
  templateUrl: './content-entry-list.component.html',
  styleUrls: ['./content-entry-list.component.css']
})
export class ContentEntryListComponent extends UmBaseComponent implements 
core.com.ustadmobile.core.view.ContentEntryListFragmentView {
  
  entryList = [];
  env = environment;
  label_language_options : string = "";
  label_reading_level : string = "";
  private presenter: core.com.ustadmobile.core.controller.ContentEntryListFragmentPresenter;
  languages : db.com.ustadmobile.lib.db.entities.Language[]
  categories: db.com.ustadmobile.lib.db.entities.DistinctCategorySchema[];
  categoryMap : any[][] = [];
  umFormLanguage : FormGroup;
  umFormCategories: FormGroup;
  private navigationSubscription;
  entry_thumbnail_class : string;
  entry_summary_class: string;
  entry_collection_class: string;
  entry_options_class: string;

  constructor(umService: UmBaseService, router: Router, route: ActivatedRoute,formBuilder: FormBuilder) {
    super(umService, router, route);
    this.entry_summary_class =  this.umService.isLTRDirectionality() ? "right":"left entry-summary-left";
    this.entry_options_class =  this.umService.isLTRDirectionality() ? "right":"left";
    this.entry_thumbnail_class = this.umService.isLTRDirectionality() ? "left entry-ltr":"right entry-rtl";
    this.entry_collection_class = this.umService.isLTRDirectionality() ? "":"collection-rtl"; 

    this.umFormLanguage = formBuilder.group({
      'language': ['-1', Validators.required]
    });

    this.umFormCategories = formBuilder.group({
      'category': ['-1', Validators.required]
    });
    this.navigationSubscription = this.router.events.filter(event => event instanceof NavigationEnd)
    .subscribe( _ => {
      this.entryList = []; 
      UmAngularUtil.registerResourceReadyListener(this)
    }); 
  }

  onCreate(){
    super.onCreate()
    this.presenter = new core.com.ustadmobile.core.controller.ContentEntryListFragmentPresenter(
      this.context, UmAngularUtil.queryParamsToMap(), this,this.umService.getDbInstance().contentEntryDao);
    this.presenter.onCreate(null);
    this.setToolbarTitle("...")
    this.label_language_options = this.getString(this.MessageID.also_available_in);
    this.label_reading_level = this.getString(this.MessageID.library_reading_level); 
  }

  ngOnInit() {
    super.ngOnInit();

    //setup language spinner/select listener
    this.umFormLanguage.valueChanges.subscribe((form: any) => {
        if(form.language > -1){
           this.presenter.handleClickFilterByLanguage(form.language);
         }
    });

    //setup category spinner/select listener
    this.umFormCategories.valueChanges.subscribe((form: any) => {
      if(form.category > -1){
         this.presenter.handleClickFilterByCategory(form.category);
       }
    });
  }


  setContentEntryProvider(provider: any){
    const context = this
    provider.create().load(0,100, function(error, entries) {
      context.entryList = context.entryList.concat(UmAngularUtil.kotlinListToJsArray(entries))
    }) 
  }

  setLanguageOptions(languages: any){
    const languageList = UmAngularUtil.kotlinListToJsArray(languages)
    this.languages = languageList.splice(1,languageList.length);
  }

  setCategorySchemaSpinner(categories: any){
    const categoriesMap = UmAngularUtil.kotlinCategoryMapToJSArray(categories)
    var counter = 0;
    categoriesMap.forEach(categoryList =>{  
      this.categoryMap[counter] = categoryList.splice(2,3)
      counter++;
    })  
  }

  openEntry(entry) {
    UmAngularUtil.fireTitleUpdate(entry.title)
    this.presenter.handleContentEntryClicked(entry);
  }

  ngOnDestroy(){
    super.ngOnDestroy()
    if(this.presenter){
      this.presenter.onDestroy();
    }
    if (this.navigationSubscription) {  
      this.navigationSubscription.unsubscribe();
    }
  }
}
