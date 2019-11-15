import { UmAngularUtil } from './../../util/UmAngularUtil';
import { Component, ElementRef, Output, EventEmitter, OnDestroy } from '@angular/core';
import { UmBaseComponent } from '../um-base-component';
import { UmBaseService } from '../../service/um-base.service';
import { Router, ActivatedRoute, NavigationEnd } from '@angular/router';
import core from 'UstadMobile-core';
import util from 'UstadMobile-lib-util';
import { Subscription } from 'rxjs';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';
import { MzModalService } from 'ngx-materialize';
import { XapiTreeviewDialogComponent } from "../xapi-treeview-dialog/XapiTreeviewDialogComponent";
import { DatePipe } from '@angular/common';

@Component({
  selector: 'app-xapi-report-options', 
  templateUrl: './xapi-report-options.component.html',
  styleUrls: ['./xapi-report-options.component.css']
})
export class XapiReportOptionsComponent extends UmBaseComponent implements OnDestroy,
core.com.ustadmobile.core.view.XapiReportOptionsView {
  
  private presenter: core.com.ustadmobile.core.controller.XapiReportOptionsPresenter;
  private navigationSubscription;
  umFormReportOptions: FormGroup;
  translatedYAxisList = []
  translatedGraphList = []
  translatedXAxisList = []

  visualization_label: string = ''
  y_axis_label: string = '..'
  x_axis_label: string = '..'
  sub_group_label: string = '..'
  graph_who_label: string = '..'
  graph_did_label: string = '..'
  graph_what_label: string = '..'
  graph_when_start_label: string = '..'
  graph_when_end_label: string = '..'

  selectedWhoList = []
  selectedDidList = []
  selectedWhatList = []
  inMemoryWhoList = []
  inMemoryDidList = []
  inMemorySelectedNames = ""
  depth_value = "z-depth-1"

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
    formBuilder: FormBuilder, private datePipe: DatePipe,
    private elementRef: ElementRef, private modalService: MzModalService) {
    super(umService, router, route);
    const currentDate = this.datePipe.transform(new Date(), 'yyyy-MM-dd')
    const tomorrow = this.datePipe.transform(new Date().setDate(new Date().getDate() + 1), 'yyyy-MM-dd')
    //Build form for capturing report options
    this.umFormReportOptions = formBuilder.group({
      'visualization': ['0', Validators.required],
      'y_axis': ['0', Validators.required],
      'x_axis': ['0', Validators.required],
      'sub_group': ['0', Validators.required],
      'graph_what': [this.inMemorySelectedNames, Validators.required],
      'graph_when_start': [currentDate, Validators.required],
      'graph_when_end': [tomorrow, Validators.required]
    });

    if(this.umService.isMobile){
      this.depth_value = "z-depth-0"
    }

    this.navigationSubscription = this.router.events.filter(event => event instanceof NavigationEnd)
      .subscribe(_ => {
        UmAngularUtil.registerResourceReadyListener(this)
      });
  }


  ngOnInit() {
    super.ngOnInit()
  }

  onChanges() {
    this.umFormReportOptions.valueChanges.subscribe(formValue => {
      this.presenter.handleSelectedYAxis(formValue.y_axis)
      this.presenter.handleSelectedChartType(formValue.visualization)
      this.presenter.handleSelectedXAxis(formValue.x_axis)
      this.presenter.handleSelectedSubGroup(formValue.sub_group)
      const dateFrom = new Date(formValue.graph_when_start)
      const dateTo = new Date(formValue.graph_when_end)
      this.presenter.handleDialogFromCalendarSelected(dateFrom.getFullYear(), dateFrom.getMonth(), dateFrom.getDate())
      this.presenter.handleDialogToCalendarSelected(dateTo.getFullYear(), dateTo.getMonth(), dateTo.getDate())

    });
    const scope = this;
    this.elementRef.nativeElement.addEventListener('input', event => {
      const valueChange = $(event.target).get(0).value
      switch ($(event.target).parent().attr('id')) {
        case 'graph_who':
          scope.presenter.handleWhoDataTyped(valueChange, UmAngularUtil.jsArrayToKotlinList(this.getSelectedPersonIds()))
          break;
        case 'graph_did':
          scope.presenter.handleDidDataTyped(valueChange, UmAngularUtil.jsArrayToKotlinList(this.getSelectedDidUIds()))
          break;
      }
    }, false);
    this.datepickerOptions = {
      clear: this.getString(this.MessageID.xapi_clear),
      close: this.getString(this.MessageID.ok),
      today: this.getString(this.MessageID.xapi_today),
      closeOnClear: true,
      closeOnSelect: false,
      format: 'dddd, dd mmm, yyyy',
      formatSubmit: 'yyyy-mm-dd',
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
    this.onChanges()
    this.presenter = new core.com.ustadmobile.core.controller.XapiReportOptionsPresenter(
      this.context, UmAngularUtil.getArgumentsFromQueryParams(), this, this.umService.getDbInstance().personDao,
      this.umService.getDbInstance().xObjectDao, this.umService.getDbInstance().xLangMapEntryDao);
    this.presenter.onCreate(null);
    UmAngularUtil.fireTitleUpdate(this.getString(this.MessageID.xapi_options_report_title))
  }

  onDataChange(data) {
    if (data.nodes != undefined) {
      this.selectedWhatList = []
      if (data.nodes === "clear") {
        this.inMemorySelectedNames = ""
      } else {
        const selectedNames: string[] = []
        const selectedUid: number[] = []
        JSON.parse(data.nodes).forEach(selection => {
          const data = JSON.parse(atob(selection))
          this.selectedWhatList.push(data)
          selectedNames.push(data.name)
          selectedUid.push(+data.id)
        });
        this.presenter.handleEntriesListSelected(
          util.com.ustadmobile.lib.util.UMUtil.jsArrayToKotlinList(selectedUid))
        this.inMemorySelectedNames = selectedNames.join(",")
      }
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
    UmAngularUtil.registerDataChangeListener(this)
    this.inMemorySelectedNames = ''
    this.modalService.open(XapiTreeviewDialogComponent);
  }

  updateWhoDataAdapter(whoList) {
    const whoArray = UmAngularUtil.kotlinListToJsArray(whoList)
    this.inMemoryWhoList = whoArray
    whoArray.forEach(person => {
      this.whoAutoComplete.data[person.name] = null
    });
  }

  updateDidDataAdapter(didList) {
    const didArray = UmAngularUtil.kotlinListToJsArray(didList)
    this.inMemoryDidList = didArray
    didArray.forEach(verb => {
      this.didAutoComplete.data[verb.valueLangMap] = null
    });
  }

  onAddPerson(event) {
    const person: any = UmAngularUtil.getElementFromObject(this.inMemoryWhoList, "name", event.tag)
    if (person) {
      this.selectedWhoList.push(person);
    }
  }

  onDeletePerson(event) {
    const person = UmAngularUtil.getElementFromObject(this.inMemoryWhoList, "name", event.tag)
    if (person) {
      this.deleteChip(this.selectedWhoList, person);

    }
  }

  private getSelectedPersonIds() {
    const personUids = []
    this.selectedWhoList.forEach(person => {
      personUids.push(parseInt(person.personUid))
    })
    return personUids
  }

  private getSelectedDidUIds() {
    const verbUids = []
    this.selectedDidList.forEach(verb => {
      verbUids.push(parseInt(verb.verbLangMapUid))
    })
    return verbUids
  }

  onAddVerb(event) {
    const verb = UmAngularUtil.getElementFromObject(this.inMemoryDidList, "valueLangMap", event.tag)
    if (verb) {
      this.selectedDidList.push(verb);
    }
  }

  onDeleteVerb(event) {
    const verb = UmAngularUtil.getElementFromObject(this.inMemoryDidList, "valueLangMap", event.tag)
    if (verb) {
      this.deleteChip(this.selectedDidList, verb);
    }
  }
  private deleteChip(list: any[], element: any) {
    var index = this.selectedWhoList.indexOf(element);
    if (index > -1) {
      list.splice(index, 1);
    }
  }

  updateFromDialogText() {}

  updateToDialogText() {}

  updateWhenRangeText() {}

  updateChartTypeSelected() {}

  updateYAxisTypeSelected() {}

  updateXAxisTypeSelected() {}

  updateSubgroupTypeSelected() {}

  updateWhoListSelected() {}

  updateDidListSelected() {}

  handleViewReportPreview() {
    const didList = []
    const whoList = []

    this.selectedDidList.forEach(did => {
      didList.push(did.verbLangMapUid)
    });

    this.selectedWhoList.forEach(who => {
      whoList.push(who.personUid)
    });

    this.presenter.handleViewReportPreview(util.com.ustadmobile.lib.util.UMUtil.jsArrayToKotlinList(didList),
      util.com.ustadmobile.lib.util.UMUtil.jsArrayToKotlinList(whoList))
  }

  showBaseProgressBar() {}

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
