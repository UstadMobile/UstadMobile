import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ContentEntryDetailComponent } from './content-entry-detail.component';

describe('ContentEntryDetailComponent', () => {
  let component: ContentEntryDetailComponent;
  let fixture: ComponentFixture<ContentEntryDetailComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ContentEntryDetailComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ContentEntryDetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
