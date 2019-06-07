import { Language } from './../../core/db/um-db-mock.service';
import { Component } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { Location } from '@angular/common';
import { UmBaseComponent } from '../um-base-component';
import { UmBaseService } from '../../service/um-base.service';
import { UmDbMockService } from '../../core/db/um-db-mock.service';
import { Subscription } from 'rxjs/internal/Subscription';
import { UmAngularUtil } from '../../util/UmAngularUtil';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent extends UmBaseComponent {

  toolbar_title: string;
  menu_libaries : string;
  menu_reports: string;
  subscription: Subscription;
  icon_position_class : string;
  toolbar_icon_class : string;
  toolbar_arrow: string;
  toolbar_title_class: string;
  drawer_menu_class: string;
  language_class : string;
  supportedLanguages = [];
  umFormLanguage : FormGroup;

  constructor(private location: Location,umService: UmBaseService,
              router: Router, route: ActivatedRoute, umDb: UmDbMockService, formBuilder: FormBuilder) {
    super(umService, router, route, umDb);
    this.icon_position_class =  this.umService.isLTRDirectionality() ? "left":"right icon-left-spacing";
    this.toolbar_icon_class = this.umService.isLTRDirectionality() ? "left icon-right-spacing":"right icon-left-spacing";
    this.toolbar_arrow = this.umService.isLTRDirectionality() ? "arrow_back":"arrow_forward";
    this.toolbar_title_class = this.umService.isLTRDirectionality() ? "brand-logo-ltr":"brand-logo-rtl";
    this.drawer_menu_class = this.umService.isLTRDirectionality() ? "right drawer-menu-ltr":"left drawer-menu-rtl";
    this.language_class = this.umService.isLTRDirectionality() ? "language-ltr":"language-rtl";

    this.umFormLanguage = formBuilder.group({
      'language': ['', Validators.required]
    });
   }

  ngOnInit() {
    super.ngOnInit()
    this.subscription = this.umService.getUmObserver().subscribe(content =>{
      if(content[UmAngularUtil.DISPATCH_TITLE]){
        this.toolbar_title = content[UmAngularUtil.DISPATCH_TITLE];
      }

      if(content[UmAngularUtil.DISPATCH_LANGUAGES]){
        //do something where all resources are ready
        this.supportedLanguages = this.umService.getSupportedLanguages();
      }
    });

  this.umFormLanguage.valueChanges.subscribe((form: any) => {
    if(form.language !== ""){
        const route = UmAngularUtil.getDifferentLanguageRoute(form.Language)
        this.systemImpl.go(route.view, route.args, this.context);
       }
  });
   
  }

  goBack(){
    this.umService.goBack();
  }

  openDrawer(){
    
  }

  ngOnDestroy(): void {
    super.ngOnDestroy();
    this.subscription.unsubscribe();
    
  }

}
