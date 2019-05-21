import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ContentEntryDetailsComponent } from './content-entry-details.component';

describe('ContentEntryDetailsComponent', () => {
  let component: ContentEntryDetailsComponent;
  let fixture: ComponentFixture<ContentEntryDetailsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ContentEntryDetailsComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ContentEntryDetailsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
