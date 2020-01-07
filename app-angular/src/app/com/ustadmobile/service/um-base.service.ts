import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Subject, combineLatest} from 'rxjs';
import { MzToastService } from 'ngx-materialize';
import { map } from 'rxjs/operators';
import { UmAngularUtil } from '../util/UmAngularUtil';
import db from 'UstadMobile-lib-database';
import mpp from 'UstadMobile-lib-database-mpp';
import core from 'UstadMobile-core'
import kotlin from 'kotlin' 

@Injectable({
  providedIn: 'root'
})
export class UmBaseService {

  private database: db.com.ustadmobile.core.db.UmAppDatabase
  private component: any
  public ROOT_UID = "0"
  private umObserver = new Subject <any> ();
  private directionality: string;
  public continuation = kotlin.kotlin.coroutines.js.internal.EmptyContinuation
  systemLocale: any;
  httpClient: HttpClient;
  toolBarTitle: string = ".."
  public appName: string  = "..." 
  public localeTag: string = "tracked.locale"

  isMobile = false
  isAndroidDevice = false

  constructor(private http: HttpClient, private toastService: MzToastService) { 
    this.ROOT_UID = core.com.ustadmobile.core.controller.HomePresenter.Companion.MASTER_SERVER_ROOT_ENTRY_UID.toString()
    UmAngularUtil.fireResouceReady(false); 
    this.httpClient = http 
    this.isMobile = UmAngularUtil.isSupportedEnvironment("mobile")
    this.isAndroidDevice = UmAngularUtil.isSupportedEnvironment("android")
  }
  
  /**
   * Initialize base service
   * @param component current opened component
   */
  init(component: any){
    this.component = component
    this.loadAppConfig().subscribe( configs => {
        Object.keys(configs).forEach(key => {
          UmAngularUtil.setItem(key, configs[key])  
        });
        mpp.com.ustadmobile.core.db.UmAppDatabase_JsImpl.Companion.register() 
        this.database =  db.com.ustadmobile.core.db.UmAppDatabase.Companion.getInstance(this.component.context)
        this.preloadResourcess(true) 
    });
  }

  /**
   * Get context instance
   */
  getContext(){
    return this.component.context;
  }

  /**
   * Get database instance
   */
  getDbInstance(){
    return this.database
  }

  /**
   * Preload system string and database resources
   * @param fireWhenReady fire when true otherwise don't fire any event
   */
  preloadResourcess(fireWhenReady = true){
    const endPointUrl = UmAngularUtil.getItem(UmAngularUtil.BASE_URL_TAG);
    if(endPointUrl == UmAngularUtil.TEST_ENDPOINT){
      UmAngularUtil.setItem(UmAngularUtil.API_URL_TAG, endPointUrl)
    }
    UmAngularUtil.setItem(UmAngularUtil.CONTENT_URL_TAG, endPointUrl+"ContainerMount/")
    this.preloadSystemResources(fireWhenReady)
  }

  private preloadSystemResources(fireWhenReady = true){
    const loader = combineLatest([this.loadStrings(this.systemLocale)]);
    if(fireWhenReady == true){
      loader.subscribe(resources => {
        this.component.systemImpl.setLocaleStrings(resources[0])
        UmAngularUtil.fireResouceReady(true)
      })
    }
    return loader
  }
  
  /**
   * Set current system language directionality
   * @param directionality current system language directionality
   */
  setSystemDirectionality(directionality){
    this.directionality = directionality;
  }

  /**
   * Get current system locale
   * @param locale current system locale
   */
  setSystemLocale(locale){
    this.systemLocale = locale
  }

  /**
   * Check if syatem language directionality is LTR
   */
  isLTRDirectionality() : boolean{
    return this.directionality == "ltr";
  }

  /**
   * Dispatch update to the other part of the app (other components)
   * @param content content to be passed to the observer
   */
  dispatchUpdate(content: any) {
    this.umObserver.next(content);
  }

  /**
   * Get toast service for showing feedback
   */
  getToastService(){
    return this.toastService;
  }


  /**
   * Loading string map from json file
   * @param locale current system locale
   */
  loadStrings(locale: string){
    const localeUrl = "assets/locale/locale." + locale + ".json";
    return this.http.get<Map < number, String >>(localeUrl).pipe(map(strings => strings));
  }

  /**
   * Load app config content
   */
  loadAppConfig(){
    return this.http.get<Map < number, String >>("assets/appconfig.json").pipe(map(strings => strings));
  }

}
