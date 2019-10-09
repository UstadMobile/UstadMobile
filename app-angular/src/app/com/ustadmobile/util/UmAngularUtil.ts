import core from 'UstadMobile-core';
import {Observable} from 'rxjs';
import util from 'UstadMobile-lib-util';

export const appRountes = {
  "entryList":"ContentEntryList", "entryDetails":"ContentEntryDetail", "register":"RegisterAccount",
  "login":"Login", "epub":"EpubContent", "video":"VideoPlayer", "web":"webChunk", "reportDashboard":"ReportDashboard",
  "reportOptions":"ReportOptions","notFound":"NotFound", "treeView":"EntriesTreeDialog",
  "reportPreview":"ReportPreview", "profile":"UserProfile", "home":"Home"
}

export interface UmEvent extends Event{
  key: string;
  value: string;
}

export class UmAngularUtil {
  constructor(){
  }


  private static localStorageHandler: any= {};

  /**
   * Key to be used when toolbar title value changes
   */
  static DISPATCH_TITLE = "toolbar_title";

  /**
   * Key to be used to notify when resource state changes
   */
  static DISPATCH_RESOURCE = "resouce_ready";

  static DISPATCH_DATA_CHANGE = "data_change"


  /**
   * Convert query parameters to a kotlin map to be used on presenters
   */
  static getArgumentsFromQueryParams(args: any = {}) {
    const route = args.route ? args.route : this.getRoutePathParam().path
    const params = args.params ? args.params : null
    const search = this.removeParam(this.isWithoutEntryUid(route) ? "entryid":"", (params ? params : document.location.search))
    let paramString = search + (search.includes("ref") ? "":((search.length > 0 ? "&ref=null":"?ref=null"))) 
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


  private static createEvent(eventType: any, component){

    var originalSetItem = localStorage.setItem;
    const resourceKey = this.DISPATCH_RESOURCE;
    const titleKey = this.DISPATCH_TITLE;
    const dataKey = this.DISPATCH_DATA_CHANGE;
    localStorage.setItem = function (key, value) {
      if(key == resourceKey || key == titleKey || key == dataKey){
        const event = new Event(key);
        (<any>event).key = key;
        (<any>event).value = value; 
        document.dispatchEvent(event);
      }
      originalSetItem.apply(this, arguments);
    };

    this.localStorageHandler[eventType] = event => {
      if (event.key == resourceKey) {
        document.removeEventListener(eventType,this.localStorageHandler[eventType])
        this.localStorageHandler[eventType] = null;
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
    document.addEventListener(this.DISPATCH_RESOURCE, this.localStorageHandler[this.DISPATCH_RESOURCE], false); 
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
    document.addEventListener(this.DISPATCH_TITLE, this.localStorageHandler[this.DISPATCH_TITLE], false); 
  }

  /**
   * Register data change listener
   * @param component current opened component
   */
  static registerDataChangeListener(component){
    this.createEvent(this.DISPATCH_DATA_CHANGE, component)
    document.addEventListener(this.DISPATCH_DATA_CHANGE, this.localStorageHandler[this.DISPATCH_DATA_CHANGE], false); 
  }

  /**
   * Dispatch update when resources are ready, this will load the application
   * @param ready indicated if resources are ready or not.
   */
  static fireResouceReady(ready: boolean){
    localStorage.setItem(this.DISPATCH_RESOURCE,ready+"")
  }

  /**
   * Disptach update when data set changes
   * @param data data to be associated with the event
   */
  static fireOnDataChanged(data){
    localStorage.setItem(this.DISPATCH_DATA_CHANGE,data);
  }

  /**
   * Dispatch update when title changes
   * @param title new title
   */
  static fireTitleUpdate(title: string){
     localStorage.setItem(this.DISPATCH_TITLE,title)
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
   * @param routes route map
   */
  static getActiveMenu(routes): boolean[]{
    const reportActive = window.location.pathname.includes("Report")
    return [!reportActive, reportActive]
  }

  static isSupportedEnvironment(keyToCheck = "android"){
    return navigator.appVersion.toLowerCase().includes(keyToCheck)
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
    console.log(mPath, mPath.completePath.includes(appRountes.home))
    if(mPath.completePath.includes(appRountes.home)){
      view = mPath.path
      args = UmAngularUtil.getArgumentsFromQueryParams({params: document.location.search})
    }else{
      view = mPath.path.includes(appRountes.notFound) ? appRountes.notFound+"/": appRountes.entryList
      args = mPath.path.includes(appRountes.notFound) ? UmAngularUtil.getArgumentsFromQueryParams()
      :UmAngularUtil.getArgumentsFromQueryParams({params: "?entryid=" + entryUid, route: view});
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
    const route = this.getRoutePathParam();
    return window.location.search.length == 0 && !this.hasPath(route.path);
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

  static getMountPath(containerUid: any){
    const uid = containerUid.toString()
    return localStorage.getItem("contentUrl") + uid +"/"
  }

  /**
   * Conevert category map from kotlin to JS array
   * @param data category map to be converted
   */
  static kotlinCategoryMapToJSArray(data: any): any[]{
    return util.com.ustadmobile.lib.util.UMUtil.kotlinCategoryMapToJsArray(data);
  }
}
