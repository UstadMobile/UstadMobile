import { HomeComponent } from './com/ustadmobile/view/home/home.component';
import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { ContentEntryListComponent } from './com/ustadmobile/view/content-entry-list/content-entry-list.component';
import { ContentEntryDetailsComponent } from './com/ustadmobile/view/content-entry-details/content-entry-details.component';

const routes: Routes = [
  {path: 'home', component: HomeComponent,
        children: [
          {path: 'entryList/:entryUid', component: ContentEntryListComponent},
          {path: 'entry/:entryUid', component: ContentEntryDetailsComponent},
        ]
  }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {}
