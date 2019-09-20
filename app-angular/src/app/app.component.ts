import { Subscription } from 'rxjs/internal/Subscription';
import { UmAngularUtil } from './com/ustadmobile/util/UmAngularUtil';
import { UmBaseService } from './com/ustadmobile/service/um-base.service';
import { Component, Inject, LOCALE_ID, HostBinding } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { UmDbMockService } from './com/ustadmobile/core/db/um-db-mock.service';
import { UmBaseComponent } from './com/ustadmobile/view/um-base-component';

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

  constructor(@Inject(LOCALE_ID) private locale: string, localeService: UmBaseService,router: Router,route: ActivatedRoute) {
    super(localeService, router, route);
    this.umService.setEnvironment(document.location.search.indexOf("test") == -1)
    if (this.locale.startsWith('en')) {
      this.dir = "ltr";
    } else {
      this.dir = "rtl";
    }

    this.initialRoute = UmAngularUtil.getInitialRoute(this.umService.ROOT_UID);
    this.umService.setSystemDirectionality(this.dir);
    this.umService.setSystemLocale(this.systemImpl.getSystemLocale(this.context).split("-")[0])
  }

  ngOnInit(): void {
    super.ngOnInit();
    UmAngularUtil.registerResourceReadyListener(this)
    this.umService.preloadResources()
  }

  onCreate() {
    super.onCreate()
    this.showLoading = window.location.search == "";
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
