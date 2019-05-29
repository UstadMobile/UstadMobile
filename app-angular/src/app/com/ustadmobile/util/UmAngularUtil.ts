import {com as core} from 'core';
import {Observable} from 'rxjs';

export class UmAngularUtil{

    /**
     * Convert query parameters to a kotlin map to be used on presenters
     */
    static queryParamsToMap(queryParam?: string){
      const paramString = queryParam || document.location.search + "&ref=null";
       return core.ustadmobile.core.util.UMFileUtil
         .parseURLQueryString(paramString);
    }

    static createObserver(dataToObserve) {
      return new Observable(observer => {
          setTimeout(() => {
            observer.next(dataToObserve);
          }, 300);
        });
      }

      static findObjectByLabel = function(obj, label,value) {
        var foundObj: any = null;
        Object.keys(obj).forEach(key => {
          (obj[key] as any[]).forEach(element => {
            if(element[label] === value){
             foundObj = element;
             return;
            }
          })
          if(foundObj !== null) return;
        })
        return foundObj;
      }
}
