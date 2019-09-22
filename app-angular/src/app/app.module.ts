import { ContentEntryListComponent } from './com/ustadmobile/view/content-entry-list/content-entry-list.component';
import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { MzButtonModule ,MzSelectModule, MzCardModule, MzSidenavModule,MzSpinnerModule,MzProgressModule,
   MzNavbarModule, MzChipModule, MzToastModule, MzInputModule, MzDropdownModule, MzModalModule} from 'ngx-materialize';
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
import { AuthGuard } from './com/ustadmobile/service/AuthGuard';
import { XapiContentComponent } from './com/ustadmobile/view/xapi-content/xapi-content.component';
import { GoogleChartsModule } from 'angular-google-charts';
import { ReportDashboardComponent } from './com/ustadmobile/view/report-dashboard/report-dashboard.component';

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
    ReportDashboardComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    MzSidenavModule,
    MzCardModule,
    MzButtonModule,
    MzNavbarModule,
    MzChipModule,
    NgProgressModule,
    HttpClientModule,
    MzSelectModule,
    MzSpinnerModule,
    MzModalModule,
    FormsModule,
    MzToastModule,
    MzInputModule,
    MzProgressModule,
    MzDropdownModule,
    ReactiveFormsModule,
    GoogleChartsModule
  ],
  providers: [
    UmDbMockService,
    UmBaseService,
    AuthGuard,
    { provide: HTTP_INTERCEPTORS, useClass: NgProgressInterceptor, multi: true }
  ],
  bootstrap: [AppComponent]
})
export class AppModule{}
