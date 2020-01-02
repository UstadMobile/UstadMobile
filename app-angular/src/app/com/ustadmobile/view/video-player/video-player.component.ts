import { Component, NgZone, OnDestroy } from '@angular/core';
import { UmBaseComponent } from '../um-base-component';
import core from 'UstadMobile-core'
import { UmBaseService } from '../../service/um-base.service';
import { Router, ActivatedRoute, NavigationEnd } from '@angular/router';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { Subscription } from 'rxjs';
import { UmAngularUtil } from '../../util/UmAngularUtil';

@Component({
  selector: 'app-video-player',
  templateUrl: './video-player.component.html',
  styleUrls: ['./video-player.component.css']
})
export class VideoPlayerComponent extends UmBaseComponent implements OnDestroy,
 core.com.ustadmobile.core.view.VideoPlayerView{

  private presenter: core.com.ustadmobile.core.controller.VideoPlayerPresenter
  private navigationSubscription: Subscription;
  safeUrl: SafeResourceUrl = null

  constructor(umservice: UmBaseService, router: Router, route: ActivatedRoute, private sanitizer: DomSanitizer, private zone:NgZone) { 
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
    this.presenter = new core.com.ustadmobile.core.controller.VideoPlayerPresenter(
      this.context, UmAngularUtil.getArgumentsFromQueryParams(), this,
       this.umService.getDbInstance(), this.umService.getDbInstance())
    this.presenter.onCreate(null)
  }

  setVideoInfo(entry){
    this.presenter.onResume()
  }

  setVideoParamsJs(videoPath, audioPath, srtLangList, srtMap){
   this.zone.run(()=>{
     if(this.showIframe == true){
      this.safeUrl = this.getSafeUrl(this.sanitizer, videoPath)
     }else{
       super.openOnNewtab(videoPath)
     }
   })
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
