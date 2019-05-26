import { UmBaseService } from './com/ustadmobile/service/um-base.service';
import { Component, OnInit } from '@angular/core';
import { environment } from 'src/environments/environment.prod';
import { Router, ActivatedRoute } from '@angular/router';
import { com } from 'core';
import { UmDbMockService } from './com/ustadmobile/core/db/um-db-mock.service';
import { UmContextWrapper } from './com/ustadmobile/util/UmContextWrapper';
import { UmBaseComponent } from './com/ustadmobile/view/um-base-component';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent extends UmBaseComponent{
  
  private readonly umContext: UmContextWrapper;

  constructor(localeService: UmBaseService, private router: Router, private route: ActivatedRoute, private umDb: UmDbMockService){
    super(localeService);
    this.umContext = new UmContextWrapper(router);
    this.umContext.setActiveRoute(this.route);
    localeService.setContext(this.umContext)
  }

  ngOnInit(): void {
    super.ngOnInit();
    const args = { queryParams: { parentUid: this.umDb.ROOT_UID} };
    com.ustadmobile.core.impl.UstadMobileSystemImpl.Companion.instance.go('contentEntryList',args, this.umContext,0);
  }

  ngOnDestroy(): void {}
}
