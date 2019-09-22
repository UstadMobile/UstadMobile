import { Subscription } from 'rxjs/internal/Subscription';
import { UmAngularUtil } from './com/ustadmobile/util/UmAngularUtil';
import { UmBaseService } from './com/ustadmobile/service/um-base.service';
import { Component, Inject, LOCALE_ID, HostBinding } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { UmDbMockService, ContentEntryDao } from './com/ustadmobile/core/db/um-db-mock.service';
import { UmBaseComponent } from './com/ustadmobile/view/um-base-component';
import { combineLatest } from 'rxjs/internal/observable/combineLatest';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})

export class AppComponent extends UmBaseComponent {

  @HostBinding('attr.dir') dir: string;

  showLoading: boolean = false;
  private navigationSubscription : Subscription;
  private splashScreenTimeout  = () => {
    this.showLoading = false; 
     //check and redirect to a specific views based on URL path & params
    const initialRoute = UmAngularUtil.getInitialRoute(this.umDb.ROOT_UID);
    this.systemImpl.go(initialRoute.view, initialRoute.args, this.context, 0)
  };

  constructor(@Inject(LOCALE_ID) private locale: string, localeService: UmBaseService, router: Router,
    route: ActivatedRoute, private umDb: UmDbMockService) {
    super(localeService, router, route, umDb);
    if (this.locale.startsWith('en')) {
      this.dir = "ltr";
    } else {
      this.dir = "rtl";
    }
    this.umService.setSystemDirectionality(this.dir);
  }

  ngOnInit(): void {
    super.ngOnInit();
    const systemLocale = this.systemImpl.getSystemLocale(this.context).split("-")[0];
    this.showLoading = window.location.search == "";

    //Load all resources async 
    combineLatest([this.umService.loadEntries(),this.umService.loadEntryJoins(),this.umService.loadStrings(systemLocale)
    ]).subscribe(responses => {
      this.umDatabase.contentEntryDao = new ContentEntryDao(responses[0], responses[1])
      this.systemImpl.setLocaleStrings(responses[2])
      this.umService.dispatchUpdate(UmAngularUtil.getContentToDispatch(
        UmAngularUtil.DISPATCH_RESOURCE, true))
        if(UmAngularUtil.showSplashScreen()){ 
          window.setTimeout(this.splashScreenTimeout, 3500)
        }
    })
  }

  ngOnDestroy(): void {
    super.ngOnDestroy()
    this.navigationSubscription.unsubscribe();
  }
}
