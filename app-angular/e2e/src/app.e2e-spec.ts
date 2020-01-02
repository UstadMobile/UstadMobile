import {ReportOptions, EntryListPage} from './app.po';
import { browser } from 'protractor';

describe('Content Entry', () => {
  let pageEntryList: EntryListPage;
  browser.ignoreSynchronization = true
  browser.waitForAngularEnabled(true)
  beforeAll(() => {
    pageEntryList = new EntryListPage();
  })

  it('giveApplicationLaunched_whenLibraryMenuSelected_shouldBeAbleToBrowseContent', () => {
    pageEntryList.launch();
    expect(pageEntryList.getPage().entries.count()).toBeGreaterThanOrEqual(4)
    pageEntryList.getPage().entries.count().then(count => {
      pageEntryList.getPage().entries.get(1).click()
      pageEntryList.getPage().entries.get(0).click()
      expect(browser.getCurrentUrl()).toContain(pageEntryList.views.details);
    });
  });
});

/* fdescribe('xAPI Reports', () => {
  let pageOptions: ReportOptions;
  browser.ignoreSynchronization = true
  browser.waitForAngularEnabled(true)
  beforeAll(() => {
    pageOptions = new ReportOptions();
  })

  it('giveApplicationLaunched_whenReportMenuSelected_shouldBeAbleToCreateReportAndPreview', () => {
    pageOptions.launch();
    pageOptions.getPage().inputViews.get(0).click()
    browser.sleep(2000)
    expect(pageOptions.getPage().dialog.count()).toEqual(1);
    pageOptions.selectDropDown(2,1)
    pageOptions.selectDropDown(3,4).then(()=> {
      browser.sleep(2000)
      pageOptions.getPage().doneBtn.click()
      expect(browser.getCurrentUrl()).toContain(pageOptions.views.done)
    })
  });
}); */