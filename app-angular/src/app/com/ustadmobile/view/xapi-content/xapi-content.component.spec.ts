import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { XapiContentComponent } from './xapi-content.component';

describe('XapiContentComponent', () => {
  let component: XapiContentComponent;
  let fixture: ComponentFixture<XapiContentComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ XapiContentComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(XapiContentComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
