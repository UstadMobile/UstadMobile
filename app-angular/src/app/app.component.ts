import { UmContextWrapper } from './com/ustadmobile/util/UmContextWrapper';
import { Component, OnInit } from '@angular/core';
import { environment } from 'src/environments/environment.prod';
import { Router, ActivatedRoute } from '@angular/router';
import { com } from 'core';
import { UmDbMockService } from './com/ustadmobile/core/db/um-db-mock.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit{
  
  private readonly umContext: UmContextWrapper;

  constructor(private router: Router, private route: ActivatedRoute, private umDb: UmDbMockService){
    this.umContext = new UmContextWrapper(this.router);
    this.umContext.setActiveRoute(this.route);
  }

  ngOnInit(): void {
    const args = { queryParams: { parentUid: this.umDb.ROOT_UID} };
    com.ustadmobile.core.impl.UstadMobileSystemImpl.Companion.instance.go('contentEntryList',args, this.umContext,0);
  }

  ngOnDestroy(): void {}
}
