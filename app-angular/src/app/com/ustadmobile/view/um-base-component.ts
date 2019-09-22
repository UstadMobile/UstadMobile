import { Subscription } from 'rxjs';
import { UmAngularUtil } from './../util/UmAngularUtil';
import { UmDbMockService } from './../core/db/um-db-mock.service';
import { UmContextWrapper } from './../util/UmContextWrapper';
import { UmBaseService } from './../service/um-base.service';
import { OnInit, OnDestroy } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { environment } from 'src/environments/environment';
import core from 'UstadMobile-core';

export abstract class UmBaseComponent implements OnInit, OnDestroy{

  public env = environment;
  protected systemImpl: any;
  protected readonly context: UmContextWrapper;
  protected readonly MessageID;
  public app_name: String = "...";
  protected viewContext: UmContextWrapper;
  protected subscription : Subscription;


  protected constructor(protected umService: UmBaseService, protected router: Router, protected route: ActivatedRoute,
     protected umDatabase: UmDbMockService){
    this.systemImpl = core.com.ustadmobile.core.impl.UstadMobileSystemImpl.Companion.instance;
    this.MessageID = core.com.ustadmobile.core.generated.locale.MessageID;
    this.context = new UmContextWrapper(router);
    this.context.setActiveRoute(this.route);
    this.viewContext = this.context; 
    this.umService.setContext(this.context);
  }

  ngOnInit(): void {
    //Listen for resources being ready
    this.subscription = this.umService.getUmObserver().subscribe(content =>{
      if(content[UmAngularUtil.DISPATCH_RESOURCE]){
        this.app_name = this.getString(this.MessageID.app_name);
      }
    });
  }

  runOnUiThread(runnable){
    runnable.run();
  }

  showError(errorMessage){
    this.umService.getToastService().show(errorMessage ? errorMessage : 
      this.getString(this.MessageID.error), 4000, 'red', () => {});
  }

  getString(messageId: number){
    return this.systemImpl.getString(messageId, this.context)
  }

  ngOnDestroy(){
    this.subscription.unsubscribe();
  }
}
