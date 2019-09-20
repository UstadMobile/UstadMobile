import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable, Subject, combineLatest} from 'rxjs';
import { MzToastService } from 'ngx-materialize';
import { map } from 'rxjs/operators';
import { UmAngularUtil } from '../util/UmAngularUtil';
import db from 'UstadMobile-lib-database';
import mpp from 'UstadMobile-lib-database-mpp';
import ents from 'UstadMobile-lib-database-entities'
import util from 'UstadMobile-lib-util';

@Injectable({
  providedIn: 'root'
})
export class UmBaseService {

  private database: db.com.ustadmobile.core.db.UmAppDatabase
  private environment: boolean = false
  private component: any
  public ROOT_UID = 1311236
  private umObserver = new Subject <any> ();
  private directionality: string;
  systemLocale: any;
  toolBarTitle: string = ".."
  private umListener = <Observable<any>>this.umObserver;
  public appName: string  = "..." 

  constructor(private http: HttpClient, private toastService: MzToastService) {
    UmAngularUtil.fireResouceReady(false); 
    this.loadAndSaveAppConfig(); 
  }

  /**
   * Set application environment
   * @param test true when is in test mode otherwise false
   */
  setEnvironment(test){
    this.environment = test
  }

  /**
   * Initialize application database
   */
  initDb(component: any){
    this.component = component
    mpp.com.ustadmobile.core.db.UmAppDatabase_JsImpl.Companion.register() 
    this.database =  db.com.ustadmobile.core.db.UmAppDatabase.Companion.getInstance(component.context)
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
    if(this.environment){
      //clear table
      this.http.get("http://localhost:8087/UmAppDatabase/clearAllTables", {responseType: 'text' })
      .subscribe(clearResponse =>{

        //get dummy data
        combineLatest([
          this.http.get<any>("assets/entries.json").pipe(map(entries => entries)),
          this.http.get<any>("assets/entries_parent_join.json").pipe(map(joins => joins))
        ]).subscribe(dataResponse => {
          //this.database.contentEntryDao.insertAsync(util.com.ustadmobile.lib.util.UMUtil.jsArrayToKotlinList(dataResponse[0])[0])
          console.log(this.database)
          return this.preloadSystemResources(fireWhenReady)
        })
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
  

  /**
   * Load appconfig properties files and store them using localstorage manager
   */
  loadAndSaveAppConfig(){
    this.http.get<Map < number, String >>("assets/appconfig.json")
    .pipe(map(strings => strings)).subscribe(configs => {
        Object.keys(configs).forEach(key => {
          localStorage.setItem(key, configs[key])  
        });
    });
  }

}
