import { UmContextWrapper } from './../util/UmContextWrapper';
import { UmBaseService } from './../service/um-base.service';
import { OnInit } from '@angular/core';
import { com } from 'core';
export abstract class UmBaseComponent implements OnInit{
  public systemImpl: any;
  public C
  constructor(private localeService: UmBaseService){
    this.systemImpl = com.ustadmobile.core.impl.UstadMobileSystemImpl.Companion.instance;
    this.localeService.setImpl(this.systemImpl);
  }
  ngOnInit(): void {
    this.localeService.setCurrentLocale("en");
  }
}
