import { Observable, combineLatest } from 'rxjs';
import { UmAngularUtil } from './../../util/UmAngularUtil';
import { UmDbMockService, ContentEntryDao} from './../../core/db/um-db-mock.service';
import {Component} from '@angular/core';
import {environment} from 'src/environments/environment.prod';
import { Router, ActivatedRoute, NavigationEnd } from '@angular/router';
import core from 'UstadMobile-core';
import db from 'UstadMobile-lib-database';
import util from 'UstadMobile-lib-util';
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
  
  entries : db.com.ustadmobile.lib.db.entities.ContentEntry[] = [];
  env = environment;
  label_language_options : string = "";
  label_reading_level : string = "";
  private entryListObservable: Observable<any[]> = null
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

  constructor(umService: UmBaseService, router: Router, route: ActivatedRoute, 
     umDb: UmDbMockService, formBuilder: FormBuilder) {
    super(umService, router, route, umDb);
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
      this.entries = []; 
      if(this.umDatabase.contentEntryDao){
        this.onCreate()
      }
    }); 
  }

  private onCreate(){
    if(this.umDatabase.contentEntryDao){
      this.presenter = new core.com.ustadmobile.core.controller.ContentEntryListFragmentPresenter(
        this.context, UmAngularUtil.queryParamsToMap(), this,this.umDatabase.contentEntryDao);
      this.presenter.onCreate(null);
    }
  }

  ngOnInit() {
    super.ngOnInit();

    combineLatest([
      this.umService.loadEntries(),
      this.umService.loadEntryJoins()
    ]).subscribe(responses => {
      this.umDatabase.contentEntryDao = new ContentEntryDao(responses[0], responses[1])
      this.onCreate() 
    })

    //Listen for resources being ready
    this.subscription = this.umService.getUmObserver().subscribe(content =>{
      if(content[UmAngularUtil.DISPATCH_RESOURCE]){
        this.label_language_options = this.getString(this.MessageID.also_available_in);
        this.label_reading_level = this.getString(this.MessageID.library_reading_level); 
      }
    });

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

  setContentEntryProvider(provider : Observable<any[]>){
    this.entryListObservable = provider;
    this.entryListObservable.subscribe(entries =>{ 
      this.entries = entries;
    })
  }

  openEntry(entry : db.com.ustadmobile.lib.db.entities.ContentEntry) {
    this.presenter.handleContentEntryClicked(entry);
  }

  setLanguageOptions(languages){
    const languageList = util.com.ustadmobile.lib.util.UMUtil.kotlinListToJsArray(languages);
    this.languages = languageList.splice(1,languageList.length);
  }

  setCategorySchemaSpinner(categories){
    const categoriesMap: any[] = util.com.ustadmobile.lib.util.UMUtil.kotlinCategoryMapToJsArray(categories);
    var counter = 0;
    categoriesMap.forEach(categoryList =>{  
      this.categoryMap[counter] = categoryList.splice(2,3)
      counter++;
    }) 
  }

  setToolbarTitle(title: string){
    this.umService.dispatchUpdate(UmAngularUtil.getContentToDispatch(
      UmAngularUtil.DISPATCH_TITLE, title));
  }

  ngOnDestroy(){
    super.ngOnDestroy()
    this.presenter.onDestroy();
    if (this.navigationSubscription) {  
      this.navigationSubscription.unsubscribe();
    }
  }
}
