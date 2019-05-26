import { UmContextWrapper } from './../util/UmContextWrapper';
import { UmWordLimitPipe } from './../util/pipes/um-word-limit.pipe';
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { kotlin } from 'kotlin';
@Injectable({
  providedIn: 'root'
})
export class UmBaseService {

  private systemImpl: any;

  private umContext: UmContextWrapper;

  constructor(private http: HttpClient) {}

  setImpl(systemImpl: any){
    this.systemImpl = systemImpl;
  }

  setContext(context: UmContextWrapper){
    this.umContext = context;
  }

  setCurrentLocale(locale: string){
    const localeUrl = "assets/locale/locale."+locale+".json";
    this.loadLocaleString(localeUrl);
  }


  private loadLocaleString(localeUrl: string){
    this.http.get<kotlin.collections.HashMap<Number, String>>(localeUrl).subscribe(response => {
      this.systemImpl.setLocaleStrings(response);
      console.log("string", this.systemImpl.getString(80, this.umContext)) 
    })}
}
