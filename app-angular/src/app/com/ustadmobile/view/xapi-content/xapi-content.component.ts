import {  DomSanitizer } from '@angular/platform-browser';
import { Subscription } from 'rxjs';
import { Component } from '@angular/core';
import { UmBaseComponent } from '../um-base-component';
import { UmBaseService } from '../../service/um-base.service';
import { Router, ActivatedRoute, NavigationEnd } from '@angular/router';
import { UmDbMockService } from '../../core/db/um-db-mock.service';
import core from 'UstadMobile-core';
import { UmAngularUtil } from '../../util/UmAngularUtil';

@Component({
  selector: 'app-xapi-content',
  templateUrl: './xapi-content.component.html',
  styleUrls: ['./xapi-content.component.css']
})


export class XapiContentComponent extends UmBaseComponent {

  private presenter: core.com.ustadmobile.core.controller.XapiPackageContentPresenter;
  navigationSubscription: Subscription;
  urlToLoad: string = "https://www.ustadmobile.com/files/s4s/2-coverletter/en/EPUB/main.html";
  
  
  constructor(umService: UmBaseService, router: Router, route: ActivatedRoute, 
    umDb: UmDbMockService, public sanitizer: DomSanitizer) {
    super(umService, router, route, umDb);

    this.navigationSubscription = this.router.events.filter(event => event instanceof NavigationEnd)
    .subscribe(() => {
      this.presenter = new core.com.ustadmobile.core.controller
        .XapiPackageContentPresenter(this.context, UmAngularUtil.queryParamsToMap(), this);
        this.presenter.onCreate(null);
    });

    }

  ngOnInit() {
    super.ngOnInit();
  }


  setTitle(title){
    this.umService.dispatchUpdate(UmAngularUtil.getContentToDispatch(
      UmAngularUtil.DISPATCH_TITLE, title));
  }
  loadUrl(url){
    this.urlToLoad = url;
  }

  ngOnDestroy(){
    super.ngOnDestroy();
    this.presenter.onDestroy()
    this.navigationSubscription.unsubscribe();
  }

}
