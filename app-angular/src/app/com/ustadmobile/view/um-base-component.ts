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
  public entry_title: String = "...";
  public subject = new Subject<any> ();


  protected constructor(public localeService: UmBaseService, public router: Router, public route: ActivatedRoute, public mockedUmDb: UmDbMockService){
    this.systemImpl = com.ustadmobile.core.impl.UstadMobileSystemImpl.Companion.instance;
    this.MessageID = com.ustadmobile.core.generated.locale.MessageID;
    this.localeService.setImpl(this.systemImpl);
    this.context = new UmContextWrapper(router);
    this.context.setActiveRoute(this.route);
    localeService.setContext(this.context);
  }

  ngOnInit(): void {
    //load locale strings
    const systemDefaultLocale = this.systemImpl.getSystemLocale(this.context).split("-")[0];
    this.localeService.loadLocaleStrings(systemDefaultLocale).subscribe((loaded) => {
      if(loaded){
        this.app_name = this.getString(this.MessageID.app_name);
      }
    });
    this.subject.subscribe(data =>{
      data.next("...")
    })
  }

  ngOnDestroy(){
    
  }

  public getString(messageId: number){
    return this.systemImpl.getString(messageId, this.context)
  }
}
