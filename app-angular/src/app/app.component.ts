import { Subscription } from 'rxjs/internal/Subscription';
import { UmAngularUtil } from './com/ustadmobile/util/UmAngularUtil';
import { UmBaseService } from './com/ustadmobile/service/um-base.service';
import { Component, Inject, LOCALE_ID, HostBinding } from '@angular/core';
import { Router, ActivatedRoute, NavigationEnd } from '@angular/router';
import { UmDbMockService, ContentEntryDao } from './com/ustadmobile/core/db/um-db-mock.service';
import { UmBaseComponent } from './com/ustadmobile/view/um-base-component';
import { UmAppDatabaseService } from './com/ustadmobile/core/db/um-app-database.service';

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

  constructor(@Inject(LOCALE_ID) private locale: string, localeService: UmBaseService, router: Router,
    route: ActivatedRoute, private umDb: UmDbMockService, private db: UmAppDatabaseService) {
    super(localeService, router, route, umDb);
    db.getInstance(this.context) 
    if (this.locale.startsWith('en')) {
      this.dir = "ltr";
    } else {
      this.dir = "rtl";
    }

    this.initialRoute = UmAngularUtil.getInitialRoute(this.umDb.ROOT_UID);
    this.umService.setSystemDirectionality(this.dir);
    this.umService.setSystemLocale(this.systemImpl.getSystemLocale(this.context).split("-")[0])
  }

  ngOnInit(): void {
    super.ngOnInit();
    UmAngularUtil.registerResourceReadyListener(this)
    this.umService.preloadResources().subscribe(responses => { 
      this.umDatabase.contentEntryDao = new ContentEntryDao(responses[0], responses[1])
      this.systemImpl.setLocaleStrings(responses[2])
    })
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
