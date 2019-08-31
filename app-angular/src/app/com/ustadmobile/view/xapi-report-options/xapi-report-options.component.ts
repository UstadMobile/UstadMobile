import { UmAngularUtil } from './../../util/UmAngularUtil';
import { Component, Renderer2, ElementRef, Output, EventEmitter } from '@angular/core';
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
umFormReportOptions: FormGroup;
translatedYAxisList = []
translatedGraphList = []
translatedXAxisList = []

visualization_label: string = ''
y_axis_label: string = ''
x_axis_label: string = ''
sub_group_label: string = ''
graph_who_label: string = ''
graph_did_label: string = ''
graph_what_label: string = ''
graph_when_start_label: string = ''
graph_when_end_label: string = ''
@Output()
public domChange = new EventEmitter();

datepickerOptions: Pickadate.DateOptions = {};

whoAutoComplete: Materialize.AutoCompleteOptions = {
  data: {
    '': null
  },
};

didAutoComplete: Materialize.AutoCompleteOptions = {
  data: {
    '': null
  },
};

constructor(umService: UmBaseService, router: Router, route: ActivatedRoute,
  umDb: UmDbMockService, formBuilder: FormBuilder, private elementRef: ElementRef) {
  super(umService, router, route, umDb);

  //Build form for capturing report options
  this.umFormReportOptions = formBuilder.group({
    'visualization': ['', Validators.required],
    'y_axis': ['', Validators.required],
    'x_axis': ['', Validators.required],
    'sub_group': ['', Validators.required],
    'graph_who': ['', Validators.required],
    'graph_did': ['', Validators.required],
    'graph_what': ['', Validators.required],
    'graph_when_start': ['', Validators.required],
    'graph_when_end': ['', Validators.required]
  });
}


ngOnInit() {
  super.ngOnInit()
  this.subscription = UmAngularUtil.registerUmObserver(this)
}

onChanges() {
  this.umFormReportOptions.valueChanges.subscribe(fields => {});
  const scope = this;
   this.elementRef.nativeElement.addEventListener('input', event => {
    const valueChange = $(event.target).get(0).value
    switch($(event.target).parent().attr('id')){
      case 'graph_who':
          scope.presenter.handleWhoDataTyped(valueChange,[0,2])
        break;
      case 'graph_did':
          scope.presenter.handleDidDataTyped(valueChange,[]) 
          break;
    }
  }, false);
   this.datepickerOptions = {
    clear: this.getString(this.MessageID.xapi_clear),
    close: this.getString(this.MessageID.ok),
    today: this.getString(this.MessageID.xapi_today),
    closeOnClear: true, closeOnSelect: false,
    format: 'dddd, dd mmm, yyyy', formatSubmit: 'yyyy-mm-dd',
    selectMonths: true,
    selectYears: 10,
  };

  this.visualization_label = this.getString(this.MessageID.xapi_options_visual_type)
    this.y_axis_label = this.getString(this.MessageID.xapi_options_y_axes)
    this.x_axis_label = this.getString(this.MessageID.xapi_options_x_axes)
    this.sub_group_label = this.getString(this.MessageID.xapi_options_subgroup)
    this.graph_who_label = this.getString(this.MessageID.xapi_options_who)
    this.graph_did_label = this.getString(this.MessageID.xapi_options_did)
    this.graph_what_label = this.getString(this.MessageID.xapi_options_what)
    this.graph_when_start_label = this.getString(this.MessageID.from)
    this.graph_when_end_label = this.getString(this.MessageID.tocao)
}


onCreate() {
  super.onCreate()
  if (this.umDatabase.xObjectDao) {
    this.onChanges()
    this.presenter = new core.com.ustadmobile.core.controller.XapiReportOptionsPresenter(
      this.context, UmAngularUtil.getRouteArgs(this.routes.treeView,0), this, this.umDatabase.personDao,
      this.umDatabase.xObjectDao, this.umDatabase.xLangMapEntryDao);
    this.presenter.onCreate(null);
    this.setToolbarTitle(this.getString(this.MessageID.xapi_options_report_title))
  }
}

fillVisualChartType(graphList) {
  this.translatedGraphList = util.com.ustadmobile.lib.util.UMUtil.kotlinListToJsArray(graphList)
}

fillYAxisData(translatedYAxisList) {
  this.translatedYAxisList = util.com.ustadmobile.lib.util.UMUtil.kotlinListToJsArray(translatedYAxisList)
}

fillXAxisAndSubGroupData(translatedXAxisList) {
  this.translatedXAxisList = util.com.ustadmobile.lib.util.UMUtil.kotlinListToJsArray(translatedXAxisList)
}

handleWhatClicked() {
  this.presenter.handleWhatClicked()
}
updateWhoDataAdapter(whoList) {
  whoList.forEach(person => {
    this.whoAutoComplete.data[person.name] = null
  });
}

updateDidDataAdapter(didList) {
  didList.forEach(verb => {
    this.didAutoComplete.data[verb.valueLangMap] = null 
  });
  console.log(this.didAutoComplete.data)
}

updateFromDialogText(fromDate) {
  console.log("fromDate", fromDate)
}

updateToDialogText(toDate) {
  console.log("toDate", toDate)
}

updateWhenRangeText(rangeText) {
  console.log("rangeText", rangeText)
}

updateChartTypeSelected(indexChart) {
  console.log("indexChart", indexChart)
}

updateYAxisTypeSelected(indexYAxis) {
  console.log("indexYAxis", indexYAxis)
}

updateXAxisTypeSelected(indexXAxis) {
  console.log("indexXAxis", indexXAxis)
}

updateSubgroupTypeSelected(indexSubgroup) {
  console.log("indexesSub", indexSubgroup)
}

updateWhoListSelected(personList) {
  console.log("persons", personList)
}

updateDidListSelected(verbs) {
  console.log("verbs", verbs)
}

handleDoneSelected() {
  this.presenter.handleViewReportPreview([],[])
}

ngOnDestroy() {
  super.ngOnDestroy()
  this.presenter.onDestroy();
  if (this.navigationSubscription) {
    this.navigationSubscription.unsubscribe();
  }
}

}
