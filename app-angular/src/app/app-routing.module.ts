import { XapiContentComponent } from './com/ustadmobile/view/xapi-content/xapi-content.component';
import { LoginComponent } from './com/ustadmobile/view/login/login.component';
import { NotFoundComponent } from './com/ustadmobile/view/not-found/not-found.component';
import { ContentEntryDetailComponent } from './com/ustadmobile/view/content-entry-detail/content-entry-detail.component';
import { HomeComponent } from './com/ustadmobile/view/home/home.component';
import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { ContentEntryListComponent } from './com/ustadmobile/view/content-entry-list/content-entry-list.component';
import { RegisterComponent } from './com/ustadmobile/view/register/register.component';
import { AuthGuard } from './com/ustadmobile/service/AuthGuard';
import { ReportDashboardComponent } from './com/ustadmobile/view/report-dashboard/report-dashboard.component';
import { XapiReportOptionsComponent } from './com/ustadmobile/view/xapi-report-options/xapi-report-options.component';
import { XapiReportDetailsComponent } from './com/ustadmobile/view/xapi-report-details/xapi-report-details.component';
import { XapiTreeviewDialogComponent } from './com/ustadmobile/view/xapi-treeview-dialog/xapi-treeview-dialog.component';

const routes: Routes = [
  {path: 'Home', component: HomeComponent,
        children: [
          {path: 'ContentEntryList', component: ContentEntryListComponent},
          {path: 'ContentEntryDetail', component: ContentEntryDetailComponent},
          {path: 'RegisterAccount', component: RegisterComponent},
          {path: 'Login', component: LoginComponent},
          {path: "XapiPackage", component: XapiContentComponent},
          {path: "VideoPlayer", component: XapiContentComponent},
          {path: "webChunk", component: XapiContentComponent},
          {path: "Container", component: XapiContentComponent},
          {path: "ReportDashboard", component: ReportDashboardComponent},
          {path: "ReportOptions", component: XapiReportOptionsComponent},
          {path: "EntriesTreeDialog",component: XapiTreeviewDialogComponent},
          {path: "ReportPreviewView", component: XapiReportDetailsComponent} 
        ],
        canActivate: [AuthGuard],
        runGuardsAndResolvers: "always"
  },
  {path: "NotFound", component: NotFoundComponent}
];

@NgModule({
  imports: [RouterModule.forRoot(routes, {onSameUrlNavigation: "reload"})],
  exports: [RouterModule]
})
export class AppRoutingModule {}
