import { com as core} from 'core';
import { com as db } from 'lib-database';
import {Observable} from 'rxjs';

export class UmAngularUtil {

  /**
   * Content dispatch keys - communication btn components
   */
  static DISPATCH_TITLE = "toolbar_title";

  static DISPATCH_RESOURCE = "resouce_ready";

  static DISPATCH_LANGUAGES = "languages_ready";


  /**
   * Convert query parameters to a kotlin map to be used on presenters
   */
  static queryParamsToMap(queryParam ? : string, notFound? :boolean) {
    var paramString = queryParam || document.location.search + (notFound ? "":"&ref=null");
    return core.ustadmobile.core.util.UMFileUtil
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


  static findEntry = function (entries,entryUid) {
    var foundEntry: any = null;
    entries.forEach(entry => {
      if (entry.contentEntryUid as number == entryUid as number) {
        foundEntry = entry;
        return;
      }
    });
    return foundEntry as db.ustadmobile.lib.db.entities.ContentEntry;
  }

  static findChildrenByParentUid(joins: any[], entries: any[], parentEntryUid) {
    var foundEntryUids = [];
    var foundEntries = [];

    joins.forEach(join =>{
      if(join.cepcjParentContentEntryUid == parentEntryUid){
        foundEntryUids.push(join.cepcjChildContentEntryUid);
      }
    });

    foundEntryUids.forEach(entryUid => {
      foundEntries.push(this.findEntry(entries,entryUid));
    });

    return foundEntries;
  }

  
  private static getRoutePathParam(){
    var routePath = document.location.pathname;
    const pathSections = routePath.split("/");
    return {completePath: routePath, path: pathSections[pathSections.length - 1] + "/" };
  }

  static getInitialRoute(entryUid ? : number) {
    var args, view = null
    const route = this.getRoutePathParam();

    if (route.completePath == "/"  || UmAngularUtil.queryParamsToMap().size == 0) {
      args = UmAngularUtil.queryParamsToMap("?entryid=" + entryUid)
      view = 'ContentEntryList/'
    } else if(UmAngularUtil.queryParamsToMap().size > 0 && 
    (route.completePath.includes ("ContentEntryList")  || route.completePath.includes ("ContentEntryList") 
    || route.completePath.includes("Register") || route.completePath.includes("Login") 
    || route.completePath.includes("XapiPackage"))) {
      args = UmAngularUtil.queryParamsToMap();
      view =  route.path;
    }else{
      view = "/NotFound/"
      args = UmAngularUtil.queryParamsToMap("", true)
    }
    return {view: view, args: args};
  }

  static getDifferentLanguageRoute(languageCode){
    var route = this.getRoutePathParam();
    const args = UmAngularUtil.queryParamsToMap();
    return {view: route.path, args: args};
  }


}
