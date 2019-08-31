import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { XapiReportDetailsComponent } from './xapi-report-details.component';

describe('XapiReportDetailsComponent', () => {
  let component: XapiReportDetailsComponent;
  let fixture: ComponentFixture<XapiReportDetailsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ XapiReportDetailsComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(XapiReportDetailsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
