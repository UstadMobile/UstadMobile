import { Component, OnInit } from '@angular/core';
import { UmBaseComponent } from '../um-base-component';
import { Subscription } from 'rxjs';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';
import { UmBaseService } from '../../service/um-base.service';
import { Router, ActivatedRoute, NavigationEnd } from '@angular/router';
import { UmDbMockService } from '../../core/db/um-db-mock.service';
import { UmAngularUtil } from '../../util/UmAngularUtil';
import { com as core } from 'core';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent extends UmBaseComponent implements core.ustadmobile.core.view.Login2View{

  subscription: Subscription;
  umFormLogin : FormGroup;
  label_username: string = "";
  label_password: string = "";;
  formValidated : boolean = false;
  showProgress : boolean = false;
  serverUrl: string = "";
  presenter: core.ustadmobile.core.controller.Login2Presenter;

  constructor(umService: UmBaseService, router: Router, route: ActivatedRoute, 
    umDb: UmDbMockService, formBuilder: FormBuilder) {
    super(umService, router, route, umDb);
    this.umFormLogin = formBuilder.group({
      'username': ['', Validators.required],
      'password': ['', Validators.required]
    });

    this.umFormLogin.valueChanges.subscribe(
      (form: any) => { 
          this.formValidated = this.umFormLogin.status == "VALID";
      }
  );

    this.router.events.subscribe((e: any) => {
      if (e instanceof NavigationEnd) {
        this.presenter = new core.ustadmobile.core.controller
        .Login2Presenter(this.context, UmAngularUtil.queryParamsToMap(), this);
        this.presenter.onCreate(null);
      }
    });
  }

  ngOnInit() {
    super.ngOnInit();
    this.subscription = this.umService.getUmObserver().subscribe(content =>{
      if(content[UmAngularUtil.DISPATCH_RESOURCE]){
        this.umService.dispatchUpdate(UmAngularUtil.getContentToDispatch(UmAngularUtil.DISPATCH_TITLE,
        this.getString(this.MessageID.login))); 
        this.label_username = this.getString(this.MessageID.username);
        this.label_password = this.getString(this.MessageID.password);
      }
    });
  }

  startLogin(){
   this.formValidated  = false;
    this.presenter.handleClickLogin(this.umFormLogin.value.username,
      this.umFormLogin.value.password,this.serverUrl);
  }
  
  setInProgress(inProgress: boolean){
    this.showProgress = inProgress;
  }
  
  setErrorMessage(errorMessage: string){
    this.showError(errorMessage);
  }

  setServerUrl(serverUrl: string){
    this.serverUrl = serverUrl;
  }

  setUsername(username: string){
    this.umFormLogin.value.username = username;
  }

  setPassword(password: string){
    this.umFormLogin.value.password = password;
  }
  

  ngOnDestroy(){
    super.ngOnDestroy()
    this.presenter.onDestroy();
    this.subscription.unsubscribe();
  }

}
