import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DownloadFromPlaystoreComponent } from './download-from-playstore.component';

describe('DownloadFromPlaystoreComponent', () => {
  let component: DownloadFromPlaystoreComponent;
  let fixture: ComponentFixture<DownloadFromPlaystoreComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DownloadFromPlaystoreComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DownloadFromPlaystoreComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
