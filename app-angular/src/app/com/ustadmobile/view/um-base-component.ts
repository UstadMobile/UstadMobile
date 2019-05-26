import { UmContextWrapper } from './../util/UmContextWrapper';
import { UmBaseService } from './../service/um-base.service';
import { OnInit } from '@angular/core';
import { com } from 'core';
import { Router, ActivatedRoute } from '@angular/router';
export abstract class UmBaseComponent implements OnInit{
  public systemImpl: any;
  public readonly umContext: UmContextWrapper;
  public C
  constructor(private localeService: UmBaseService, private router: Router, private route: ActivatedRoute, ){
    this.systemImpl = com.ustadmobile.core.impl.UstadMobileSystemImpl.Companion.instance;
    this.localeService.setImpl(this.systemImpl);
    this.umContext = new UmContextWrapper(router);
    this.umContext.setActiveRoute(this.route);
    localeService.setContext(this.umContext)
  }
  ngOnInit(): void {
    this.localeService.setCurrentLocale("en");
  }
}
