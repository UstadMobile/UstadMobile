import { Component, OnInit, NgZone } from '@angular/core';
import core from 'UstadMobile-core'
import { Subscription } from 'rxjs';
import { UmBaseService } from '../../service/um-base.service';
import { Router, ActivatedRoute, NavigationEnd } from '@angular/router';
import { UmAngularUtil } from '../../util/UmAngularUtil';
import { UmBaseComponent } from '../um-base-component';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';

@Component({
  selector: 'app-user-profile',
  templateUrl: './user-profile.component.html',
  styleUrls: ['./user-profile.component.css']
})
export class UserProfileComponent extends UmBaseComponent implements core.com.ustadmobile.core.view.UserProfileView {

  private presenter: core.com.ustadmobile.core.controller.UserProfilePresenter
  private navigationSubscription: Subscription;
  userProfile: string = ""
  umFormLanguage: FormGroup;
  supportedLanguages = []

  loggedPerson

  constructor(umservice: UmBaseService, router: Router, route: ActivatedRoute, private zone:NgZone,  formBuilder: FormBuilder) { 
    super(umservice,router, route)

    //Listen for the navigation changes - changes on url
    this.navigationSubscription = this.router.events.filter(event => event instanceof NavigationEnd)
      .subscribe(_ => {
        UmAngularUtil.registerResourceReadyListener(this)
      });

      const currentLocale = this.systemImpl.getAllUiLanguage(this.context)[core.com.ustadmobile.core.impl.UstadMobileSystemCommon.PREFKEY_LOCALE]
      this.umFormLanguage = formBuilder.group({
        'language': [currentLocale, Validators.required]
      }); 
  }

  ngOnInit() {
    super.ngOnInit()

    this.umFormLanguage.valueChanges.subscribe((form: any) => {
      if (form.language !== "") {
        this.presenter.handleLanguageSelected(this.supportedLanguages.indexOf(form.language)) 
      }
    });
  }

  onCreate(){
    super.onCreate()
    this.presenter = new core.com.ustadmobile.core.controller.UserProfilePresenter(
      this.context, UmAngularUtil.getArgumentsFromQueryParams(), this,
      this.umService.getDbInstance().personDao,this.systemImpl)
    this.presenter.onCreate(null)
    this.presenter.handleShowLanguageOptions()
  }

  setCurrentLanguage(language){
    console.log(language)
  }

  setLanguageOption(languageOptions){
    this.zone.run(()=>{
      this.supportedLanguages = UmAngularUtil.kotlinListToJsArray(languageOptions)
    })
  }

  setLoggedPerson(person){
    this.loggedPerson = person
    this.zone.run(()=>{
      super.setToolbarTitle(person.username)
    })
  }

  loadProfileIcon(profile){
    if (profile != "") {
      this.userProfile = profile
    } else {
      this.userProfile = window.location.origin + "/assets/images/guest_user_icon.png"
    }
  }

  restartUI(){
    UmAngularUtil.kotlinMapToJsArray(this.systemImpl.getAllUiLanguage(this.context)).forEach(language =>{
      if(language.value == this.umFormLanguage.value.language){
        window.open(window.location.origin + "/" + language.key + "/", "_self")
      }
    })
  }

  logoutUser(){
    this.presenter.handleUserLogout()
  }

  showLanguageOptions(){}

  ngOnDestroy(): void {
    super.ngOnDestroy();
    if (this.presenter) {
      this.presenter.onDestroy();
    }
    if (this.navigationSubscription) {
      this.navigationSubscription.unsubscribe();
    }
  }


}
