import { Subscription } from 'rxjs/internal/Subscription';
import { UmAngularUtil, appRountes } from './com/ustadmobile/util/UmAngularUtil';
import { UmBaseService } from './com/ustadmobile/service/um-base.service';
import { Component, Inject, LOCALE_ID, HostBinding } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { UmBaseComponent } from './com/ustadmobile/view/um-base-component';
import { MzModalService } from 'ngx-materialize';
import { DownloadFromPlaystoreComponent } from './com/ustadmobile/view/download-from-playstore/download-from-playstore.component';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})

export class AppComponent extends UmBaseComponent {

  @HostBinding('attr.dir') dir: string;

  showLoading: boolean = false;
  private initialRoute = {view: {},args: {}}
  private navigationSubscription: Subscription;
  private splashScreenTimeout = () => {
    this.showLoading = false;
    this.systemImpl.go(this.initialRoute.view, this.initialRoute.args, this.context, 0)
  };

  constructor(@Inject(LOCALE_ID) private locale: string, private modalService: MzModalService, localeService: UmBaseService,router: Router,route: ActivatedRoute) {
    super(localeService, router, route);
    if (this.locale.startsWith('en')) {
      this.dir = "ltr";
    } else {
      this.dir = "rtl";
    }

    const storedLocale = localStorage.getItem(this.umService.localeTag)
     this.systemImpl.setLocale(storedLocale ? storedLocale : locale, this.context)
    this.initialRoute = UmAngularUtil.getInitialRoute(this.umService.ROOT_UID);
    this.umService.setSystemDirectionality(this.dir);
    this.umService.setSystemLocale(this.systemImpl.getSystemLocale(this.context).split("-")[0])

    if(UmAngularUtil.isSupportedEnvironment()){ 
      this.modalService.open(DownloadFromPlaystoreComponent);
    }
  }

  ngOnInit(): void {
    super.ngOnInit();
    UmAngularUtil.registerResourceReadyListener(this)
    this.umService.preloadResources()
  }

  onCreate() {
    super.onCreate()
    this.showLoading = UmAngularUtil.getRoutePathParam().path.includes(this.routes.notFound)
     ? false: UmAngularUtil.getRoutePathParam().search == "";
    if(UmAngularUtil.showSplashScreen()) {
      window.setTimeout(this.splashScreenTimeout, 2000) 
    }
  }

  ngOnDestroy(): void {
    super.ngOnDestroy()
    if (this.navigationSubscription) {
      this.navigationSubscription.unsubscribe();
    }
  }
}
