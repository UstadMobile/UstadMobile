import { UmAngularUtil } from './../../util/UmAngularUtil';
import { Component, Renderer2, ElementRef } from '@angular/core';
import { UmBaseComponent } from '../um-base-component';
import { UmBaseService } from '../../service/um-base.service';
import { Router, ActivatedRoute, NavigationEnd } from '@angular/router';
import { UmDbMockService } from '../../core/db/um-db-mock.service';
import core from 'UstadMobile-core';
import util from 'UstadMobile-lib-util';
import { Subscription } from 'rxjs';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';

@Component({
  selector: 'app-xapi-report-options', 
  templateUrl: './xapi-report-options.component.html',
  styleUrls: ['./xapi-report-options.component.css']
})
export class XapiReportOptionsComponent extends UmBaseComponent implements
core.com.ustadmobile.core.view.XapiReportOptionsView {

 presenter: core.com.ustadmobile.core.controller.XapiReportOptionsPresenter;
 private navigationSubscription;
 subscription: Subscription;
 umFormReportOptions : FormGroup;
 translatedYAxisList = []
 translatedGraphList = []
 translatedXAxisList = []
 whoList = []
 didList = []
 whatList = []
 whenList = []

 constructor(umService: UmBaseService, router: Router, route: ActivatedRoute, 
   umDb: UmDbMockService, formBuilder: FormBuilder) { 
     super(umService, router, route, umDb);

     this.umFormReportOptions = formBuilder.group({
      'visualization': ['', Validators.required],
      'y_axis': ['', Validators.required],
      'x_axis': ['', Validators.required],
      'sub_group': ['', Validators.required],
      'graph_who': ['', Validators.required],
      'graph_did': ['', Validators.required],
      'graph_what': ['', Validators.required],
      'graph_when': ['', Validators.required]
    });

    this.umFormReportOptions.valueChanges.subscribe(
      () => { });

     this.navigationSubscription = this.router.events.filter(event => event instanceof NavigationEnd)
     .subscribe( _ => { if(this.umDatabase.xObjectDao){
       this.subscription = this.umService.getUmObserver().subscribe(content => {
        if (content[UmAngularUtil.DISPATCH_RESOURCE]) {
          this.onCreate()
        }
      });
     }
   }); 
   }

   private onCreate(){
     if(this.umDatabase.xObjectDao){
       this.presenter = new core.com.ustadmobile.core.controller.XapiReportOptionsPresenter(
         this.context, UmAngularUtil.queryParamsToMap(),this, this.umDatabase.personDao,
         this.umDatabase.xObjectDao, this.umDatabase.xLangMapEntryDao);
       this.presenter.onCreate(null);
       this.setToolbarTitle(this.getString(this.MessageID.xapi_options_report_title)) 
     }
   }

   fillVisualChartType(graphList){
     this.translatedGraphList = util.com.ustadmobile.lib.util.UMUtil.kotlinListToJsArray(graphList)
   }
   
   fillYAxisData(translatedYAxisList){
     this.translatedYAxisList = util.com.ustadmobile.lib.util.UMUtil.kotlinListToJsArray(translatedYAxisList)
   }

   fillXAxisAndSubGroupData(translatedXAxisList){
    this.translatedXAxisList = util.com.ustadmobile.lib.util.UMUtil.kotlinListToJsArray(translatedXAxisList)
   }

   updateWhoDataAdapter(whoList){
     this.whoList = whoList;
     console.log("whoList",whoList)
   }

   updateDidDataAdapter(didList){
     this.didList = didList;
     console.log("didList",didList)
   }

   updateFromDialogText(fromDate){
     console.log("fromDate",fromDate)
   }

   updateToDialogText(toDate){
     console.log("toDate",toDate)
   }

   updateWhenRangeText(rangeText){
     console.log("rangeText",rangeText)
   }

   updateChartTypeSelected(indexChart){
     console.log("indexChart",indexChart)
   }

   updateYAxisTypeSelected(indexYAxis){
     console.log("indexYAxis",indexYAxis)
   }

   updateXAxisTypeSelected(indexXAxis){
     console.log("indexXAxis",indexXAxis)
   }

   updateSubgroupTypeSelected(indexSubgroup){
     console.log("indexesSub",indexSubgroup)
   }

   updateWhoListSelected(personList){
     console.log("persons",personList)
   }

   updateDidListSelected(verbs){
     console.log("verbs", verbs)
   }

   handleOptionsSelected(){

   }

   ngOnDestroy(){
     super.ngOnDestroy()
     this.presenter.onDestroy();
     if (this.navigationSubscription) {  
       this.navigationSubscription.unsubscribe();
     }
   }

}

