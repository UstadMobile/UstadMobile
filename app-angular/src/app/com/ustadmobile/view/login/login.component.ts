import { Component, OnInit } from '@angular/core';
import { UmBaseComponent } from '../um-base-component';
import { Subscription } from 'rxjs';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';
import { UmBaseService } from '../../service/um-base.service';
import { Router, ActivatedRoute, NavigationEnd } from '@angular/router';
import { UmDbMockService } from '../../core/db/um-db-mock.service';
import { UmAngularUtil } from '../../util/UmAngularUtil';
import core from 'UstadMobile-core';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent extends UmBaseComponent implements core.com.ustadmobile.core.view.Login2View{

  subscription: Subscription;
  umFormLogin : FormGroup;
  formValidated : boolean = false;
  showProgress : boolean = false;
  serverUrl: string = "";
  presenter: core.com.ustadmobile.core.controller.Login2Presenter;
  private navigationSubscription;

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

    this.navigationSubscription = this.router.events.filter(event => event instanceof NavigationEnd)
    .subscribe((event:NavigationEnd) => {
      this.presenter = new core.com.ustadmobile.core.controller
        .Login2Presenter(this.context, UmAngularUtil.queryParamsToMap(), this);
        this.presenter.onCreate(null);
    });

    
  }

  ngOnInit() {
    super.ngOnInit();
    this.subscription = this.umService.getUmObserver().subscribe(content =>{
      if(content[UmAngularUtil.DISPATCH_RESOURCE]){
        this.umService.dispatchUpdate(UmAngularUtil.getContentToDispatch(UmAngularUtil.DISPATCH_TITLE,
        this.getString(this.MessageID.login)));
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
    if (this.navigationSubscription) {  
      this.navigationSubscription.unsubscribe();
    }
  }

}
