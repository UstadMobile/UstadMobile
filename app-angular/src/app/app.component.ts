import { UmAngularUtil } from './com/ustadmobile/util/UmAngularUtil';
import { UmBaseService } from './com/ustadmobile/service/um-base.service';
import { Component, OnDestroy } from '@angular/core';
import { Router, ActivatedRoute, NavigationEnd } from '@angular/router';
import { UmDbMockService } from './com/ustadmobile/core/db/um-db-mock.service';
import { UmBaseComponent } from './com/ustadmobile/view/um-base-component';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent extends UmBaseComponent{
  
  constructor(localeService: UmBaseService, router: Router,
              route: ActivatedRoute, private umDb: UmDbMockService){
    super(localeService, router, route, umDb);
  }

  ngOnInit(): void {
    super.ngOnInit();
    //check and redirect to a specific views based on URL path & params
    var args , viewName = null
    if(document.location.pathname === "/" && UmAngularUtil.queryParamsToMap().size  == 0){
      args = UmAngularUtil.queryParamsToMap("?entryid=" + this.umDb.ROOT_UID)
      viewName = 'ContentEntryList'
    }else{
      args = UmAngularUtil.queryParamsToMap();
      viewName = document.location.pathname.split("/")[2]
    }
    this.systemImpl.go(viewName,args, this.context,0);
  }

  ngOnDestroy(): void {
    super.ngOnDestroy()
  }
}
