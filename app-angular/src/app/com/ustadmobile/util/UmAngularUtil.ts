import {com as core} from 'core';
import {Observable} from 'rxjs';

export class UmAngularUtil{

    /**
     * Convert query parameters to a kotlin map to be used on presenters
     */
    static queryParamsToMap(){
       return core.ustadmobile.core.util.UMFileUtil
         .parseURLQueryString(document.location.search);
    }

    static createObserver(dataToObserve) {
      return new Observable(observer => {
          setTimeout(() => {
            observer.next(dataToObserve);
          }, 300);
        });
      }
}
