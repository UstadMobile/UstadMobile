import { Component } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { Location } from '@angular/common';
import { UmBaseComponent } from '../um-base-component';
import { UmBaseService } from '../../service/um-base.service';
import { UmDbMockService } from '../../core/db/um-db-mock.service';
import { Subscription } from 'rxjs/internal/Subscription';
import { UmAngularUtil } from '../../util/UmAngularUtil';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent extends UmBaseComponent {

  toolbar_title: string;
  menu_libaries : string;
  menu_reports: string;
  subscription: Subscription;

  constructor(private location: Location,umService: UmBaseService,
              router: Router, route: ActivatedRoute, umDb: UmDbMockService) {
    super(umService, router, route, umDb);
   }

  ngOnInit() {
    super.ngOnInit()
    this.subscription = this.umService.getUmObserver().subscribe(content =>{
      if(content[UmAngularUtil.DISPATCH_TITLE]){
        this.toolbar_title = content[UmAngularUtil.DISPATCH_TITLE];
      }

      if(content[UmAngularUtil.DISPATCH_RESOURCE]){
        //do something where all resources are ready
      }
    });
   
  }

  goBack(){
    this.umService.goBack();
  }

  ngOnDestroy(): void {
    super.ngOnDestroy();
    this.subscription.unsubscribe();
    
  }

}
