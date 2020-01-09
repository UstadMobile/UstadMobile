import core from 'UstadMobile-core';
import {Observable} from 'rxjs';
import util from 'UstadMobile-lib-util';

/**
 * All app routes
 */
export const appRountes = {
  "entryList":"ContentEntryList", "entryDetails":"ContentEntryDetail", "register":"RegisterAccount",
  "login":"Login", "epub":"EpubContent", "video":"VideoPlayer", "web":"webChunk", "reportDashboard":"ReportDashboard",
  "reportOptions":"ReportOptions","notFound":"NotFound", "treeView":"EntriesTreeDialog",
  "reportPreview":"ReportPreview", "profile":"UserProfile", "home":"Home"
}

/**
 * Custom event used for inter-components communication
 */
export interface UmEvent extends Event{
  key: string;
  value: string;
}

/**
 * Util class contains all utility methods
 */
export class UmAngularUtil {
  constructor(){
  }

  public static ARG_CONTENT_ENTRY_UID = core.com.ustadmobile.core.view.UstadView.Companion.ARG_CONTENT_ENTRY_UID.toString()

  private static storageEventHandler: any = {};

  public static CONTENT_URL_TAG = "contentUrl"

  public static BASE_URL_TAG = "doordb.endpoint.url"

  public static TEST_ENDPOINT = "http://localhost:8087/"

  /**
   * Key to be used when toolbar title value changes
   */
  static DISPATCH_TITLE = "toolbar_title";

  /**
   * Key to be used to notify when resource state changes
   */
  static DISPATCH_RESOURCE = "resouce_ready";

  /**
   * Key to be used to notify when there is data change
   */
  static DISPATCH_DATA_CHANGE = "data_change"


  /**
   * Convert query parameters to a kotlin map to be used on presenters
   */
  static getArgumentsFromQueryParams(args: any = {}) {
    const route = args.route ? args.route : this.getRoutePathParam().path
    const params = args.params ? args.params : null
    const search = this.removeParam(this.isWithoutEntryUid(route) 
    ? this.ARG_CONTENT_ENTRY_UID:"", (params ? params : this.getRoutePathParam().search))
    let paramString = search + (search.includes("ref") ? "":((search.length > 0 ? "&ref=null&libraries=null":"?ref=null"))) 
    return core.com.ustadmobile.core.util.UMFileUtil
      .parseURLQueryString(paramString); 
  }


  private static removeParam(key, sourceURL) {
    var rtn = "",param,params_arr = [],
        queryString = (sourceURL.indexOf("?") !== -1) ? sourceURL.split("?")[1] : "";
    if (queryString !== "") {
        params_arr = queryString.split("&");
        for (var i = params_arr.length - 1; i >= 0; i -= 1) {
            param = params_arr[i].split("=")[0];
            if (param === key) {
                params_arr.splice(i, 1);
            }
        }
        rtn = rtn + (params_arr.length > 0 ? "?" + params_arr.join("&"):"")
    }
    return rtn;
}


