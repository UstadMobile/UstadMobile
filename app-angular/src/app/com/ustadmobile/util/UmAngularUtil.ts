import {com as core} from 'core';
import {Observable} from 'rxjs';

export class UmAngularUtil {

  /**
   * Convert query parameters to a kotlin map to be used on presenters
   */
  static queryParamsToMap(queryParam ? : string, notFound? :boolean) {
    var paramString = queryParam || document.location.search + (notFound ? "":"&ref=null");
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

  static findObjectByLabel = function (obj, label, value) {
    var foundObj: any = null;
    Object.keys(obj).forEach(key => {
      (obj[key] as any[]).forEach(element => {
        if (element[label] == value) {
          foundObj = element;
          return;
        }
      })
    })
    return foundObj;
  }

  

  static getInitialRoute(entryUid ? : number) {
    var args, view = null
    var routePath = document.location.pathname;
    if (routePath == "/"  || UmAngularUtil.queryParamsToMap().size == 0) {
      args = UmAngularUtil.queryParamsToMap("?entryid=" + entryUid)
      view = 'ContentEntryList/'
    } else if(UmAngularUtil.queryParamsToMap().size > 0 && 
    (routePath.includes ("ContentEntryList")  || routePath.includes ("ContentEntryList"))) {
      args = UmAngularUtil.queryParamsToMap();
      const pathSections = routePath.split("/");
      view =  pathSections[pathSections.length - 1] + "/";
    }else{
      view = "/NotFound/";
      args = UmAngularUtil.queryParamsToMap("", true)
    }
    return {view: view, args: args};
  }
}
