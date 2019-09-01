import { Component } from '@angular/core';
import core from 'UstadMobile-core';
import { UmBaseComponent } from '../um-base-component';
import { UmBaseService } from '../../service/um-base.service';
import { Router, ActivatedRoute, NavigationEnd } from '@angular/router';
import { UmDbMockService } from '../../core/db/um-db-mock.service';
import { UmAngularUtil } from '../../util/UmAngularUtil';
import util from 'UstadMobile-lib-util';

@Component({
  selector: 'app-xapi-report-details',
  templateUrl: './xapi-report-details.component.html',
  styleUrls: ['./xapi-report-details.component.css']
})
export class XapiReportDetailsComponent extends UmBaseComponent implements
core.com.ustadmobile.core.view.XapiReportDetailView{

  presenter: core.com.ustadmobile.core.controller.XapiReportDetailPresenter
  private navigationSubscription;

   chartTitle:string = '';
   chartType:string = '';
   chartData: any[] = [];
   columnNames: any[] = ['Year', 'Asia','Europe'];
    options = {};
    width = 900;
    height = 550;  


  constructor(umService: UmBaseService, router: Router, route: ActivatedRoute,umDb: UmDbMockService) {
    super(umService, router, route, umDb);
    this.navigationSubscription = this.router.events.filter(event => event instanceof NavigationEnd)
    .subscribe( _ => {
      this.subscription = UmAngularUtil.registerUmObserver(this)
    });
  }

  ngOnInit() {
    super.ngOnInit()
    this.subscription = UmAngularUtil.registerUmObserver(this)
  }

  onCreate(){
    super.onCreate()
    if(this.umDatabase.statementDao && this.umDatabase.xLangMapEntryDao){
      this.setToolbarTitle(this.getString(this.MessageID.activity_preview_xapi))
      const args = UmAngularUtil.queryParamsToMap(document.location.search, false)
      this.chartType = JSON.parse(util.com.ustadmobile.lib.util.UMUtil.kotlinMapToJsArray(args)[1].value).chartType == 100
       ? 'LineChart':'ColumnChart'
      this.presenter = new core.com.ustadmobile.core.controller.XapiReportDetailPresenter(this.context,
      args,this,this.systemImpl,this.umDatabase.statementDao,this.umDatabase.xLangMapEntryDao);
     this.presenter.onCreate(null)
    }
  }
  
  setChartData(chartData: any, options: any, xAxisLabels: any, subgroupLabels: any){
    //console.log("labels",util.com.ustadmobile.lib.util.UMUtil.kotlinMapToJsArray(options))
    const dataList = [];
    util.com.ustadmobile.lib.util.UMUtil.kotlinListToJsArray(chartData).forEach(chart => {
      dataList.push([chart.xAxis,chart.yAxis, +chart.subgroup]) 
    });
    this.chartData = dataList
  }
  
  setChartYAxisLabel(yAxisLabel: string){
    //console.log("setClabelshat",yAxisLabel)
  }
  
  setReportListData(listResults: any){
    //console.log("list", listResults)
  }

  handleAddToDashboard(){
    //show dialog
    this.presenter.handleAddDashboardClicked("S")
  }

  ngOnDestroy() {
    super.ngOnDestroy()
    if(this.presenter){
      this.presenter.onDestroy();
    }
    if (this.navigationSubscription) {
      this.navigationSubscription.unsubscribe();
    }
  }


}