  private static isWithoutEntryUid(viewName: String): boolean{
    return  viewName.includes("Report") || viewName.includes("Register") ||
            viewName.includes("Login") || viewName.includes("Profile")
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

  /**
   * Create custom event to be fired within the app
   * @param eventType type of event to be fired 
   * @param component active component
   */
  private static createEvent(eventType: any, component){
    const resourceKey = this.DISPATCH_RESOURCE;
    const titleKey = this.DISPATCH_TITLE;
    const dataKey = this.DISPATCH_DATA_CHANGE;
    this.storageEventHandler[eventType] = function(event) {
      if (event.key == resourceKey) {
        document.removeEventListener(eventType,UmAngularUtil.storageEventHandler[eventType])
        UmAngularUtil.storageEventHandler[eventType] = null;
        component.onCreate()
      }else if(event.key == titleKey){
        component.setToolbarTitle(event.value)
      }else if(event.key == dataKey){ 
        component.onDataChange(event.value)
      }
    };
  }

  /**
   * register resource ready listener
   * @param component current opened component
   */
  static registerResourceReadyListener(component) {
    this.createEvent(this.DISPATCH_RESOURCE, component)
    document.addEventListener(this.DISPATCH_RESOURCE, this.storageEventHandler[this.DISPATCH_RESOURCE], false); 
    if(component.systemImpl.getString(component.MessageID.app_name, component.context) != ''){
      this.fireResouceReady(true)
    }
  }

  /**
   * Register title change listener
   * @param component current opened component
   */
  static registerTitleChangeListener(component) {
    this.createEvent(this.DISPATCH_TITLE, component)
    document.addEventListener(this.DISPATCH_TITLE, this.storageEventHandler[this.DISPATCH_TITLE], false); 
  }

  /**
   * Register data change listener
   * @param component current opened component
   */
  static registerDataChangeListener(component){
    this.createEvent(this.DISPATCH_DATA_CHANGE, component)
    document.addEventListener(this.DISPATCH_DATA_CHANGE, this.storageEventHandler[this.DISPATCH_DATA_CHANGE], false); 
  }

  /**
   * Dispatch update when resources are ready, this will load the application
   * @param ready indicated if resources are ready or not.
   */
  static fireResouceReady(ready: boolean){
    this.setItem(this.DISPATCH_RESOURCE, ready+"")
  }

  /**
   * Disptach update when data set changes
   * @param data data to be associated with the event
   */
  static fireOnDataChanged(data){
    this.setItem(this.DISPATCH_DATA_CHANGE,data);
  }

  /**
   * Set preference item, extension function to localstorage setItem
   * @param itemKey item key to be used
   * @param itemValue item value to be set
   */
  static setItem(itemKey, itemValue){
    const resourceKey = this.DISPATCH_RESOURCE;
    const titleKey = this.DISPATCH_TITLE;
    const dataKey = this.DISPATCH_DATA_CHANGE;

    localStorage.setItem(itemKey, itemValue)
    if(itemKey == resourceKey || itemKey == titleKey || itemKey == dataKey){
        const event = new Event(itemKey);
        (<any>event).key = itemKey;
        (<any>event).value = itemValue; 
        document.dispatchEvent(event);
      }
  }

  /**
   * Get item from localstorage
   * @param itemKey item key to be used
   */
  static getItem(itemKey): string{
    return localStorage.getItem(itemKey)
  }

  /**
   * Remove an item from the localstorage
   * @param itemkey item key to be used
   */
  static removeItem(itemkey){
    localStorage.removeItem(itemkey)
  }

  /**
   * Dispatch update when title changes
   * @param title new title
   */
  static fireTitleUpdate(title: string){
     this.setItem(this.DISPATCH_TITLE,title)
  }

  /**
   * Structure data received to be used for GOOGLE charts
   * @param dataList data list as recevived from kotlin
   * @param component current component context
   */
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


  private static getDataToFill(maxSize: number){
    const fillData = []
    if(maxSize != 0){
      for(let i = 0; i < maxSize; i++){
        fillData.push(0)
      }
    }
    return fillData;
  }

  private static maxColumsNumber(dataList: any[]){
    let dataListLength = 0;
    dataList.forEach(data => {
      if(data.length > dataListLength){
        dataListLength = data.length
      }
    })
    return dataListLength
  }

  /**
   * Decide which menu to be highlited based on current opened section
   */
  static getActiveMenu(): boolean[]{
    const reportActive = this.getRoutePathParam().path.includes("Report")
    return [!reportActive, reportActive]
  }

  /**
   * Check current running platform enviroment 
   * @param keyToCheck platform to be checked from
   */
  static isSupportedEnvironment(keyToCheck = "android"){
    return navigator.appVersion.toLowerCase().includes(keyToCheck)
  }

  /**
   * Get route path
   */
  static getRoutePathParam(){
    const docLoc = window.location
    const urlProps =  docLoc.hash.replace("#","")
    let paths = docLoc.pathname.split("/").filter(function (el) {return el != "";})
    paths = paths.slice(0, paths.length - 1);
    const routePath = urlProps.substring(0,urlProps.indexOf("?"))
    const searchParams =  urlProps.indexOf("?") == -1 ? "": urlProps.substring(urlProps.indexOf("?"))
    const mPaths = routePath.split("/");
    return {origin: docLoc.origin + "/" + paths.join("/") ,completePath: routePath, path: mPaths[mPaths.length - 1], size:  mPaths.length, search: searchParams};
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

  /**
   * Get element from object
   * @param object Object to be used
   * @param key object key
   * @param value value to be checked
   */
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

  private static getElementsFromObject(object: any[], key: string, value: string){
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
  static getInitialRoute(entryUid) {
    var args, view = null
    const mPath = this.getRoutePathParam();
    if(mPath.completePath.includes(appRountes.home)){
      view = mPath.path
      args = UmAngularUtil.getArgumentsFromQueryParams({params: this.getRoutePathParam().search})
    }else{
      view = mPath.path.includes(appRountes.notFound) ? appRountes.notFound+"/": appRountes.entryList
      args = mPath.path.includes(appRountes.notFound) ? UmAngularUtil.getArgumentsFromQueryParams()
      :UmAngularUtil.getArgumentsFromQueryParams({params:
         "?"+this.ARG_CONTENT_ENTRY_UID+"=" + entryUid, route: view});
    }
    return {view: view, args: args};
  }


  /**
   * Get language specific route
   * @param languageCode lamguage standard code
   */
  static getDifferentLanguageRoute(){
    var route = this.getRoutePathParam();
    const args = UmAngularUtil.getArgumentsFromQueryParams();
    return {view: route.path, args: args};
  }

  /**
   * Show splash screen when opening the application
   */
  static showSplashScreen(){
    const route = this.getRoutePathParam().path;
    return this.getRoutePathParam().search.length == 0 && !this.hasPath(route);
  }

  /**
   * Convert JS array to Kotlin list
   * @param data array to be converted
   */
  static jsArrayToKotlinList(data: any){
    return util.com.ustadmobile.lib.util.UMUtil.jsArrayToKotlinList(data);
  }

  /**
   * Convert kotlin list to JS array
   * @param data data list to be converted
   */
  static kotlinListToJsArray(data: any): any[]{
    return util.com.ustadmobile.lib.util.UMUtil.kotlinListToJsArray(data);
  }

  /**
   * Covert kotlin map to JS array
   * @param data map to be converted
   */
  static kotlinMapToJsArray(data: any): any[]{
    return util.com.ustadmobile.lib.util.UMUtil.kotlinMapToJsArray(data);
  }

  /**
   * Construct path where containr will be mounted.
   * @param containerUid conter id to be used
   */
  static getMountPath(containerUid: any){
    const uid = containerUid.toString()
    return UmAngularUtil.getItem(this.CONTENT_URL_TAG) + uid +"/"
  }

  /**
   * Conevert category map from kotlin to JS array
   * @param data category map to be converted
   */
  static kotlinCategoryMapToJSArray(data: any): any[]{
    return util.com.ustadmobile.lib.util.UMUtil.kotlinCategoryMapToJsArray(data);
  }

  /**
   * Sometimes with larger number of list items, list tends to be pushed up like 4 items.
   *  This will always scroll back the list to item 0
   */
  static scrollToTop(){
    window.scrollTo(0, 0);
  }
}
