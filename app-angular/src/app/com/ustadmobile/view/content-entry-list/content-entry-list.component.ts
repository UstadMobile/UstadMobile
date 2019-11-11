import { UmAngularUtil } from './../../util/UmAngularUtil';
import {Component, NgZone, OnDestroy} from '@angular/core';
import { Router, ActivatedRoute, NavigationEnd } from '@angular/router';
import core from 'UstadMobile-core';
import { UmBaseComponent } from '../um-base-component';
import { UmBaseService } from '../../service/um-base.service';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';

@Component({
  selector: 'app-content-entry-list',
  templateUrl: './content-entry-list.component.html',
  styleUrls: ['./content-entry-list.component.css']
})

export class ContentEntryListComponent extends UmBaseComponent implements OnDestroy,
core.com.ustadmobile.core.view.ContentEntryListFragmentView , OnDestroy{

  entryList = [];
  label_language_options: string = "";
  label_reading_level: string = "";
  presenter: core.com.ustadmobile.core.controller.ContentEntryListFragmentPresenter;
  languages = []
  categories = []
  umFilterForm: FormGroup;
  navigationSubscription;
  class_entry_thumbnail: string;
  class_entry_summary: string;
  class_entry_collection: string;
  class_entry_options: string;
  scrollDistance = 2
  scrollThrottle = 300
  maxItemsPerPage = 20 
  pageIndex = 1 
  private provider: any = null;

  constructor(umService: UmBaseService, router: Router, route: ActivatedRoute,
     formBuilder: FormBuilder, private zone:NgZone) {
    super(umService, router, route);
    this.class_entry_summary = this.umService.isLTRDirectionality() ? "right" : "left entry-summary-left";
    this.class_entry_options = this.umService.isLTRDirectionality() ? "right" : "left";
    this.class_entry_thumbnail = this.umService.isLTRDirectionality() ? "left entry-ltr" : "right entry-rtl";
    this.class_entry_collection = this.umService.isLTRDirectionality() ? "" : "collection-rtl";

    this.umFilterForm = formBuilder.group({
      'language': ['-1', Validators.required],
      'category': ['-1', Validators.required]
    });

    this.navigationSubscription = this.router.events.filter(event => event instanceof NavigationEnd)
      .subscribe(_ => {
        this.entryList = [];
        this.categories = []
        this.languages = []
        UmAngularUtil.registerResourceReadyListener(this)
      });
  }

  onCreate() {
    super.onCreate()
    this.pageIndex = 1
    this.presenter = new core.com.ustadmobile.core.controller.ContentEntryListFragmentPresenter(
      this.context, UmAngularUtil.getArgumentsFromQueryParams(), this, this.umService.getDbInstance().contentEntryDao);
    this.presenter.onCreate(null);
    this.setToolbarTitle("...")
    this.label_language_options = this.getString(this.MessageID.also_available_in);
    this.label_reading_level = this.getString(this.MessageID.library_reading_level);
  }

  ngOnInit() {
    super.ngOnInit();

    //setup language spinner/select listener
    this.umFilterForm.valueChanges.subscribe((form: any) => {
      if (form.language != -1) {
        this.presenter.handleClickFilterByLanguage(form.language);
      }

       if (form.category != -1) { 
        this.presenter.handleClickFilterByCategory(form.category);
      }
    });
  }

  onScrollDown(){
    this.pageIndex +=  this.pageIndex  
    this.loadPaggedList()
  }


  setContentEntryProvider(provider: any) {
    this.provider = provider.create()
    if(this.umFilterForm.value.language != -1 || this.umFilterForm.value.category != -1){
      this.entryList = []
      this.pageIndex = 1 
    }
    this.loadPaggedList()
  }

  private loadPaggedList(){
    const context = this
    const startIndex = (this.pageIndex - 1) * this.maxItemsPerPage
    const endIndex = startIndex + this.maxItemsPerPage
    this.provider.load(startIndex, endIndex, function (error, entries) {
      if(!error){
        context.zone.run( ()=>{
          context.entryList = context.entryList.concat(UmAngularUtil.kotlinListToJsArray(entries))
        })
      }else{
        console.error(error)
      }
    })
    
  }

  getLicenceType(licenseType){ 
    return core.com.ustadmobile.core.controller.ContentEntryDetailPresenter.Companion.getLicenseType(licenseType)
  }

  setLanguageOptions(languages: any) {
    const languageList = UmAngularUtil.kotlinListToJsArray(languages)
    this.zone.run(() =>{
      this.languages = languageList.splice(1, languageList.length);
    })
  }

  setCategorySchemaSpinner(map: any) {
    const categoryList = UmAngularUtil.kotlinCategoryMapToJSArray(map)
    var counter = 0;
    const categoryMap: any[][] = []
    categoryList.forEach(categories => {
      categoryMap[counter] = categories.splice(1, 4)
      counter++;
    }) 
   this.zone.run(() =>{
    this.categories = categoryMap 
   })  
  }

  handleContentEntryClicked(entry) {
    UmAngularUtil.fireTitleUpdate(entry.title)
    this.presenter.handleContentEntryClicked(entry);
  }

  ngOnDestroy() {
    super.ngOnDestroy()
    if (this.presenter) {
      this.presenter.onDestroy();
    }
    if (this.navigationSubscription) {
      this.navigationSubscription.unsubscribe();
    }
  }
}
