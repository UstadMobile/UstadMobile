import { Component, NgZone, OnDestroy } from '@angular/core';
import core from 'UstadMobile-core'
import { UmBaseComponent } from '../um-base-component';
import { Subscription } from 'rxjs';
import { UmBaseService } from '../../service/um-base.service';
import { Router, ActivatedRoute, NavigationEnd } from '@angular/router';
import { DomSanitizer } from '@angular/platform-browser';
import { UmAngularUtil } from '../../util/UmAngularUtil';
@Component({
  selector: 'app-web-chunk',
  templateUrl: './web-chunk.component.html',
  styleUrls: ['./web-chunk.component.css']
})
export class WebChunkComponent extends UmBaseComponent implements OnDestroy,
 core.com.ustadmobile.core.view.WebChunkView {

  
  private presenter: core.com.ustadmobile.core.controller.WebChunkPresenter;
  private navigationSubscription: Subscription;
  urlToLoad: string

  constructor(umservice: UmBaseService, router: Router, route: ActivatedRoute, public sanitizer: DomSanitizer, private zone:NgZone) {
    super(umservice,router, route)

    //Listen for the navigation changes - changes on url
    this.navigationSubscription = this.router.events.filter(event => event instanceof NavigationEnd)
      .subscribe(_ => {
        UmAngularUtil.registerResourceReadyListener(this)
      });
  }

  onCreate(){
    super.onCreate()
    this.presenter = new core.com.ustadmobile.core.controller.WebChunkPresenter(
      this.context, UmAngularUtil.getArgumentsFromQueryParams(), this,false, this.umService.getDbInstance())
    this.presenter.onCreate(null)
  }

  ngOnInit() {
    super.ngOnInit()}

  mountChunk(container, callback){
    callback.onSuccess(UmAngularUtil.getMountPath(container.containerUid))
  }

  loadUrl(url){
    this.zone.run(()=>{
      if(this.showIframe == true){
        this.urlToLoad = url
      }else{
        super.openOnNewtab(url)
      }
    })
  }

  showErrorWithAction(message, app, mimetype){
    super.showError(message)
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
