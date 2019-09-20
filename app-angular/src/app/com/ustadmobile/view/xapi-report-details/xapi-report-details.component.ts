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
core.com.ustadmobile.core.view.XapiReportDetailView {

  presenter: core.com.ustadmobile.core.controller.XapiReportDetailPresenter
  private navigationSubscription;

  chartTitle: string = '';
  chartType: string = '';
  chartData: any[] = [];
  inMemoryChartData: any[] = []
  columnNames: any[] = [];
  inMemoryColumnNames: any[] = []
  tableDataList = []
  options = {
    title: '',
    colors: ["#009688", "#FF9800", "#2196F3", "#f44336", "#673AB7", "#607D8B", "#E91E63", "#9C27B0", "#795548", "9E9E9E", "#4CAF50"], 
    vAxis: {
      title: ''
    }
  };
  width = 900;
  height = 550;


  constructor(umService: UmBaseService, router: Router, route: ActivatedRoute) {
    super(umService, router, route);
    this.navigationSubscription = this.router.events.filter(event => event instanceof NavigationEnd)
      .subscribe(_ => {
        UmAngularUtil.registerResourceReadyListener(this)
      });
  }

  ngOnInit() {
    super.ngOnInit()
  }

  onCreate() {
    super.onCreate()
    UmAngularUtil.fireTitleUpdate(this.getString(this.MessageID.activity_preview_xapi))
    const args = UmAngularUtil.queryParamsToMap(document.location.search, false)
    this.chartType = JSON.parse(util.com.ustadmobile.lib.util.UMUtil.kotlinMapToJsArray(args)[1].value).chartType == 100 ?
      'ColumnChart' : 'LineChart'
    this.presenter = new core.com.ustadmobile.core.controller.XapiReportDetailPresenter(this.context,
      args, this, this.systemImpl, this.umService.getDbInstance().statementDao, 
      this.umService.getDbInstance().xLangMapEntryDao);
    this.presenter.onCreate(null)
  }

  setChartData(chartData: any, options: any, xAxisLabels: any, subgroupLabels: any) {
    const rawData = util.com.ustadmobile.lib.util.UMUtil.kotlinListToJsArray(chartData)
    const formattedData = UmAngularUtil.getGoogleChartFormattedData(rawData, this)
    this.inMemoryChartData = formattedData.data
    this.inMemoryColumnNames = formattedData.columns
    this.protGraph()
  }

  setChartYAxisLabel(yAxisLabel: string) {
    this.options.vAxis.title = yAxisLabel
  }

  private protGraph() {
    this.chartData = this.inMemoryChartData;
    this.columnNames = this.inMemoryColumnNames;
    this.inMemoryChartData = []
    this.inMemoryColumnNames = []
  }

  setReportListData(listResults: any) {
    this.tableDataList = util.com.ustadmobile.lib.util.UMUtil.kotlinListToJsArray(listResults);
  }

  handleAddToDashboard() {
    //show dialog
    this.presenter.handleAddDashboardClicked("S")
  }

  showBaseProgressBar(showProgress){
    
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
