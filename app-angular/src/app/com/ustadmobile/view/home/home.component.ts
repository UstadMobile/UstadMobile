import { Component, NgZone, OnDestroy, ElementRef, Renderer2 } from '@angular/core';
import { Router, ActivatedRoute, NavigationEnd, NavigationStart } from '@angular/router';
import { Location } from '@angular/common';
import { UmBaseComponent } from '../um-base-component';
import { UmBaseService } from '../../service/um-base.service';
import { Subscription } from 'rxjs/internal/Subscription';
import { UmAngularUtil } from '../../util/UmAngularUtil';
import util from 'UstadMobile-lib-util';
import core from 'UstadMobile-core'
import { FormGroup, FormBuilder, Validators } from '@angular/forms';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent extends UmBaseComponent implements OnDestroy,
 core.com.ustadmobile.core.view.HomeView {

  menu_libaries: string;
  menu_reports: string;
  class_icon_position: string;
  class_icon_toolbar: string;
  class_toolbar_arrow: string;
  class_toolbar_title: string;
  class_drawer_menu: string;
  class_open_profile: string;
  supportedLanguages = [];

  public modalOptions: Materialize.ModalOptions = {
    dismissible: true,
    opacity: .5, 
    inDuration: 300,
    outDuration: 200, 
    startingTop: '100%', 
    endingTop: '30%',
    ready: (modal, trigger) => {
      this.onDialogReady()
    } 
  };

  umFormLanguage: FormGroup;
  
  private navigationSubscription: Subscription;
  person = {username: "gestUser", emailAddr: "", firstNames:"Guest", lastName: "User"}
  static toolBarTitle: string = ".."
  activeState = [true, false]
  private presenter: core.com.ustadmobile.core.controller.HomePresenter;
  private navItemMaps = {}
  navMenuOptions = []
  doOpenEntries = false; 

  constructor(private location: Location, umService: UmBaseService, private elem: ElementRef,private renderer: Renderer2,
    router: Router, route: ActivatedRoute, private zone:NgZone, formBuilder: FormBuilder) {
    super(umService, router, route);
    this.class_icon_position = this.umService.isLTRDirectionality() ? "left" : "right icon-left-spacing";
    this.class_icon_toolbar = this.umService.isLTRDirectionality() ? "left icon-right-spacing" : "right icon-left-spacing";
    this.class_toolbar_arrow = this.umService.isLTRDirectionality() ? "arrow_back" : "arrow_forward";
    this.class_toolbar_title = this.umService.isLTRDirectionality() ? "brand-logo-ltr" : "brand-logo-rtl";
    this.class_drawer_menu = this.umService.isLTRDirectionality() ? "right drawer-menu-ltr" : "left drawer-menu-rtl";
    this.class_open_profile = this.umService.isLTRDirectionality() ? "right":"left"
    this.navItemMaps[core.com.ustadmobile.core.generated.locale.MessageID.reports] = {icon: "pie_chart",i18n: "@@libraries"}; 
    this.navItemMaps[core.com.ustadmobile.core.generated.locale.MessageID.contents] = {icon:"local_library", i18n:"@@reports"}; 
    this.router.events.subscribe((event: NavigationStart)  => {
      if(event.url && event.url == "/"){
        const initialRoute = UmAngularUtil.getInitialRoute(this.umService.ROOT_UID);
        this.systemImpl.go(initialRoute.view, initialRoute.args, this.context, 0)
      }
    });

    this.route.queryParams.subscribe(params => {
      const isFromNext = this.router.url.split("/")[2].split("?")[0] == this.routes.entryList && !params[UmAngularUtil.ARG_CONTENT_ENTRY_UID],
      isTopEntry = !params[UmAngularUtil.ARG_FILTER_BUTTONS] && this.umService.ROOT_UID == params[UmAngularUtil.ARG_CONTENT_ENTRY_UID]
      this.doOpenEntries =  isTopEntry || isFromNext
    });
    
    //Listen for the navigation changes - changes on url
    this.navigationSubscription = this.router.events.filter(event => event instanceof NavigationEnd)
      .subscribe(_ => {
        UmAngularUtil.registerResourceReadyListener(this)
        UmAngularUtil.registerTitleChangeListener(this)
        this.activeState = UmAngularUtil.getActiveMenu() 
      });
      const currentLocale = this.systemImpl.getAllUiLanguage(this.context)[core.com.ustadmobile.core.impl.UstadMobileSystemCommon.PREFKEY_LOCALE]
      this.umFormLanguage = formBuilder.group({
        'language': [currentLocale, Validators.required]
      }); 
  }

  onDialogReady(){
    let footerElements = this.elem.nativeElement.querySelectorAll('.modal-footer');
    let dialogContet = this.elem.nativeElement.querySelectorAll('.modal-content');
    footerElements.forEach(element => {
      this.renderer.removeChild(this.elem.nativeElement, element)
    })
    dialogContet.forEach(element => {
      this.renderer.removeClass(element, "modal-content");
    })
  }


  ngOnInit() {
    super.ngOnInit()
    this.umFormLanguage.valueChanges.subscribe((form: any) => {
      if (form.language !== "") {
        this.handleLanguageSelected(form.language)
      }
    });
  }

  handleLanguageSelected(language, bottomSheetModal = null){
    this.zone.run(()=>{
      this.presenter.handleLanguageSelected(this.supportedLanguages.indexOf(language))
    })
  }


  onCreate() {
    super.onCreate()
    this.supportedLanguages = util.com.ustadmobile.lib.util.UMUtil.kotlinMapToJsArray(
      this.systemImpl.getAllUiLanguage(this.context))
    this.presenter = new core.com.ustadmobile.core.controller.HomePresenter(
      this.context, UmAngularUtil.getArgumentsFromQueryParams(), this, this.umService.getDbInstance().personDao, this.systemImpl)
    this.presenter.onCreate(null)
    this.presenter.handleShowLanguageOptions()
    //TODO: make sure to remove this function manual calling when things getresolved
    this.presenter.onChanged(core.com.ustadmobile.core.impl.UmAccountManager.getActiveAccountWithContext(this.context));
  }

  handleClickPersonIcon(){
    this.presenter.handleClickPersonIcon()
  }

  setNavigationOptions(options){
    const menuOptions = UmAngularUtil.kotlinListToJsArray(options);
    this.navMenuOptions = []
      menuOptions.forEach(option => {
        const menuNav = this.navItemMaps[option.first]
        menuNav['label'] = this.getString(option.first)
        menuNav['params'] = option.second
        this.navMenuOptions.push(menuNav)
      }) 

      if(this.doOpenEntries){
        this.handleSideMenuSelected(0)
      }
  }

  showDownloadAllButton() {}

  setLoggedPerson(person) {
    this.person = person
  }

  loadProfileIcon(profile) {
    if (profile != "") {
      this.userProfile = profile
    }
  }

  goBack() {
    if (!UmAngularUtil.getRoutePathParam().search.includes(this.umService.ROOT_UID + "")) {
      this.location.back();
    }
  }

  handleSideMenuSelected(index) {
    //TODO: handle this well when dashboard is integrated
    const activeAcount = core.com.ustadmobile.core.impl.UmAccountManager.getActiveAccountWithContext(this.context),
    route = this.navMenuOptions[index].params.split("?")[0], 
    isReport = route.includes(this.routes.reportOptions), 
    initQueryParams = this.navMenuOptions[index].params.split("?")[1] 
    ? "?"+this.navMenuOptions[index].params.split("?")[1]:"",
    routeView =  isReport && !activeAcount ? this.routes.login : route == "DashboardView" ? this.routes.reportOptions:route,
    routeParams = {params:!isReport ? initQueryParams: (route != routeView ? "?next="+route: null), route: routeView};
    this.systemImpl.go(routeView, UmAngularUtil.getArgumentsFromQueryParams(routeParams), this.context);
  }

  setToolbarTitle(title) {
    this.toolBarTitle = this.umService.isMobile ? super.truncate(title, 3): title
  }

  setCurrentLanguage(language){}

  setLanguageOption(languageOptions){
    this.zone.run(()=>{
      this.supportedLanguages = UmAngularUtil.kotlinListToJsArray(languageOptions)
    })
  }

  showLanguageOptions(){}


  ngOnDestroy(): void {
    super.ngOnDestroy();
    if (this.presenter) {
      this.presenter.onDestroy();
    }
    if (this.navigationSubscription) {
      this.navigationSubscription.unsubscribe();
    }
  }

}
