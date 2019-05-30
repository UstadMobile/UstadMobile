import { Component } from '@angular/core';
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

  section_title: string;

  constructor(private location: Location,umService: UmBaseService,
              router: Router, route: ActivatedRoute, umDb: UmDbMockService) {
    super(umService, router, route, umDb);
    
  }

  ngOnInit() {
    super.ngOnInit()
    this.umService.getUmObservable().subscribe(title =>{
      this.section_title = title;
    });
  }

  goBack(){
    this.location.back();
  }

  ngOnDestroy(): void {
    super.ngOnDestroy()
  }

}
