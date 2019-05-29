import { Subscription } from 'rxjs';
import { Component, OnInit } from '@angular/core';
import { environment } from 'src/environments/environment.prod';
import { Router, ActivatedRoute } from '@angular/router';
import { Location } from '@angular/common';
import { UmBaseComponent } from '../um-base-component';
import { UmBaseService } from '../../service/um-base.service';
import { UmDbMockService } from '../../core/db/um-db-mock.service';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent extends UmBaseComponent {

  entryTitleSubscription: Subscription
  constructor(private location: Location,localeService: UmBaseService,
              router: Router, route: ActivatedRoute, private umDb: UmDbMockService) {
    super(localeService, router, route, umDb);
    
  }

  ngOnInit() {
    super.ngOnInit()
    this.entryTitleSubscription = this.subject.asObservable().subscribe(title => {
      console.log("subscripton", title);
    });
  }

  goBack(){
    this.location.back();
  }



  ngOnDestroy(): void {
    super.ngOnDestroy()
    //this.entryTitleSubscription.unsubscribe()
  }

}
