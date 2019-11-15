import { Component, OnDestroy } from '@angular/core';
import { UmBaseComponent } from '../um-base-component';
import { Subscription } from 'rxjs';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';
import { UmBaseService } from '../../service/um-base.service';
import { Router, ActivatedRoute, NavigationEnd } from '@angular/router';
import { UmAngularUtil } from '../../util/UmAngularUtil';
import core from 'UstadMobile-core';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})

export class LoginComponent extends UmBaseComponent implements OnDestroy, core.com.ustadmobile.core.view.Login2View {

  umFormLogin: FormGroup;
  formValidated: boolean = false;
  showProgress: boolean = false;
  class_login_tbn: string;
  private presenter: core.com.ustadmobile.core.controller.LoginPresenter;
  navigationSubscription;
  class_login_button: string;
  serverUrl: string;
  showRegistration: boolean = false;

  constructor(umService: UmBaseService, router: Router, route: ActivatedRoute, formBuilder: FormBuilder) {
    super(umService, router, route);

    this.class_login_button = this.umService.isLTRDirectionality() ? "right-align" : "left-align";
    this.umFormLogin = formBuilder.group({
      'username': ['', Validators.required],
      'password': ['', Validators.required]
    });

    this.umFormLogin.valueChanges.subscribe(
      () => {
        this.formValidated = this.umFormLogin.status == "VALID";
      });

    this.navigationSubscription = this.router.events.filter(event => event instanceof NavigationEnd)
      .subscribe(() => {
        UmAngularUtil.registerResourceReadyListener(this)
      });
  }

  ngOnInit() {
    super.ngOnInit();
  }

  onCreate() {
    UmAngularUtil.fireTitleUpdate(this.getString(this.MessageID.login))
    this.presenter = new core.com.ustadmobile.core.controller.LoginPresenter(
      this.context, UmAngularUtil.getArgumentsFromQueryParams(), this, this.systemImpl);
    this.presenter.onCreate(null);
  }

  handleClickLogin() {
    this.formValidated = false;
    this.presenter.handleClickLogin(this.umFormLogin.value.username,
      this.umFormLogin.value.password, this.serverUrl);
  }

  setInProgress(inProgress: boolean) {
    this.showProgress = inProgress;
  }

  setErrorMessage(errorMessage: string) {
    this.showError(errorMessage);
  }

  setRegistrationLinkVisible(visible){
    this.showRegistration = visible
  }

  setServerUrl(serverUrl: string) {
    this.serverUrl = serverUrl;
  }

  setUsername(username: string) {
    this.umFormLogin.value.username = username;
  }

  setPassword(password: string) {
    this.umFormLogin.value.password = password;
  }

  createAccount(){
    this.presenter.handleCreateAccount()
  }

  ngOnDestroy() {
    super.ngOnDestroy()
    this.presenter.onDestroy();
    if (this.navigationSubscription) {
      this.navigationSubscription.unsubscribe();
    }
  }

}
