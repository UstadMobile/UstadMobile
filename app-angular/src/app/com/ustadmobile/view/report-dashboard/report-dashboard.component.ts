import { UmAngularUtil, appRountes } from './../../util/UmAngularUtil';
import { Component, Renderer2, ElementRef } from '@angular/core';
import { UmBaseComponent } from '../um-base-component';
import { UmBaseService } from '../../service/um-base.service';
import { Router, ActivatedRoute, NavigationEnd } from '@angular/router';
import { UmDbMockService } from '../../core/db/um-db-mock.service';

@Component({
  selector: 'app-report-dashboard',
  templateUrl: './report-dashboard.component.html',
  styleUrls: ['./report-dashboard.component.css']
})
export class ReportDashboardComponent extends UmBaseComponent {

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

  onAddTag(event) {}

  handleNewGraphCreated() {
    this.systemImpl.go(this.routes.reportOptions, UmAngularUtil.getRouteArgs(
      this.routes.reportOptions, this.umService.ROOT_UID), this.context);
  }

  onViewMore(reportId) {
    const args = UmAngularUtil.queryParamsToMap("?reportId=" + reportId)
    this.systemImpl.go("/ReportDetails", args, this.context, 0)
  }


  ngOnDestroy() {
    super.ngOnDestroy()
    if (this.navigationSubscription) {
      this.navigationSubscription.unsubscribe();
    }
  }

}
