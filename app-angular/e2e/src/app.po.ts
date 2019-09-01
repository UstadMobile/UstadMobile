import { browser, element, by } from 'protractor';

export const sleepTime = 500;

export class HomePage {

  menus = ['Libraries', 'Reports']
  baseUrl = "http://localhost:";
  launch() {
    browser.get(browser.baseUrl+"/Home/ContentEntryList?entryid=1311236") as Promise<any>;
    browser.sleep(sleepTime)
  }

  getPage() {
    return new ElementUtils().getPageElements().componentHome;
  }
}

export class DashboardPage{

  views = {"dashboard":"ReportDashboard", "options":"ReportOptions"};

  launch() {
    const pageHome = new HomePage()
    pageHome.launch()
    pageHome.getPage().menus.get(1).click()
    browser.sleep(sleepTime);
  }
  getPage() {
    return new ElementUtils().getPageElements().componentReportDashboard
  }
}

export class ReportOptions{

  views = {"what":"EntriesTreeDialog", "done":""}
  launch() {
    const pageDashboard = new DashboardPage()
    pageDashboard.launch()
    pageDashboard.getPage().newReport.click()
    browser.sleep(sleepTime);
  }
  getPage() {
    return new ElementUtils().getPageElements().componentReportOptions
  }
}

export class ReportDetails{

  views = {"dashboard":"ReportDashboard"}
  launch() {
    const pageOption = new ReportOptions()
    pageOption.launch()
    pageOption.getPage().doneBtn.click()
    browser.sleep(sleepTime);
  }
  getPage() {
    return new ElementUtils().getPageElements().reportDetails
  }
}

export class ElementUtils{
   getPageElements() {
    return {
      componentHome:{
        title: browser.getTitle() as Promise<any>,
        menus: element.all(by.css('app-root app-home > header mz-sidenav mz-sidenav-link span'))
      },
      componentReportDashboard: {
        newReport: element.all(by.css('app-root app-report-dashboard > div div.fixed-action-btn')),
      },
      componentReportOptions:{
        formFields: element.all(by.css('app-xapi-report-options > div div.input-field')),
        selectViews: element.all(by.css('app-xapi-report-options > div mz-select-container')),
        inputViews: element.all(by.css('app-xapi-report-options > div mz-input-container .what')),
        selectViewsOption: element.all(by.css('app-xapi-report-options > div option')),
        doneBtn: element.all(by.css('app-xapi-report-options > div div.fixed-action-btn'))
      },
      reportDetails: {
        graph: element.all(by.css('app-xapi-report-details > div google-chart')),
        addBtn: element.all(by.css('app-xapi-report-details > div div.fixed-action-btn'))
      }
    };
  }
}
