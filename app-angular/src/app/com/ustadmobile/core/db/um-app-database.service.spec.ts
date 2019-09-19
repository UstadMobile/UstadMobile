import { TestBed } from '@angular/core/testing';

import { UmAppDatabaseService } from './um-app-database.service';

describe('UmAppDatabaseService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: UmAppDatabaseService = TestBed.get(UmAppDatabaseService);
    expect(service).toBeTruthy();
  });
});
