import { Component, Renderer2, ElementRef } from '@angular/core';
import { Router, ActivatedRoute, NavigationEnd } from '@angular/router';
import { Location } from '@angular/common';
import { UmBaseComponent } from '../um-base-component';
import { UmBaseService } from '../../service/um-base.service';
import { Subscription } from 'rxjs/internal/Subscription';
import { UmAngularUtil, appRountes } from '../../util/UmAngularUtil';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';
import util from 'UstadMobile-lib-util';
import core from 'UstadMobile-core'

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent extends UmBaseComponent implements core.com.ustadmobile.core.view.HomeView{

  menu_libaries: string;
  menu_reports: string;
  icon_position_class: string;
  toolbar_icon_class: string;
  toolbar_arrow: string;
  toolbar_title_class: string;
  drawer_menu_class: string;
  supportedLanguages = [];
  routes = appRountes
  umFormLanguage: FormGroup;
  navigationSubscription: Subscription;
  showReports = false
  userName: string = "Guest User"
  userEmail: string = "guestmail"
  userProfile: string = ""
  static toolBarTitle: string = ".."
  activeState = [true, false]
  presenter: core.com.ustadmobile.core.controller.HomePresenter;

  constructor(private location: Location, umService: UmBaseService,
    router: Router, route: ActivatedRoute,formBuilder: FormBuilder, private renderer: Renderer2,
     private elem: ElementRef) {
    super(umService, router, route);
    this.icon_position_class = this.umService.isLTRDirectionality() ? "left" : "right icon-left-spacing";
    this.toolbar_icon_class = this.umService.isLTRDirectionality() ? "left icon-right-spacing" : "right icon-left-spacing";
    this.toolbar_arrow = this.umService.isLTRDirectionality() ? "arrow_back" : "arrow_forward";
    this.toolbar_title_class = this.umService.isLTRDirectionality() ? "brand-logo-ltr" : "brand-logo-rtl";
    this.drawer_menu_class = this.umService.isLTRDirectionality() ? "right drawer-menu-ltr" : "left drawer-menu-rtl";
    this.umFormLanguage = formBuilder.group({
      'language': ['', Validators.required]
    });

    //Listen for the navigation changes - changes on url
    this.navigationSubscription = this.router.events.filter(event => event instanceof NavigationEnd)
      .subscribe(_ => {
        UmAngularUtil.registerResourceReadyListener(this)
        UmAngularUtil.registerTitleChangeListener(this)
        this.activeState = UmAngularUtil.getActiveMenu(this.routes) 
      });
  }


  ngOnInit() {
    super.ngOnInit()
    this.umFormLanguage.valueChanges.subscribe((form: any) => {
      if (form.language !== "") {
        window.open(window.location.origin + "/" + form.language + "/", "_self")
      }
    });
  }


  onCreate() {
    super.onCreate()
    this.supportedLanguages = util.com.ustadmobile.lib.util.UMUtil.kotlinMapToJsArray(
      this.systemImpl.getAllUiLanguage(this.context))
    this.presenter = new core.com.ustadmobile.core.controller.HomePresenter(
      this.context, UmAngularUtil.queryParamsToMap(), this, this.umService.getDbInstance().personDao)
    this.presenter.onCreate(null)
  }

  showReportMenu(show){
    this.showReports = show
  }

  showDownloadAllButton(show){}

  setLoggedPerson(person){
    this.userEmail = person.emailAddr
    this.userName = person.firstNames + " " + person.lastName
  }

  loadProfileIcon(profile){
    if(profile != ""){
      this.userProfile = profile
    }else{
      this.userProfile = window.location.origin +"/assets/images/guest_user_icon.png"
    }
  }

  goBack() {
    if (!window.location.search.includes(this.umService.ROOT_UID + "")) {
      this.location.back();
    }
  }

  navigateTo(route) {
    const activeAcount = core.com.ustadmobile.core.impl.UmAccountManager.getActiveAccountWithContext(this.context)
    const routeTo = route == "ReportDashboard" && !activeAcount  ? this.routes.login: route 
    const args = UmAngularUtil.getRouteArgs(routeTo, this.umService.ROOT_UID) 
    this.systemImpl.go(routeTo, args, this.context);
  }

  setToolbarTitle(title){
    this.toolBarTitle = title
  }


  ngOnDestroy(): void {
    super.ngOnDestroy();
    if(this.presenter){
      this.presenter.onDestroy(); 
    }
    if(this.navigationSubscription){
      this.navigationSubscription.unsubscribe(); 
    }
  }

}
