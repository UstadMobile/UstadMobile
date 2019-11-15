import { ContentEntryListComponent } from './com/ustadmobile/view/content-entry-list/content-entry-list.component';
import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { MzButtonModule ,MzSelectModule, MzCardModule, MzSidenavModule,MzSpinnerModule,MzProgressModule,
   MzNavbarModule, MzChipModule, MzDatepickerModule,MzCheckboxModule, MzToastModule, MzInputModule, MzDropdownModule, MzModalModule} from 'ngx-materialize';
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { HomeComponent } from './com/ustadmobile/view/home/home.component';
import { ContentEntryDetailComponent } from './com/ustadmobile/view/content-entry-detail/content-entry-detail.component';
import { HTTP_INTERCEPTORS, HttpClientModule } from '@angular/common/http';
import {NgProgressInterceptor, NgProgressModule } from 'ngx-progressbar';
import { UmBaseService } from './com/ustadmobile/service/um-base.service';
import { NotFoundComponent } from './com/ustadmobile/view/not-found/not-found.component';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RegisterComponent } from './com/ustadmobile/view/register/register.component';
import { LoginComponent } from './com/ustadmobile/view/login/login.component';
import { GoogleChartsModule } from 'angular-google-charts';
import { ReportDashboardComponent } from './com/ustadmobile/view/report-dashboard/report-dashboard.component';
import { XapiReportOptionsComponent } from './com/ustadmobile/view/xapi-report-options/xapi-report-options.component';
import { XapiReportDetailsComponent } from './com/ustadmobile/view/xapi-report-details/xapi-report-details.component';
import { XapiTreeviewDialogComponent } from "./com/ustadmobile/view/xapi-treeview-dialog/XapiTreeviewDialogComponent";
import { DatePipe, LocationStrategy, HashLocationStrategy } from '@angular/common';
import { InfiniteScrollModule } from 'ngx-infinite-scroll';
import { RouteGuardService } from './com/ustadmobile/service/route-guard.service';
import { ActionResultDataPipe } from './com/ustadmobile/pipes/action-result-data.pipe';
import { UmTreeNodeComponent } from './com/ustadmobile/view/xapi-treeview-dialog/um-tree-node/um-tree-node.component';
import { EpubContentComponent } from './com/ustadmobile/view/epub-content/epub-content.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { VideoPlayerComponent } from './com/ustadmobile/view/video-player/video-player.component';
import { XapiContentComponent } from './com/ustadmobile/view/xapi-content/xapi-content.component';
import { WebChunkComponent } from './com/ustadmobile/view/web-chunk/web-chunk.component';
import { UserProfileComponent } from './com/ustadmobile/view/user-profile/user-profile.component';
import { DownloadFromPlaystoreComponent } from './com/ustadmobile/view/download-from-playstore/download-from-playstore.component';


@NgModule({
  declarations: [
    AppComponent,
    ContentEntryListComponent,
    HomeComponent,
    ContentEntryDetailComponent,
    NotFoundComponent,
    RegisterComponent,
    LoginComponent,
    ReportDashboardComponent,
    XapiReportOptionsComponent,
    XapiReportDetailsComponent,
    XapiTreeviewDialogComponent,
    DownloadFromPlaystoreComponent,
    ActionResultDataPipe,
    UmTreeNodeComponent,
    EpubContentComponent,
    VideoPlayerComponent,
    XapiContentComponent,
    WebChunkComponent,
    UserProfileComponent
  ],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    AppRoutingModule,
    NgProgressModule,
    HttpClientModule,
    MzSelectModule,MzSidenavModule,MzCardModule,MzButtonModule,MzNavbarModule,MzCheckboxModule,
    MzChipModule,MzSpinnerModule,MzModalModule,MzToastModule,MzInputModule,MzProgressModule,MzDropdownModule,
    MzDatepickerModule,
    FormsModule,
    InfiniteScrollModule,
    ReactiveFormsModule,
    GoogleChartsModule,
  ],
  entryComponents: [DownloadFromPlaystoreComponent],
  providers: [
    UmBaseService,
    DatePipe,
    RouteGuardService,
    {provide: LocationStrategy, useClass: HashLocationStrategy},
    { provide: HTTP_INTERCEPTORS, useClass: NgProgressInterceptor, multi: true }
  ],
  bootstrap: [AppComponent]
})
export class AppModule{}
