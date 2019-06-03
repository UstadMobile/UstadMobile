import { Component} from '@angular/core';
import { UmBaseComponent } from '../um-base-component';
import { UmBaseService } from '../../service/um-base.service';
import { Router, ActivatedRoute, NavigationEnd } from '@angular/router';
import { UmDbMockService } from '../../core/db/um-db-mock.service';
import { Subscription } from 'rxjs';
import { UmAngularUtil } from '../../util/UmAngularUtil';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';
import { com as core } from 'core';
import { com as db } from 'lib-database';
import { umValidatePassword } from '../../util/UmValidators';

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css']
})
export class RegisterComponent extends UmBaseComponent implements core.ustadmobile.core.view.Register2View{

  subscription: Subscription;
  umFormRegister : FormGroup;
  label_first_name: string = "";
  label_last_name: string = "";
  label_username: string = "";
  label_email: string = "";
  label_password: string = "";
  label_confirm_password: string = "";
  formValidated : boolean = false;
  showProgress : boolean = false;
  serverUrl: string = "";
  presenter: core.ustadmobile.core.controller.Register2Presenter;

  constructor(umService: UmBaseService, router: Router, route: ActivatedRoute, 
    umDb: UmDbMockService, formBuilder: FormBuilder) {
    super(umService, router, route, umDb);
    this.umFormRegister = formBuilder.group({
      'first_name': ['', Validators.required],
      'last_name': ['', Validators.required],
      'password': ['', Validators.required, umValidatePassword, Validators.minLength(8)],
      'username': ['', Validators.required],
      'email': ['', Validators.compose([Validators.required,Validators.pattern('[a-zA-Z0-9._-]+@[a-z]+\.+[a-z]+')])],
      'confirm_password': ['', Validators.required, umValidatePassword, Validators.minLength(8)]
    });

    this.umFormRegister.valueChanges.subscribe(
      (form: any) => {
          this.formValidated = this.umFormRegister.status == "VALID";
      }
  );

    this.router.events.subscribe((e: any) => {
      if (e instanceof NavigationEnd) {
        this.presenter = new core.ustadmobile.core.controller
        .Register2Presenter(this.context, UmAngularUtil.queryParamsToMap(), this);
        this.presenter.onCreate(null);
      }
    });
  }

  ngOnInit() {
    super.ngOnInit();
    this.subscription = this.umService.getUmObserver().subscribe(content =>{
      if(content[UmAngularUtil.DISPATCH_RESOURCE]){
        this.umService.dispatchUpdate(UmAngularUtil.getContentToDispatch(UmAngularUtil.DISPATCH_TITLE,
        this.getString(this.MessageID.create_new_account))); 

        this.label_first_name = this.getString(this.MessageID.first_name);
        this.label_last_name = this.getString(this.MessageID.last_name);
        this.label_username = this.getString(this.MessageID.username);
        this.label_email = this.getString(this.MessageID.email);
        this.label_password = this.getString(this.MessageID.password);
        this.label_confirm_password = this.getString(this.MessageID.confirm_password);
      }
    });
  }

  startRegistration(){
    const person = new db.ustadmobile.lib.db.entities.Person();
    
    this.presenter.handleClickRegister(person, this.umFormRegister.value.password,this.serverUrl);
  }
  setErrorMessageView(errorMessage: string){
    this.showError(errorMessage);
  }
  
  setServerUrl(url: string){
    this.serverUrl = url;
  }

  setInProgress(inProgress: boolean){
    this.showProgress = inProgress;
  }

  ngOnDestroy(){
    super.ngOnDestroy()
    this.presenter.onDestroy();
    this.subscription.unsubscribe();
  }


}
