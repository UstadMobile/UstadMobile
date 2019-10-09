import { UmAngularUtil } from './../../util/UmAngularUtil';
import { Component, Renderer2, ElementRef, OnDestroy } from '@angular/core';
import { UmBaseComponent } from '../um-base-component';
import { UmBaseService } from '../../service/um-base.service';
import { Router, ActivatedRoute, NavigationEnd } from '@angular/router';

@Component({
  selector: 'app-report-dashboard',
  templateUrl: './report-dashboard.component.html',
  styleUrls: ['./report-dashboard.component.css']
})
export class ReportDashboardComponent extends UmBaseComponent implements OnDestroy{

  tagList = ["All Tags", "Session", "Perfomance", "Session", "Performance", "Session", "Perfomance", "20+ More"]
  title = '';
  type = 'ColumnChart';
  data = [
    ["2012", 900, 390],
    ["2013", 1000, 400],
    ["2014", 1170, 440],
    ["2015", 1250, 480],
    ["2016", 1530, 540]
  ];
  columnNames = ['Year', 'Asia', 'Europe'];
  options = {};
  width = 370;
  height = 400;
  hideFirst: boolean = false

  modalOptions: Materialize.ModalOptions = {
    dismissible: false,
    opacity: .5,
    inDuration: 300,
    outDuration: 200,
    startingTop: '100%',
    endingTop: '10%'
  };

  graphList = ["", "", "", ""]

  private navigationSubscription;

  constructor(umService: UmBaseService, router: Router, route: ActivatedRoute,
    private renderer: Renderer2, private elem: ElementRef) {
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
    UmAngularUtil.fireTitleUpdate("Report Dashboard") 
  }


  onDeleteTag(event) {
    console.log(event)
  }

  onSelectTag(event) {
    let elements = this.elem.nativeElement.querySelectorAll('.chip');
    elements.forEach(element => {
      this.renderer.removeClass(element, "selected-chip");
    })
    this.renderer.addClass(event.target, "selected-chip");
  }

  onAddTag() {}

  handleNewGraphCreated() {
    this.systemImpl.go(this.routes.reportOptions, UmAngularUtil.getArgumentsFromQueryParams(
      {params: "?entryid="+this.umService.ROOT_UID,route: this.routes.reportOptions}), this.context);
  }

  onViewMore(reportId) {
    this.systemImpl.go("/ReportDetails", UmAngularUtil.getArgumentsFromQueryParams({params:"?reportId=" + reportId}), this.context, 0)
  }


  ngOnDestroy() {
    super.ngOnDestroy()
    if (this.navigationSubscription) {
      this.navigationSubscription.unsubscribe();
    }
  }

}
