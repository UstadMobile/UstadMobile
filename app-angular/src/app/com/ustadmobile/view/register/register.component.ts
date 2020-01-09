import { Component, OnDestroy} from '@angular/core';
import { UmBaseComponent } from '../um-base-component';
import { UmBaseService } from '../../service/um-base.service';
import { Router, ActivatedRoute, NavigationEnd } from '@angular/router';
import { UmAngularUtil } from '../../util/UmAngularUtil';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';
import core from 'UstadMobile-core';
import entity from 'UstadMobile-lib-database-entities';

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css']
})
export class RegisterComponent extends UmBaseComponent implements OnDestroy,
 core.com.ustadmobile.core.view.Register2View {

  umFormRegister: FormGroup;
  label_wrong_email: string = "";
  label_password_mismatch: string = "";
  formValidated: boolean = false;
  showProgress: boolean = false;
  serverUrl: string = "";
  private presenter: core.com.ustadmobile.core.controller.Register2Presenter;
  private navigationSubscription;

  constructor(umService: UmBaseService, router: Router, route: ActivatedRoute, formBuilder: FormBuilder) {
    super(umService, router, route);
    this.umFormRegister = formBuilder.group({
      'first_name': ['', Validators.required],
      'last_name': ['', Validators.required],
      'password': ['', [Validators.required, Validators.minLength(8)]],
      'username': ['', Validators.required],
      'email': ['', [Validators.required, Validators.pattern('[a-zA-Z0-9._-]+@[a-z]+\.+[a-z]+')]],
      'confirm_password': ['', [Validators.required, Validators.minLength(8)]]
    });

    this.umFormRegister.valueChanges.subscribe(
      (form: any) => {
        this.formValidated = this.umFormRegister.valid && form.password == form.confirm_password;
      }
    );

    this.navigationSubscription = this.router.events.filter(event => event instanceof NavigationEnd)
      .subscribe(() => {
        UmAngularUtil.registerResourceReadyListener(this)
      });
  }

  onCreate() {
    this.presenter = new core.com.ustadmobile.core.controller.Register2Presenter(
      this.context, UmAngularUtil.getArgumentsFromQueryParams(), this);
    this.presenter.onCreate(null);

    UmAngularUtil.fireTitleUpdate(this.getString(this.MessageID.create_new_account))
    this.label_wrong_email = this.getString(this.MessageID.register_incorrect_email);
    this.label_password_mismatch = this.getString(this.MessageID.filed_password_no_match);
  }

  ngOnInit() {
    super.ngOnInit();
  }

  handleClickRegister() {
    this.formValidated = false;
    const formValues = this.umFormRegister.value;
    const person = new entity.com.ustadmobile.lib.db.entities.Person(formValues.username,
      formValues.first_name, formValues.last_name);
    person.emailAddr = formValues.email;
    this.presenter.handleClickRegister(person, formValues.password, this.serverUrl);
  }
  setErrorMessageView(errorMessage: string) {
    this.showError(errorMessage);
  }

  setServerUrl(url: string) {
    this.serverUrl = url;
  }

  setInProgress(inProgress: boolean) {
    this.showProgress = inProgress;
  }

  ngOnDestroy() {
    super.ngOnDestroy()
    this.presenter.onDestroy();
    if (this.navigationSubscription) {
      this.navigationSubscription.unsubscribe();
    }
  }


}
