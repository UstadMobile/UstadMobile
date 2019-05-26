import { UmContextWrapper } from './../util/UmContextWrapper';
import { UmBaseService } from './../service/um-base.service';
import { OnInit } from '@angular/core';
import { com } from 'core';
import { Router, ActivatedRoute } from '@angular/router';
export abstract class UmBaseComponent implements OnInit{
  public systemImpl: any;
  public readonly context: UmContextWrapper;
  constructor(public localeService: UmBaseService, public router: Router, public route: ActivatedRoute, ){
    this.systemImpl = com.ustadmobile.core.impl.UstadMobileSystemImpl.Companion.instance;
    this.localeService.setImpl(this.systemImpl);
    this.context = new UmContextWrapper(router);
    this.context.setActiveRoute(this.route);
    localeService.setContext(this.context)
  }
  ngOnInit(): void {
    this.localeService.setCurrentLocale("fa");
  }

  public getString(messageId: number){
    return this.systemImpl.getString(messageId, this.context)
  }
}
