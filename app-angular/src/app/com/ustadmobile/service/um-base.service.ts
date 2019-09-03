import {UmContextWrapper} from './../util/UmContextWrapper';
import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable, Subject} from 'rxjs';
import { MzToastService } from 'ngx-materialize';
import { map } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class UmBaseService {

  private umObserver = new Subject < any > ();
  private directionality: string;

  constructor(private http: HttpClient, private toastService: MzToastService) {
    this.loadAndSaveAppConfig();
  }

  /**
   * Set current system language directionality
   * @param directionality current system language directionality
   */
  setSystemDirectionality(directionality){
    this.directionality = directionality;
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
    return this.umObserver.asObservable();
  }

  setContext(context: UmContextWrapper) {
  }

  /**
   * Loading string map from json file
   * @param locale current system locale
   */
  loadStrings(locale: string){
    const localeUrl = "assets/locale/locale." + locale + ".json";
    console.log("LOcale",locale)
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


  /**
   * Load entries
   */
  loadEntries(){
    const entriesUrl = "assets/entries.json";
    return this.http.get<any>(entriesUrl).pipe(map(entries => entries));
  }

  /**
   * Load content entry parent child joins
   */
  loadEntryJoins(){
    const joinUrl = "assets/entries_parent_join.json";
    return this.http.get<any>(joinUrl).pipe(map(joins => joins));
  }

}
