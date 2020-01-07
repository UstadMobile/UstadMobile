import { browser, element, by} from 'protractor';

export const sleepTime = 500;

export const rootUid = "-4103245208651563007"

export class HomePage{

  menus = ['Libraries', 'Reports']
  title = 'Ustad Mobile'
  baseUrl = "http://localhost:";
  launch() {
    browser.get(browser.baseUrl+"/#/Home/ContentEntryList?entryid="+rootUid) as Promise<any>;
    return browser.waitForAngularEnabled(true)
  }

  getPage() {
    return new ElementUtils().getPageElements().componentHome;
  }
}

export class EntryListPage{

  views = {details:"ContentEntryDetail", list: "ContentEntryList"}
  
  launch(){
    const pageHome = new HomePage()
    pageHome.launch()
    browser.sleep(sleepTime * 8)
  }

  getPage() {
    return new ElementUtils().getPageElements().entryList
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
  views = {"what":"EntriesTreeDialog", "done":"ReportPreview"}
  launch() {
    const pageDashboard = new DashboardPage()
    pageDashboard.launch()
    pageDashboard.getPage().newReport.click()
  }

  selectDropDown(dropdownIndex, optionIndex){
    this.getPage().selectViews.get(dropdownIndex).click()
    browser.sleep(sleepTime * 2)
    return new ElementUtils().selectDropDown('app-xapi-report-options',dropdownIndex,optionIndex);
  }

  getPage() {
    return new ElementUtils().getPageElements().componentReportOptions
  }
}

export class ReportDetails{

  views = {"dashboard":"ReportDashboard"}
  launch() {
    browser.get(browser.baseUrl+'/#/Home/ReportPreview?ref=null&options=%7B"chartType":100,"yAxis":200,"xAxis":302,"subGroup":300,"whoFilterList":%5B1%5D,"didFilterList":%5B200%5D,"objectsList":%5B%5D,"entriesList":%5B%5D,"fromDate":0,"toDate":0,"locationsList":%5B%5D,"reportTitle":"null"%7D') as Promise<any>;
    return new ElementUtils().launchAsync()
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
      entryList: {
        entries: element.all(by.css('app-root app-content-entry-list > div ul li.open-content'))
      },
      componentReportDashboard: {
        newReport: element.all(by.css('app-root app-report-dashboard > div div.fixed-action-btn-right')),
      },
      componentReportOptions:{
        formFields: element.all(by.css('app-xapi-report-options > div div.input-field')),
        selectViews: element.all(by.css('app-xapi-report-options > div mz-select-container')),
        inputViews: element.all(by.css('app-xapi-report-options > div mz-input-container')),
        selectViewsOption: element.all(by.css('app-xapi-report-options > div option')),
        doneBtn: element.all(by.css('app-xapi-report-options > div div.fixed-action-btn-right')),
        dialog: element.all(by.css('xapi-treeview-dialog mz-modal'))
      },
      reportDetails: {
        graph: element.all(by.css('app-xapi-report-details > div google-chart')),
        tableRows: element.all(by.css('app-xapi-report-details > div div table tbody tr')),
        addBtn: element.all(by.css('app-xapi-report-details > div div.fixed-action-btn-right'))
      }
    };
  }

  launchAsync(){
    var currentTitle;
    return browser.getTitle().then(function(title) {
      currentTitle = title;
    }).then(function() {
            browser.wait(function() {
                return browser.getTitle().then(function (title) {
                  return title == currentTitle;
                });
            });
        }
    )
  }

  selectDropDown(root, instanceIndex, optionIndex){
    return element.all(by.css(root+' div.select-wrapper')).get(instanceIndex).all(by.css('ul li')).get(optionIndex).click();
  }
}
