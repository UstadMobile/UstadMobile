import {UmContextWrapper} from './../util/UmContextWrapper';
import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {kotlin} from 'kotlin';
import {Observable} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class UmBaseService {

  private systemImpl: any;

  loadedLocale: boolean = false;

  private context: UmContextWrapper;

  constructor(private http: HttpClient) {}

  setImpl(systemImpl: any){
    this.systemImpl = systemImpl;
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
          this.http.get<kotlin.collections.HashMap<number, String>>(localeUrl).subscribe(strings => {
            this.systemImpl.setLocaleStrings(strings);
            observer.next(true);
          });
        }
      }, 300);
    });
  }
}
