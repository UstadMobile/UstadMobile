import { TestBed } from '@angular/core/testing';

import { UmDbMockService } from './um-db-mock.service';

describe('UmDbMockService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: UmDbMockService = TestBed.get(UmDbMockService);
    expect(service).toBeTruthy();
  });
});
