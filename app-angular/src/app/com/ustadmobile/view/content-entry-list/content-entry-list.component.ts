import { Observable, Subscription } from 'rxjs';
import { UmAngularUtil } from './../../util/UmAngularUtil';
import { UmDbMockService, ContentEntry } from './../../core/db/um-db-mock.service';
import {Component} from '@angular/core';
import {environment} from 'src/environments/environment.prod';
import { Router, ActivatedRoute, NavigationEnd } from '@angular/router';
import core  from 'UstadMobile-core';
import db  from 'UstadMobile-lib-database';
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
  
  entries : db.ustadmobile.lib.db.entities.ContentEntry[] = [];
  env = environment;
  label_language_options : string = "";
  label_reading_level : string = "";
  private entryListObservable: Observable<ContentEntry[]> = null
  private presenter: core.com.ustadmobile.core.controller.ContentEntryListFragmentPresenter;
  languages : db.ustadmobile.lib.db.entities.Language[]
  categories: db.ustadmobile.lib.db.entities.DistinctCategorySchema[];
  categoryMap : any[][] = [];
  umFormLanguage : FormGroup;
  umFormCategories: FormGroup;
  private subscription: Subscription;
  private navigationSubscription;

  constructor(umService: UmBaseService, router: Router, route: ActivatedRoute, 
     umDb: UmDbMockService, formBuilder: FormBuilder) {
    super(umService, router, route, umDb);

    this.umFormLanguage = formBuilder.group({
      'language': ['-1', Validators.required]
    });

    this.umFormCategories = formBuilder.group({
      'category': ['-1', Validators.required]
    });
  
    this.navigationSubscription = this.router.events.filter(event => event instanceof NavigationEnd)
    .subscribe((event:NavigationEnd) => {
      this.entries = [];
        this.presenter = new core.com.ustadmobile.core.controller
        .ContentEntryListFragmentPresenter(this.context, UmAngularUtil.queryParamsToMap(), this);
        this.umService.setPresenterInstance(this.presenter);
        this.presenter.onCreate(null);
    });

    
  }

  ngOnInit() {
    super.ngOnInit();

    //Listen for resources being ready
    this.subscription = this.umService.getUmObserver().subscribe(content =>{
      if(content[UmAngularUtil.DISPATCH_RESOURCE]){
        this.label_language_options = this.getString(this.MessageID.also_available_in);
        this.label_reading_level = this.getString(this.MessageID.label_reading_level); 
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

  setContentEntryProvider(provider : Observable<ContentEntry[]>){
    this.entryListObservable = provider;
    this.entryListObservable.subscribe(entries =>{ 
      this.entries = entries;
    })
  }

  openEntry(entry : db.ustadmobile.lib.db.entities.ContentEntry) {
    this.presenter.handleContentEntryClicked(entry);
  }

  setLanguageOptions(languages){
    const languageList = util.ustadmobile.lib.util.UMUtil.kotlinListToJsArray(languages);
    this.languages = languageList.splice(1,languageList.length);
  }

  setCategorySchemaSpinner(categories){
    const categoriesMap: any[] = util.ustadmobile.lib.util.UMUtil.kotlinCategoryMapToJsArray(categories);
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
    this.subscription.unsubscribe();
    if (this.navigationSubscription) {  
      this.navigationSubscription.unsubscribe();
    }
  }
}
