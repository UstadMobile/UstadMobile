import {Component} from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { UmBaseComponent } from '../um-base-component';
import { UmBaseService } from '../../service/um-base.service';

@Component({
  selector: 'app-not-found',
  templateUrl: './not-found.component.html',
  styleUrls: ['./not-found.component.css']
})
export class NotFoundComponent extends UmBaseComponent {

  constructor(umService: UmBaseService, router: Router, route: ActivatedRoute) {
    
    super(umService, router, route);
  }

  ngOnInit() {
    super.ngOnInit()
  }

}
