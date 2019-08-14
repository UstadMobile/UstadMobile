import ktorclientserial from 'ktor-ktor-client-serialization';
import core from 'UstadMobile-core';
import {Observable} from 'rxjs';

export class UmAngularUtil {

  /**
   * Content dispatch keys - communication btn components
   */
  static DISPATCH_TITLE : string = "toolbar_title";

  static DISPATCH_RESOURCE : string = "resouce_ready";


  /**
   * Convert query parameters to a kotlin map to be used on presenters
   */
  static queryParamsToMap(queryParam ? : string, notFound? :boolean) {
    const ktor = ktorclientserial 
    var paramString = queryParam || document.location.search + (notFound ? "":"&ref=null");
    return core.com.ustadmobile.core.util.UMFileUtil
      .parseURLQueryString(paramString);
  }

  static getContentToDispatch(key, value){
    const content = {}
    content[key] = value;
    return content;
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
    const pathSections = routePath.split("/");
    const route =  pathSections[pathSections.length - 1] + "/";

    if (routePath == "/"  || UmAngularUtil.queryParamsToMap().size == 0) {
      args = UmAngularUtil.queryParamsToMap("?entryid=" + entryUid)
      view = 'ContentEntryList/'
    } else if(UmAngularUtil.queryParamsToMap().size > 0 && 
    (routePath.includes ("ContentEntryList")  || routePath.includes ("ContentEntryList") 
    || routePath.includes("Register") || routePath.includes("Login"))) {
      args = UmAngularUtil.queryParamsToMap();
      view =  route;
    }else{
      view = "/NotFound/"
      args = UmAngularUtil.queryParamsToMap("", true)
    }
    return {view: view, args: args};
  }
}
