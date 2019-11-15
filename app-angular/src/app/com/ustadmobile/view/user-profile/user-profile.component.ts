import { Component, OnInit, NgZone, OnDestroy } from '@angular/core';
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
export class UserProfileComponent extends UmBaseComponent implements OnDestroy,
 core.com.ustadmobile.core.view.UserProfileView {

  private presenter: core.com.ustadmobile.core.controller.UserProfilePresenter
  private navigationSubscription: Subscription;
  umFormLanguage: FormGroup;
  supportedLanguages = []
  class_profile_holder: string;
  class_details_holder: string;
  class_logout_button: string;

  loggedPerson

  constructor(umservice: UmBaseService, router: Router, route: ActivatedRoute, 
    private zone:NgZone,  formBuilder: FormBuilder) { 
    super(umservice,router, route)

    //Listen for the navigation changes - changes on url
    this.navigationSubscription = this.router.events.filter(event => event instanceof NavigationEnd)
      .subscribe(_ => {
        UmAngularUtil.registerResourceReadyListener(this)
      });

      this.class_logout_button = this.umService.isLTRDirectionality() ? 'right':'left'
      this.class_profile_holder = this.umService.isLTRDirectionality() ? 'left':'right'
      this.class_details_holder = this.umService.isLTRDirectionality() ? 'right':'left'

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

  setLoggedPerson(person){
    this.loggedPerson = person
    this.zone.run(()=>{
      super.setToolbarTitle(person.username)
    })
  }

  loadProfileIcon(profile){
    if (profile != "") {
      this.userProfile = profile
    }
  }

  setCurrentLanguage(language){}

  setLanguageOption(languageOptions){
    this.zone.run(()=>{
      this.supportedLanguages = UmAngularUtil.kotlinListToJsArray(languageOptions)
    })
  }

  showLanguageOptions(){}

  handleUserLogout(){
    this.presenter.handleUserLogout()
  }

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
