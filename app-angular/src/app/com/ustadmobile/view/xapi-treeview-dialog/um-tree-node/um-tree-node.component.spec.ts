import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { UmTreeNodeComponent } from './um-tree-node.component';

describe('UmTreeNodeComponent', () => {
  let component: UmTreeNodeComponent;
  let fixture: ComponentFixture<UmTreeNodeComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ UmTreeNodeComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(UmTreeNodeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
