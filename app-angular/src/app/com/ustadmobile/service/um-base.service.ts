import {UmContextWrapper} from './../util/UmContextWrapper';
import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class UmBaseService {

  private systemImpl: any;

  loadedLocale: boolean = false;

  private mObservable : Observable<any>;

  private context: UmContextWrapper;

  private mObserver;

  constructor(private http: HttpClient) {
     this.mObservable = new Observable<any>(observer => {
      this.mObserver = observer;
    });
  }

  getUmObservable(){
    return this.mObservable;
  }

  setImpl(systemImpl: any){
    this.systemImpl = systemImpl;
  }

  updateSectionTitle(title: string){
    this.mObserver.next(title)
  }

  setContext(context: UmContextWrapper){
    this.context = context;
  }

  loadLocaleStrings(locale: string){
    const localeUrl = "assets/locale/locale."+locale+".json";
    return new Observable(observer => {
      setTimeout(() => {
        if (!this.loadedLocale) {
          this.loadedLocale = true;
          this.http.get<Map<number, String>>(localeUrl).subscribe(strings => {
            this.systemImpl.setLocaleStrings(strings);
            observer.next(true);
          });
        }
      }, 300);
    });
  }
}
