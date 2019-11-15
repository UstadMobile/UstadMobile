import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { XapiReportOptionsComponent } from './xapi-report-options.component';

describe('XapiReportOptionsComponent', () => {
  let component: XapiReportOptionsComponent;
  let fixture: ComponentFixture<XapiReportOptionsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ XapiReportOptionsComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(XapiReportOptionsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
