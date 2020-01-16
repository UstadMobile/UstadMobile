import {Entries, sleepTime, Reports} from './app.po';
import { browser, element, by } from 'protractor';
const fs = require('fs'), path = require('path')

describe('Content and Entry List', () => {
  let entries: Entries;
  browser.ignoreSynchronization = true
  browser.waitForAngularEnabled(true)
  beforeAll(() => {
    entries = new Entries();
  })

  it('giveApplicationLaunched_whenLibraryMenuSelected_shouldBeAbleToBrowseAndOpenContent', () => {
    entries.launch();
    expect(entries.getPage().entries.count()).toBeGreaterThanOrEqual(4)
    entries.getPage().entries.count().then(count => {
      entries.getPage().entries.get(1).click()
      entries.getPage().entries.get(1).click()
      entries.getPage().details.get(0).click()
      browser.sleep(sleepTime)
      expect(browser.getCurrentUrl()).toContain(entries.views.content);
      //next and previous navigation btn are shown
      expect(entries.getPage().controller.count()).toEqual(2)
      //Content was loaded to the iframe
      expect(entries.getPage().content.getAttribute("src")).toBeTruthy()
    });
  });
});

describe('Xapi Reports', () => {
  let reports: Reports;
  browser.ignoreSynchronization = true
  browser.waitForAngularEnabled(true)
  beforeAll(() => {
    reports = new Reports();
  })

  it('giveApplicationLaunched_whenAdminLoggedin_shouldBeAbleToCreateReports', () => {
    reports.launch();
    const filePath = path.join(__dirname, "../../../") + "app-ktor-server/./build/container/admin.txt",
    password = fs.readFileSync(filePath, {encoding: 'utf-8'})
    reports.getPage().profile.get(0).click()
    reports.getPage().loginInputs.get(0).sendKeys("admin")
    reports.getPage().loginInputs.get(1).sendKeys(password)
    reports.getPage().loginBtn.click()
    browser.sleep(sleepTime/2)
    reports.getPage().main.get(0).click()
    browser.sleep(sleepTime/2)
    reports.getPage().menus.get(1).click()
    reports.selectDropDown(2,2).then(()=> {
      reports.getPage().options.doneBtn.click()
      //preview is shown
      expect(browser.getCurrentUrl()).toContain(reports.views.preview);
      //graph was shown
      expect(reports.getPage().options.graph.count()).toEqual(1);
    })

  });
});