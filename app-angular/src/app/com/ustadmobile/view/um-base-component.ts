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
  floating_btn_class = ""

  protected constructor(public umService: UmBaseService, protected router: Router, protected route: ActivatedRoute){
    this.umService.setEnvironment(document.location.search.indexOf("test") != -1)
    this.systemImpl = core.com.ustadmobile.core.impl.UstadMobileSystemImpl.Companion.instance;
    this.MessageID = core.com.ustadmobile.core.generated.locale.MessageID;
    this.viewContext = this.context = new UmContextWrapper(router) 
    this.context.setActiveRoute(this.route);
    this.umService.init(this)   
    this.floating_btn_class = this.umService.isLTRDirectionality() ? "fixed-action-btn-right" : "fixed-action-btn-left";
  
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

  ngOnDestroy(){}
}
