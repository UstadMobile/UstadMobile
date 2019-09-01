import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { XapiTreeviewDialogComponent } from "./XapiTreeviewDialogComponent";

describe('XapiTreeviewDialogComponent', () => {
  let component: XapiTreeviewDialogComponent;
  let fixture: ComponentFixture<XapiTreeviewDialogComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ XapiTreeviewDialogComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(XapiTreeviewDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
