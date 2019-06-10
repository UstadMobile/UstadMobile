import { ContentEntryListComponent } from './com/ustadmobile/view/content-entry-list/content-entry-list.component';
import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { MzButtonModule ,MzSelectModule, MzCardModule, MzSidenavModule,MzSpinnerModule,MzProgressModule,
   MzNavbarModule, MzChipModule, MzToastModule, MzInputModule} from 'ngx-materialize';
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { HomeComponent } from './com/ustadmobile/view/home/home.component';
import { ContentEntryDetailComponent } from './com/ustadmobile/view/content-entry-detail/content-entry-detail.component';
import { HTTP_INTERCEPTORS, HttpClientModule, HttpClient } from '@angular/common/http';
import {NgProgressInterceptor, NgProgressModule } from 'ngx-progressbar';
import { UmDbMockService } from './com/ustadmobile/core/db/um-db-mock.service';
import { InfiniteScrollModule } from 'ngx-infinite-scroll';
import { UmBaseService } from './com/ustadmobile/service/um-base.service';
import { NotFoundComponent } from './com/ustadmobile/view/not-found/not-found.component';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RegisterComponent } from './com/ustadmobile/view/register/register.component';
import { LoginComponent } from './com/ustadmobile/view/login/login.component';
import { AuthGuard } from './com/ustadmobile/service/AuthGuard';

@NgModule({
  declarations: [
    AppComponent,
    ContentEntryListComponent,
    HomeComponent,
    ContentEntryDetailComponent,
    NotFoundComponent,
    RegisterComponent,
    LoginComponent
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
    FormsModule,
    MzToastModule,
    MzInputModule,
    MzProgressModule,
    ReactiveFormsModule,
    InfiniteScrollModule
  ],
  providers: [
    UmDbMockService,
    UmBaseService,
    AuthGuard,
    { provide: HTTP_INTERCEPTORS, useClass: NgProgressInterceptor, multi: true }
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
