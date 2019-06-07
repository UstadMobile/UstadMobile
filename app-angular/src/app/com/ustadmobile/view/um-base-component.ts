import { UmAngularUtil } from './../util/UmAngularUtil';
import { UmDbMockService } from './../core/db/um-db-mock.service';
import { UmContextWrapper } from './../util/UmContextWrapper';
import { UmBaseService } from './../service/um-base.service';
import { OnInit, OnDestroy } from '@angular/core';
import { com } from 'core';
import { Router, ActivatedRoute } from '@angular/router';
import { environment } from 'src/environments/environment';
import { Observable, Subject } from 'rxjs';
export abstract class UmBaseComponent implements OnInit, OnDestroy{

  public env = environment;
  public systemImpl: any;
  public readonly context: UmContextWrapper;
  public readonly MessageID;
  public app_name: String = "...";
  public viewContext: UmContextWrapper;


  protected constructor(public umService: UmBaseService, public router: Router, public route: ActivatedRoute, public mockedUmDb: UmDbMockService){
    this.systemImpl = com.ustadmobile.core.impl.UstadMobileSystemImpl.Companion.instance;
    this.MessageID = com.ustadmobile.core.generated.locale.MessageID;
    this.umService.setImpl(this.systemImpl);
    this.context = new UmContextWrapper(router);
    this.context.setActiveRoute(this.route);
    this.viewContext = this.context; 
    umService.setContext(this.context);
  }

  ngOnInit(): void {
    //load locale strings
    const systemDefaultLocale = this.systemImpl.getSystemLocale(this.context).split("-")[0];
    this.umService.loadLocaleStrings(systemDefaultLocale).subscribe((loaded) => {
      if(loaded){
        this.app_name = this.getString(this.MessageID.app_name);
        this.umService.dispatchUpdate(UmAngularUtil.getContentToDispatch(
          UmAngularUtil.DISPATCH_RESOURCE, loaded))
      }
    });

    this.umService.loadSupportedLanguages().subscribe((loaded) => {
      this.umService.dispatchUpdate(UmAngularUtil.getContentToDispatch(
        UmAngularUtil.DISPATCH_LANGUAGES, loaded))
    })
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

  ngOnDestroy(){}
}
