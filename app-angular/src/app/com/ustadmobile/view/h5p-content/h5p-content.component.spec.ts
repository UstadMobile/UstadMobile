import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { H5pContentComponent } from './h5p-content.component';

describe('H5pContentComponent', () => {
  let component: H5pContentComponent;
  let fixture: ComponentFixture<H5pContentComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ H5pContentComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(H5pContentComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
