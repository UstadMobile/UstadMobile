import { Component } from '@angular/core';
import { Router, ActivatedRoute, NavigationEnd } from '@angular/router';
import { Location } from '@angular/common';
import { UmBaseComponent } from '../um-base-component';
import { UmBaseService } from '../../service/um-base.service';
import { UmDbMockService } from '../../core/db/um-db-mock.service';
import { Subscription } from 'rxjs/internal/Subscription';
import { UmAngularUtil, appRountes } from '../../util/UmAngularUtil';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';
import util from 'UstadMobile-lib-util';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent extends UmBaseComponent {
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
  showReports = true

  constructor(private location: Location, umService: UmBaseService,
    router: Router, route: ActivatedRoute, umDb: UmDbMockService, formBuilder: FormBuilder) {
    super(umService, router, route, umDb);
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
        this.subscription =  UmAngularUtil.registerUmObserver(this) 
      });
  }

  ngOnInit() {
    super.ngOnInit()
    this.umFormLanguage.valueChanges.subscribe((form: any) => {
      if (form.language !== "") {
        window.open(window.location.origin + "/" + form.language + "/", "_self")
      }
    });
    this.subscription =  UmAngularUtil.registerUmObserver(this) 
  }

  onCreate() {
    this.supportedLanguages = util.com.ustadmobile.lib.util.UMUtil.kotlinMapToJsArray(
      this.systemImpl.getAllUiLanguage(this.context))
  }

  goBack() {
    if (!window.location.search.includes(this.umDatabase.ROOT_UID + "")) {
      this.location.back();
    }
  }

  navigateTo(route) {
    this.systemImpl.go(route, UmAngularUtil.getRouteArgs(route, this.umDatabase.ROOT_UID), this.context);
  }

  ngOnDestroy(): void {
    super.ngOnDestroy();
    this.navigationSubscription.unsubscribe();
  }

}
