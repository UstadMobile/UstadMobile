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
  private context: UmContextWrapper;
  private umObserver = new Subject < any > ();
  private presenter;


  constructor(private http: HttpClient, private toastService: MzToastService) {}

  dispatchUpdate(content: any) {
    this.umObserver.next(content);
  }

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
}
