import { UmContextWrapper } from './com/ustadmobile/util/UmContextWrapper';
import { Component, OnInit } from '@angular/core';
import { environment } from 'src/environments/environment.prod';
import { Router, ActivatedRoute } from '@angular/router';
import { com } from 'core';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit{
  
  private parentUid = "E130B099-5C18-E0899-6817-009BCAC1111E6";
  private readonly umContext: UmContextWrapper;

  constructor(private router: Router, private route: ActivatedRoute){
    this.umContext = new UmContextWrapper(this.router);
    this.umContext.setActiveRoute(this.route);
  }

  ngOnInit(): void {
    const args = { queryParams: { parentUid: this.parentUid} };
    com.ustadmobile.core.impl.UstadMobileSystemImpl.Companion.instance.go('contentEntryList',args, this.umContext,0);
  }

  ngOnDestroy(): void {}
}
