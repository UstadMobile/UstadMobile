import { Component, OnInit, NgZone, OnDestroy } from '@angular/core';
import core from 'UstadMobile-core'
import { UmBaseComponent } from '../um-base-component';
import { UmBaseService } from '../../service/um-base.service';
import { Router, ActivatedRoute, NavigationEnd } from '@angular/router';
import { DomSanitizer } from '@angular/platform-browser';
import { Subscription } from 'rxjs';
import { UmAngularUtil } from '../../util/UmAngularUtil';

@Component({
  selector: 'app-xapi-content',
  templateUrl: './xapi-content.component.html',
  styleUrls: ['./xapi-content.component.css']
})


export class XapiContentComponent extends UmBaseComponent implements OnDestroy,
 core.com.ustadmobile.core.view.XapiPackageContentView {

  private presenter: core.com.ustadmobile.core.controller.XapiPackageContentPresenter
  private navigationSubscription: Subscription;
  urlToLoad: string = ""

  constructor(umservice: UmBaseService, router: Router, route: ActivatedRoute, public sanitizer: DomSanitizer, private zone:NgZone) { 
    super(umservice,router, route)
     //Listen for the navigation changes - changes on url
     this.navigationSubscription = this.router.events.filter(event => event instanceof NavigationEnd)
     .subscribe(_ => {
       UmAngularUtil.registerResourceReadyListener(this)
     });
  }

  ngOnInit() {
    super.ngOnInit()
  }

  onCreate(){
    super.onCreate()
    this.presenter = new core.com.ustadmobile.core.controller.XapiPackageContentPresenter(
      this.context, UmAngularUtil.getArgumentsFromQueryParams(), this, this.containerMounter)
    this.presenter.onCreate(null)
  }

  
  containerMounter(containerUid: any){
    return UmAngularUtil.getMountPath(containerUid)
  }

  loadUrl(url){
    this.zone.run(()=>{
      this.urlToLoad = url
    })
  }

  setTitle(title){
    super.setToolbarTitle(title)
  }

  ngOnDestroy(): void {
    super.ngOnDestroy();
    if (this.presenter) {
      this.presenter.onDestroy();
    }
    if (this.navigationSubscription) {
      this.navigationSubscription.unsubscribe();
    }
  }

}
