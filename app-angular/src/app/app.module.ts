import { ContentEntryListComponent } from './com/ustadmobile/view/content-entry-list/content-entry-list.component';
import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { MzButtonModule ,MzSelectModule, MzCardModule, MzSidenavModule,MzSpinnerModule,MzProgressModule,
   MzNavbarModule, MzChipModule, MzDatepickerModule, MzToastModule, MzInputModule, MzDropdownModule, MzModalModule, MzModalService, MzBaseModal} from 'ngx-materialize';
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { HomeComponent } from './com/ustadmobile/view/home/home.component';
import { ContentEntryDetailComponent } from './com/ustadmobile/view/content-entry-detail/content-entry-detail.component';
import { HTTP_INTERCEPTORS, HttpClientModule } from '@angular/common/http';
import {NgProgressInterceptor, NgProgressModule } from 'ngx-progressbar';
import { UmDbMockService } from './com/ustadmobile/core/db/um-db-mock.service';
import { UmBaseService } from './com/ustadmobile/service/um-base.service';
import { NotFoundComponent } from './com/ustadmobile/view/not-found/not-found.component';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RegisterComponent } from './com/ustadmobile/view/register/register.component';
import { LoginComponent } from './com/ustadmobile/view/login/login.component';
import { XapiContentComponent } from './com/ustadmobile/view/xapi-content/xapi-content.component';
import { GoogleChartsModule } from 'angular-google-charts';
import { ReportDashboardComponent } from './com/ustadmobile/view/report-dashboard/report-dashboard.component';
import { XapiReportOptionsComponent } from './com/ustadmobile/view/xapi-report-options/xapi-report-options.component';
import { XapiReportDetailsComponent } from './com/ustadmobile/view/xapi-report-details/xapi-report-details.component';
import { XapiTreeviewDialogComponent } from "./com/ustadmobile/view/xapi-treeview-dialog/XapiTreeviewDialogComponent";
import { DatePipe } from '@angular/common';
import { RouteGuardService } from './com/ustadmobile/service/route-guard.service';
import { ActionResultDataPipe } from './com/ustadmobile/pipes/action-result-data.pipe';
import { UmTreeNodeComponent } from './com/ustadmobile/view/xapi-treeview-dialog/um-tree-node/um-tree-node.component';


@NgModule({
  declarations: [
    AppComponent,
    ContentEntryListComponent,
    HomeComponent,
    ContentEntryDetailComponent,
    NotFoundComponent,
    RegisterComponent,
    LoginComponent,
    XapiContentComponent,
    ReportDashboardComponent,
    XapiReportOptionsComponent,
    XapiReportDetailsComponent,
    XapiTreeviewDialogComponent,
    ActionResultDataPipe,
    UmTreeNodeComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    NgProgressModule,
    HttpClientModule,
    MzSelectModule,MzSidenavModule,MzCardModule,MzButtonModule,MzNavbarModule,
    MzChipModule,MzSpinnerModule,MzModalModule,MzToastModule,MzInputModule,MzProgressModule,MzDropdownModule,
    MzDatepickerModule,
    FormsModule,
    ReactiveFormsModule,
    GoogleChartsModule,
  ],
  providers: [
    UmDbMockService,
    UmBaseService,
    DatePipe,
    RouteGuardService,
    { provide: HTTP_INTERCEPTORS, useClass: NgProgressInterceptor, multi: true }
  ],
  bootstrap: [AppComponent]
})
export class AppModule{}
