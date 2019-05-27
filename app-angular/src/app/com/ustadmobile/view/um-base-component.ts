import { UmContextWrapper } from './../util/UmContextWrapper';
import { UmBaseService } from './../service/um-base.service';
import { OnInit } from '@angular/core';
import { com } from 'core';
import { Router, ActivatedRoute } from '@angular/router';
import { environment } from 'src/environments/environment';
export abstract class UmBaseComponent implements OnInit{

  public env = environment;
  public systemImpl: any;
  public readonly context: UmContextWrapper;
  public readonly MessageID;
  public appName: string;

  constructor(public localeService: UmBaseService, public router: Router, public route: ActivatedRoute, ){
    this.systemImpl = com.ustadmobile.core.impl.UstadMobileSystemImpl.Companion.instance;
    this.MessageID = com.ustadmobile.core.generated.locale.MessageID;
    this.localeService.setImpl(this.systemImpl);
    this.context = new UmContextWrapper(router);
    this.context.setActiveRoute(this.route);
    localeService.setContext(this.context)
    this.appName = this.MessageID.NAME; 
    console.log(com.ustadmobile.core.generated.locale.MessageID.cancel)
  }
  ngOnInit(): void {
    this.localeService.setCurrentLocale("fa");
  }

  public getString(messageId: number){
    return this.systemImpl.getString(messageId, this.context)
  }
}
