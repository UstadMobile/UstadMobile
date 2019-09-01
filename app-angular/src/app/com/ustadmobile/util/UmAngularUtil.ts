import ktorclientserial from 'ktor-ktor-client-serialization';
import core from 'UstadMobile-core';
import db from 'UstadMobile-lib-database';
import {Observable, Subscription} from 'rxjs';

export const appRountes = {
  "entryList":"ContentEntryList", "entryDetails":"ContentEntryDetail", "register":"RegisterAccount",
  "login":"Login", "xapi":"XapiPackage", "video":"VideoPlayer", "web":"webChunk", 
  "container":"Container", "reportDashboard":"ReportDashboard",
  "reportOptions":"ReportOptions","notFound":"NotFound", "treeView":"EntriesTreeDialog",
  "reportPreview":"ReportPreview"
}

export interface UmEvent extends Event{
  key: string;
  value: string;
}

export class UmAngularUtil {
  constructor(){
    const ktor = ktorclientserial 
  }


  static localStorageHandler = null;

  /**
   * Key to be used when toolbar title value changes
   */
  static DISPATCH_TITLE = "toolbar_title";

  /**
   * Key to be used to notify when resource state changes
   */
  static DISPATCH_RESOURCE = "resouce_ready";


  /**
   * Convert query parameters to a kotlin map to be used on presenters
   */
  static queryParamsToMap(queryParam ? : string, notFound? :boolean) {
    var paramString = queryParam || document.location.search + (notFound ? "":"&ref=null");
    return core.com.ustadmobile.core.util.UMFileUtil
      .parseURLQueryString(paramString);
  }

  /**
   * Get URL params to be passed to other views
   * @param route route to navifate to
   * @param entryId entry UID to open
   */
  static getRouteArgs(route, entryId){
    const args = (route != appRountes.entryList &&  route != appRountes.reportDashboard
       && route != appRountes.reportOptions && route != appRountes.treeView) 
     ? UmAngularUtil.queryParamsToMap("?") : UmAngularUtil.queryParamsToMap("?entryid=" + entryId+"&path=true");
    return args;
  }

  /**
   * Construct an object to be dispatched to another components
   * @param key object key to be dispatched
   * @param value object vakue to be dispatched
   */
  static getContentToDispatch(key, value){
    const content = {}
    content[key] = value;
    return content;
  }

  /**
   * Create observer instance to be used for data change observing
   * @param dataToObserve data to be observed
   */
  static createObserver(dataToObserve) {
    return new Observable(observer => {
      setTimeout(() => {
        observer.next(dataToObserve);
      }, 300);
    });
  }

  static registerResourceReadyListener(component) {
    var originalSetItem = localStorage.setItem;
    const resourceKey = this.DISPATCH_RESOURCE;
    const titleKey = this.DISPATCH_TITLE;
    localStorage.setItem = function (key, value) {
      const event = new Event('itemInserted');
      (<any>event).key = key;
      (<any>event).value = value; 
      document.dispatchEvent(event);
      originalSetItem.apply(this, arguments);
    };

    this.localStorageHandler = event => {
      if (event.key == titleKey) {
        component.setToolbarTitle(event.value)
      } else if (event.key == resourceKey) {
        component.onCreate() 
      } 
    };

    document.addEventListener("itemInserted", this.localStorageHandler, false);
  }

  static removeResourceReadyListener(){
    this.localStorageHandler = null;
  }

  static fireResouceReady(ready: boolean){
    localStorage.setItem(this.DISPATCH_RESOURCE,ready+"")
  }

