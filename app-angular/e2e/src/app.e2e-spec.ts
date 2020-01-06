import {Entries, sleepTime} from './app.po';
import { browser } from 'protractor';

describe('Content and Entry List', () => {
  let entries: Entries;
  browser.ignoreSynchronization = true
  browser.waitForAngularEnabled(true)
  beforeAll(() => {
    entries = new Entries();
  })

  it('giveApplicationLaunched_whenLibraryMenuSelected_shouldBeAbleToBrowseAndOpenContent', () => {
    entries.launch();
    expect(entries.getList().entries.count()).toBeGreaterThanOrEqual(4)
    entries.getList().entries.count().then(count => {
      entries.getList().entries.get(1).click()
      entries.getList().entries.get(1).click()
      entries.getDetails().details.get(0).click()
      browser.sleep(sleepTime)
      expect(browser.getCurrentUrl()).toContain(entries.views.content);
      //next and previous navigation btn are shown
      expect(entries.getDetails().controller.count()).toEqual(2)
      //Content was loaded to the iframe
      expect(entries.getDetails().content.getAttribute("src")).toBeTruthy()
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