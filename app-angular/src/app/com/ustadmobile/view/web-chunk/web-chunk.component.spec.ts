import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { WebChunkComponent } from './web-chunk.component';

describe('WebChunkComponent', () => {
  let component: WebChunkComponent;
  let fixture: ComponentFixture<WebChunkComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ WebChunkComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(WebChunkComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
