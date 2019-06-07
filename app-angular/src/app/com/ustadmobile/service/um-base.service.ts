import {UmContextWrapper} from './../util/UmContextWrapper';
import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable, Subject} from 'rxjs';
import { MzToastService } from 'ngx-materialize';

@Injectable({
  providedIn: 'root'
})
export class UmBaseService {

  private systemImpl: any;
  loadedLocale: boolean = false;
  loadedLanguages: boolean = false;
  private context: UmContextWrapper;
  private umObserver = new Subject < any > ();
  private presenter;
  private directionality: string;
  private supportedLanguages  = []

  constructor(private http: HttpClient, private toastService: MzToastService) {}

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
   * Get list of all supported languages
   */
  getSupportedLanguages(){
    return this.supportedLanguages;
  }

  /**
   * Dispatch update to the other part of the app (other components)
   * @param content content to be passed to the observer
   */
  dispatchUpdate(content: any) {
    this.umObserver.next(content);
  }

  /**
   * Set current presenter
   * @param presenter current presenter
   */
  setPresenterInstance(presenter){
    this.presenter = presenter;
  }

  goBack(){
    this.presenter.handleUpNavigation();
  }

  getToastService(){
    return this.toastService;
  }

  getUmObserver(): Observable < any > {
    return this.umObserver.asObservable();
  }

  setImpl(systemImpl: any) {
    this.systemImpl = systemImpl;
  }

  setContext(context: UmContextWrapper) {
    this.context = context;
  }

  /**
   * Loading string map from json file
   * @param locale current system locale
   */
  loadLocaleStrings(locale: string) {
    const localeUrl = "assets/locale/locale." + locale + ".json";
    return new Observable(observer => {
      setTimeout(() => {
        if (!this.loadedLocale) {
          this.loadedLocale = true;
          this.http.get < Map < number, String >> (localeUrl).subscribe(strings => {
            this.systemImpl.setLocaleStrings(strings);
            observer.next(true);
          });
        }
      }, 300);
    });
  }

  /**
   * Loading all supported languages from the language json file.
   */
  loadSupportedLanguages(){
    const languageUrl = "assets/languages.json";
    return new Observable(observer => {
      if(!this.loadedLanguages){
        this.loadedLanguages = true;
        this.http.get < Map < number, String >> (languageUrl).subscribe(languages => {
          Object.keys(languages).forEach(key => {
            const language = {code: key, name: languages[key]};
            this.supportedLanguages.push(language);
          });
          observer.next("ready");
        });      
      }
    });
  }
}
