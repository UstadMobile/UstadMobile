import { TestBed } from '@angular/core/testing';

import { UmBaseService } from './um-base.service';

describe('UmBaseService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: UmBaseService = TestBed.get(UmBaseService);
    expect(service).toBeTruthy();
  });
});
