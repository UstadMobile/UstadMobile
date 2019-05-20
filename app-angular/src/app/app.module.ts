import { ContentEntryListComponent } from './com/ustadmobile/view/content-entry-list/content-entry-list.component';
import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { MzButtonModule , MzCardModule, MzSidenavModule, MzNavbarModule} from 'ngx-materialize';
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { HomeComponent } from './com/ustadmobile/view/home/home.component';
import { UmWordLimitPipe } from './com/ustadmobile/util/pipes/um-word-limit.pipe';

@NgModule({
  declarations: [
    AppComponent,
    ContentEntryListComponent,
    HomeComponent,
    UmWordLimitPipe
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    MzSidenavModule,
    MzCardModule,
    MzButtonModule,
    MzNavbarModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
