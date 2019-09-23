import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable, Subject, combineLatest} from 'rxjs';
import { MzToastService } from 'ngx-materialize';
import { map } from 'rxjs/operators';
import { UmAngularUtil } from '../util/UmAngularUtil';
import db from 'UstadMobile-lib-database';
import mpp from 'UstadMobile-lib-database-mpp';
import core from 'UstadMobile-core'
import entities  from 'UstadMobile-lib-database-entities'
import kotlin from 'kotlin' 

@Injectable({
  providedIn: 'root'
})
export class UmBaseService {

  private database: db.com.ustadmobile.core.db.UmAppDatabase
  private isTestEnv: boolean = false
  private component: any
  public ROOT_UID = 1311236
  private umObserver = new Subject <any> ();
  private directionality: string;
  public continuation = kotlin.kotlin.coroutines.js.internal.EmptyContinuation
  systemLocale: any;
  toolBarTitle: string = ".."
  private umListener = <Observable<any>>this.umObserver;
  public appName: string  = "..." 

  constructor(private http: HttpClient, private toastService: MzToastService) {
    UmAngularUtil.fireResouceReady(false); 
     
  }

  init(component: any){
    this.component = component
    this.loadAppConfig().subscribe( configs => {
        Object.keys(configs).forEach(key => {
          localStorage.setItem(key, configs[key])  
        });
        mpp.com.ustadmobile.core.db.UmAppDatabase_JsImpl.Companion.register() 
        this.database =  db.com.ustadmobile.core.db.UmAppDatabase.Companion.getInstance(this.component.context)
    });
  }

  /**
   * Set application environment
   * @param isTestEnv true when is in test mode otherwise false
   */
  setEnvironment(isTestEnv = false){
    this.isTestEnv = isTestEnv
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
    if(this.isTestEnv == true){
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
        const account = {username: "UstadMobileUser", personUid: 1, auth:null,endpointUrl:localStorage["doordb.endpoint.url"]} 
        core.com.ustadmobile.core.impl.UmAccountManager.setActiveAccountWithContext(account, this.component.context)
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

  getToastService(){
    return this.toastService;
  }

  getUmObserver(): Observable < any > {
    return this.umListener;
  }

  getContextWrapper(){
    return this.component.context;
  }

  /**
   * Loading string map from json file
   * @param locale current system locale
   */
  loadStrings(locale: string){
    const localeUrl = "assets/locale/locale." + locale + ".json";
    return this.http.get<Map < number, String >>(localeUrl).pipe(map(strings => strings));
  }

  loadAppConfig(){
    return this.http.get<Map < number, String >>("assets/appconfig.json").pipe(map(strings => strings));
  }

}
