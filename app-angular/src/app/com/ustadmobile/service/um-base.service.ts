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
        console.log(this.database)
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
  preloadResources(fireWhenReady = true){

    UmAngularUtil.setItem(UmAngularUtil.CONTENT_URL_TAG, UmAngularUtil.getItem(UmAngularUtil.BASE_URL_TAG)+"ContainerMount/")

    if(UmAngularUtil.TEST_ENDPOINT == UmAngularUtil.getItem(UmAngularUtil.BASE_URL_TAG)){
      combineLatest([
        this.http.get("http://localhost:8087/UmAppDatabase/clearAllTables", {responseType: 'text' }),
        this.http.get<any>("assets/data_entries.json").pipe(map(res => res)),
        this.http.get<any>("assets/data_entries_parent_join.json").pipe(map(res => res)),
        this.http.get<any>("assets/data_languages.json").pipe(map(res => res)),
        this.http.get<any>("assets/data_persons.json").pipe(map(res => res)),
        this.http.get<any>("assets/data_statements.json").pipe(map(res => res)),
        this.http.get<any>("assets/data_xlangmap.json").pipe(map(res => res)),
        this.http.get<any>("assets/data_verbs.json").pipe(map(res => res)),
      ]).subscribe(dataResponse => { 
        const account = {username: "UstadMobileUser", personUid: 1, auth:null,endpointUrl: UmAngularUtil.getItem("doordb.endpoint.url")} 
        core.com.ustadmobile.core.impl.UmAccountManager.setActiveAccountWithContext(account, this.component.context)
        const containerList = [{containerUid: 909090, fileSize: 90909000, mobileOptimized: true}]
        this.database.containerDao.insertAsync(UmAngularUtil.jsArrayToKotlinList(containerList), this.continuation)
        console.log(this.database.containerDao)
        this.database.contentEntryDao.insertListAsync(UmAngularUtil.jsArrayToKotlinList(dataResponse[1]), this.continuation)

        this.database.contentEntryParentChildJoinDao.insertListAsync(UmAngularUtil.jsArrayToKotlinList(dataResponse[2]), this.continuation)

        this.database.languageDao.insertListAsync(UmAngularUtil.jsArrayToKotlinList(dataResponse[3]), this.continuation)

        this.database.personDao.insertListAsync(UmAngularUtil.jsArrayToKotlinList(dataResponse[4]), this.continuation)
        
        this.database.statementDao.insertListAsync(UmAngularUtil.jsArrayToKotlinList(dataResponse[5]), this.continuation)
        
        this.database.xLangMapEntryDao.insertListAsync(UmAngularUtil.jsArrayToKotlinList(dataResponse[6]), this.continuation)

        this.database.verbDao.insertListAsync(UmAngularUtil.jsArrayToKotlinList(dataResponse[7]), this.continuation)

        return this.preloadSystemResources(fireWhenReady)
      })
    }else{
      UmAngularUtil.removeItem("umaccount.personid")
      return  this.preloadSystemResources(fireWhenReady)
    }
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
