import {Component, OnDestroy} from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { UmBaseComponent } from '../um-base-component';
import { UmBaseService } from '../../service/um-base.service';
import { UmAngularUtil } from '../../util/UmAngularUtil';

@Component({
  selector: 'app-not-found',
  templateUrl: './not-found.component.html',
  styleUrls: ['./not-found.component.css']
})
export class NotFoundComponent extends UmBaseComponent implements OnDestroy {

  constructor(umService: UmBaseService, router: Router, route: ActivatedRoute) {
    
    super(umService, router, route);
  }

  goBack(){
     this.systemImpl.go(this.routes.entryList, 
      UmAngularUtil.getArgumentsFromQueryParams({params: "?" 
      + UmAngularUtil.ARG_CONTENT_ENTRY_UID + "="+this.umService.ROOT_UID}), this.context)
  }

  ngOnInit() {
    super.ngOnInit()
  }

}
