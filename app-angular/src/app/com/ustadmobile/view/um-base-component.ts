import { UmAngularUtil, appRountes } from './../util/UmAngularUtil';
import { UmContextWrapper } from './../util/UmContextWrapper';
import { UmBaseService } from './../service/um-base.service';
import { OnInit, OnDestroy } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { environment } from 'src/environments/environment';
import core from 'UstadMobile-core';

export abstract class UmBaseComponent implements OnInit, OnDestroy{

  public env = environment;
  protected systemImpl: any;
  protected readonly context: any;
  public readonly MessageID = null;
  protected viewContext: any;
  public routes = appRountes;
  public toolBarTitle: string = '...';
  floating_btn_class_right = ""
  floating_btn_class_left = ""
  showIframe: boolean = true 
  changeColor: boolean = false 
  userProfile: string = "assets/images/guest_user_icon.png"
  

  protected constructor(public umService: UmBaseService, protected router: Router, protected route: ActivatedRoute){
    this.systemImpl = core.com.ustadmobile.core.impl.UstadMobileSystemImpl.Companion.instance;
    this.MessageID = core.com.ustadmobile.core.generated.locale.MessageID;
    this.viewContext = this.context = new UmContextWrapper(router) 
    this.context.setActiveRoute(this.route);
    this.umService.init(this)   
    this.showIframe = this.route.snapshot.queryParams.noiframe == "false"
    this.floating_btn_class_right = this.umService.isLTRDirectionality() ? "fixed-action-btn-right" : "fixed-action-btn-left";
    this.floating_btn_class_left = this.umService.isLTRDirectionality() ? "fixed-action-btn-left":"fixed-action-btn-right"
  }

  setToolbarTitle(title){
    UmAngularUtil.fireTitleUpdate(title)
  }

  ngOnInit(): void {}

  onCreate(){
    this.umService.appName = this.getString(this.MessageID.app_name)
  }

  runOnUiThread(runnable){
    runnable.run();
  }

  showError(errorMessage){
    if(errorMessage){
      this.umService.getToastService().show(errorMessage, 4000, 'red', () => {});
    }
  }

  getString(messageId: number){
    return this.systemImpl.getString(messageId, this.context)
  }

  openOnNewtab(url){
    if(this.showIframe == false){
      window.open(url,'_blank')
    }
  }

  restartUI(){
    window.open(UmAngularUtil.getRoutePathParam().origin + "/" + this.systemImpl.getLocale(this.context) + "/", "_self")
  }

  truncate(value: string, limit: number = 40, trail: String = '…'): string {
    let result = value || '';
    if (value) {
      const words = value.split(/\s+/);
      if (words.length > Math.abs(limit)) {
        if (limit < 0) {
          limit *= -1;
          result = trail + words.slice(words.length - limit, words.length).join(' ');
        } else {
          result = words.slice(0, limit).join(' ') + trail;
        }
      }
    }  
    return result;
  }

  ngOnDestroy(){}
}
