import ktorclientserial from 'ktor-ktor-client-serialization';
import core from 'UstadMobile-core';
import db from 'UstadMobile-lib-database';
import {Observable} from 'rxjs';

export const appRountes = {
  "list":"ContentEntryList", "details":"ContentEntryDetail", "register":"RegisterAccount",
  "login":"Login", "xapi":"XapiPackage", "video":"VideoPlayer", "web":"webChunk", 
  "container":"Container", "report":"Reports","reportDetails":"ReportDetails"
}

export class UmAngularUtil {
  constructor(){
    const ktor = ktorclientserial 
  }

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


  static findEntry = function (entries,entryUid) {
    var foundEntry: any = null;
    entries.forEach(entry => {
      if (entry.contentEntryUid as number == entryUid as number) {
        foundEntry = entry;
        return;
      }
    });
    return foundEntry as db.com.ustadmobile.lib.db.entities.ContentEntry;
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
    const mPaths = routePath.split("/");
    return {completePath: routePath, path: mPaths[mPaths.length - 1], size:  mPaths.length};
  }

  private static hasPath(mPath){
    var foundPath = false
    Object.keys(appRountes).forEach(pathKey => {
      if(mPath == appRountes[pathKey]){
        foundPath = true;
        return;
      }
    });
    return foundPath;
  }

  static getInitialRoute(entryUid ? : number) {
    var args, view = null
    const mPath = this.getRoutePathParam();
    if(mPath.size >= 4){
      //redirect as it is
      view = this.hasPath(mPath.path) ? mPath.path : "NotFound/"
      args = UmAngularUtil.queryParamsToMap();
    }else if(mPath.size <= 3){
      //redirect to default
      view = this.hasPath(mPath.path) ? mPath.path + "/" : "ContentEntryList/"
      args = !this.hasPath(mPath.path) ? UmAngularUtil.queryParamsToMap("?entryid=" + entryUid) 
      : UmAngularUtil.queryParamsToMap(document.location.search.length > 0 ? document.location.search: "?")
    }
    return {view: view, args: args};
  }

  static getDifferentLanguageRoute(languageCode){
    var route = this.getRoutePathParam();
    const args = UmAngularUtil.queryParamsToMap();
    return {view: route.path, args: args};
  }

  static showSplashScreen(){
    const route = this.getRoutePathParam();
    return window.location.search.length == 0 && !this.hasPath(route.path);
  }




}
