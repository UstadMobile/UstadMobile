import { Component} from '@angular/core';
import { UmBaseComponent } from '../um-base-component';
import { UmBaseService } from '../../service/um-base.service';
import { Router, ActivatedRoute, NavigationEnd } from '@angular/router';
import { UmDbMockService } from '../../core/db/um-db-mock.service';
import { Subscription } from 'rxjs';
import { UmAngularUtil } from '../../util/UmAngularUtil';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';
import core from 'UstadMobile-core';
import db from 'UstadMobile-lib-database';

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css']
})
export class RegisterComponent extends UmBaseComponent implements core.com.ustadmobile.core.view.Register2View{

  subscription: Subscription;
  umFormRegister : FormGroup;
  label_wrong_email: string = "";
  label_password_mismatch: string = "";
  formValidated : boolean = false;
  showProgress : boolean = false;
  serverUrl: string = "";
  presenter: core.com.ustadmobile.core.controller.Register2Presenter;
  private navigationSubscription;

  constructor(umService: UmBaseService, router: Router, route: ActivatedRoute, 
    umDb: UmDbMockService, formBuilder: FormBuilder) {
    super(umService, router, route, umDb);
    this.umFormRegister = formBuilder.group({
      'first_name': ['', Validators.required],
      'last_name': ['', Validators.required],
      'password': ['', [Validators.required, Validators.minLength(8)]],
      'username': ['', Validators.required],
      'email': ['', [Validators.required,Validators.pattern('[a-zA-Z0-9._-]+@[a-z]+\.+[a-z]+')]],
      'confirm_password': ['', [Validators.required, Validators.minLength(8)]]
    });

    this.umFormRegister.valueChanges.subscribe(
      (form: any) => { 
          this.formValidated = this.umFormRegister.valid && form.password == form.confirm_password;
      }
  );

    this.navigationSubscription = this.router.events.filter(event => event instanceof NavigationEnd)
    .subscribe((event:NavigationEnd) => {
      this.presenter = new core.com.ustadmobile.core.controller
        .Register2Presenter(this.context, UmAngularUtil.queryParamsToMap(), this);
        this.presenter.onCreate(null);
    });
  }

  ngOnInit() {
    super.ngOnInit();
    this.subscription = this.umService.getUmObserver().subscribe(content =>{
      if(content[UmAngularUtil.DISPATCH_RESOURCE]){
        this.umService.dispatchUpdate(UmAngularUtil.getContentToDispatch(UmAngularUtil.DISPATCH_TITLE,
        this.getString(this.MessageID.create_new_account))); 
        this.label_wrong_email = this.getString(this.MessageID.register_incorrect_email);
        this.label_password_mismatch = this.getString(this.MessageID.filed_password_no_match);
      }
    });
  }

  startRegistration(){
    this.formValidated = false;
    const formValues = this.umFormRegister.value;
    const person = new db.ustadmobile.lib.db.entities.Person(formValues.username, 
      formValues.first_name, formValues.last_name);
      person.emailAddr = formValues.email;
    this.presenter.handleClickRegister("person", formValues.password,this.serverUrl);
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
    if (this.navigationSubscription) {  
      this.navigationSubscription.unsubscribe();
    }
  }


}
