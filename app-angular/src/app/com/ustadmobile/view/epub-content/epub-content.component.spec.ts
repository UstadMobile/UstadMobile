import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { EpubContentComponent } from './epub-content.component';

describe('EpubContentComponent', () => {
  let component: EpubContentComponent;
  let fixture: ComponentFixture<EpubContentComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ EpubContentComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(EpubContentComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
