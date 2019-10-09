import { LoginComponent } from './com/ustadmobile/view/login/login.component';
import { NotFoundComponent } from './com/ustadmobile/view/not-found/not-found.component';
import { ContentEntryDetailComponent } from './com/ustadmobile/view/content-entry-detail/content-entry-detail.component';
import { HomeComponent } from './com/ustadmobile/view/home/home.component';
import { NgModule } from '@angular/core';
import { Routes, RouterModule, Router } from '@angular/router';
import { ContentEntryListComponent } from './com/ustadmobile/view/content-entry-list/content-entry-list.component';
import { RegisterComponent } from './com/ustadmobile/view/register/register.component';
import { ReportDashboardComponent } from './com/ustadmobile/view/report-dashboard/report-dashboard.component';
import { XapiReportOptionsComponent } from './com/ustadmobile/view/xapi-report-options/xapi-report-options.component';
import { XapiReportDetailsComponent } from './com/ustadmobile/view/xapi-report-details/xapi-report-details.component';
import { XapiTreeviewDialogComponent } from "./com/ustadmobile/view/xapi-treeview-dialog/XapiTreeviewDialogComponent";
import { RouteGuardService } from './com/ustadmobile/service/route-guard.service';
import { EpubContentComponent } from './com/ustadmobile/view/epub-content/epub-content.component';
import { VideoPlayerComponent } from './com/ustadmobile/view/video-player/video-player.component';
import { XapiContentComponent } from './com/ustadmobile/view/xapi-content/xapi-content.component';
import { WebChunkComponent } from './com/ustadmobile/view/web-chunk/web-chunk.component';
import { UserProfileComponent } from './com/ustadmobile/view/user-profile/user-profile.component';
import { appRountes } from './com/ustadmobile/util/UmAngularUtil';

const routes: Routes = [
  {path: 'Home', component: HomeComponent,
        children: [
          {path: 'ContentEntryList', component: ContentEntryListComponent},
          {path: 'ContentEntryDetail', component: ContentEntryDetailComponent},
          {path: 'RegisterAccount', component: RegisterComponent},
          {path: 'Login', component: LoginComponent},
          {path: "EpubContent", component: EpubContentComponent},
          {path: "VideoPlayer", component: VideoPlayerComponent},
          {path: "XapiContent", component: XapiContentComponent},
          {path: "WebChunk", component: WebChunkComponent},
          {path: "ReportDashboard", component: ReportDashboardComponent},
          {path: "ReportOptions", component: XapiReportOptionsComponent},
          {path: "EntriesTreeDialog",component: XapiTreeviewDialogComponent},
          {path: "ReportPreview", component: XapiReportDetailsComponent},
          {path: "UserProfile", component: UserProfileComponent},
          {path: "Home", component: HomeComponent}
        ],
        canActivate: [RouteGuardService],
        runGuardsAndResolvers: "always"
  },
  {path: "NotFound", component: NotFoundComponent}
];

@NgModule({
  imports: [RouterModule.forRoot(routes, {onSameUrlNavigation: "reload"})],
  exports: [RouterModule]
})
export class AppRoutingModule {
  constructor(private router: Router) {
    this.router.errorHandler = (error: any) => {
        
        this.router.navigate([appRountes.notFound]);
    }
  }
}
