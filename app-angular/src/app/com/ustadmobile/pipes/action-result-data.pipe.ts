import { Pipe, PipeTransform } from '@angular/core';
import entity from 'UstadMobile-lib-database-entities';
import core from 'UstadMobile-core';
import { UmBaseService } from '../service/um-base.service';

@Pipe({
  name: 'actionResultData'
})
export class ActionResultDataPipe implements PipeTransform {

  private vals: any;
  private systemImpl: any;
  private MessageID: any;
  constructor(private umService: UmBaseService){
    this.systemImpl = core.com.ustadmobile.core.impl.UstadMobileSystemImpl.Companion.instance;
    this.MessageID = core.com.ustadmobile.core.generated.locale.MessageID;
    this.vals = entity.com.ustadmobile.lib.db.entities.StatementEntity.Companion
  }

  transform(value: any, args?: any): any {
    if(value == this.vals.RESULT_SUCCESS){
      return this.systemImpl.getString(this.MessageID.success, this.umService.getContext())
    }else if(value == this.vals.RESULT_FAILURE){
      return this.systemImpl.getString(this.MessageID.failed, this.umService.getContext())
    }
    return "-";
  }

}