  static getGoogleChartFormattedData(dataList: any[], component): any{
    const dataGroups = [];
    const plotAgainst = []
    const columnNames = ['Data']
    dataList.forEach(data => {
      if(dataGroups.indexOf(data.xAxis) == -1){
        dataGroups.push(data.xAxis)
      }

      if(plotAgainst.indexOf(data.subgroup) == -1){
        plotAgainst.push(data.subgroup)
        const columnName = component.systemImpl.getString(data.subgroup, component.context)
        columnNames.push(columnName)
        
      }
    });
    const graphData = [];
    dataGroups.forEach(group => {
      const column = [group]
      this.getElementsFromObject(dataList,"xAxis",group).forEach(found => {
        column.push(found.yAxis)
      })
      graphData.push(column)
    })
  
    const columns = this.maxColumsNumber(graphData);
    const formattedDataList = []
    graphData.forEach(graph => {
      const fill = this.getDataToFill(columns - graph.length);
      formattedDataList.push(graph.concat(fill))
    })
    return {data: formattedDataList,columns: columnNames};
  }

  static getDataToFill(maxSize: number){
    const fillData = []
    if(maxSize != 0){
      for(let i = 0; i < maxSize; i++){
        fillData.push(0)
      }
    }
    return fillData;
  }

  static maxColumsNumber(dataList: any[]){
    let dataListLength = 0;
    dataList.forEach(data => {
      if(data.length > dataListLength){
        dataListLength = data.length
      }
    })
    return dataListLength
  }

  /**
   * Register resource observer - make sure everything is ready before starting application logical flow.
   * This applies to both normal navigation to url copy & paste
   * @param component current component to be observerd
   */
  static registerUmObserver(component: any) : Subscription{

    const subscription = component.umService.getUmObserver().subscribe(content => {
      if (content[UmAngularUtil.DISPATCH_RESOURCE] && component.systemImpl.stringMap) {
        component.onCreate()
      }

      if(content[UmAngularUtil.DISPATCH_TITLE]) {
        component.toolBarTitle = content[UmAngularUtil.DISPATCH_TITLE];
      }
    });
    if(component.MessageID  && component.systemImpl.stringMap){
      component.onCreate()
    }
    
    return subscription
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

  /**
   * Get route path
   */
  private static getRoutePathParam(){
    var routePath = document.location.pathname;
    const mPaths = routePath.split("/");
    return {completePath: routePath, path: mPaths[mPaths.length - 1], size:  mPaths.length};
  }

  /**
   * Check if URl has a pecific path
   * @param mPath  path to be checked
   */
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

  static getElementFromObject(object: any[], key: string, value: string){
    var foundElement = {}
    object.forEach(element => {
      if(element[key] == value){
        foundElement = element;
        return;
      }
    });
    return foundElement;
  }

  static getElementsFromObject(object: any[], key: string, value: string){
    var foundElements = []
    object.forEach(element => {
      if(element[key] == value){
        foundElements.push(element)
      }
    });
    return foundElements;
  }

  /**
   * Get the first route to be directed when application starts
   * @param entryUid root entry
   */
  static getInitialRoute(entryUid ? : number) {
    var args, view = null
    const mPath = this.getRoutePathParam();
    if(mPath.size >= 4){
      //redirect as it is
      view = this.hasPath(mPath.path) ? mPath.path : appRountes.notFound+"/"
      args = UmAngularUtil.queryParamsToMap();
    }else if(mPath.size <= 3){
      //redirect to default
      view = this.hasPath(mPath.path) ? mPath.path + "/" : appRountes.entryList
      args = !this.hasPath(mPath.path) ? UmAngularUtil.queryParamsToMap("?entryid=" + entryUid) 
      : UmAngularUtil.queryParamsToMap(document.location.search.length > 0 ? document.location.search: "?")
    }
    return {view: view, args: args};
  }

  /**
   * Get language specific route
   * @param languageCode lamguage standard code
   */
  static getDifferentLanguageRoute(languageCode){
    var route = this.getRoutePathParam();
    const args = UmAngularUtil.queryParamsToMap();
    return {view: route.path, args: args};
  }

  /**
   * Show splash screen when opening the application
   */
  static showSplashScreen(){
    const route = this.getRoutePathParam();
    return window.location.search.length == 0 && !this.hasPath(route.path);
  }
}
