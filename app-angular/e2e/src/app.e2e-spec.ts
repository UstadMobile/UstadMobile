import {HomePage, DashboardPage, ReportOptions, ReportDetails } from './app.po';
import { browser } from 'protractor';
const DEFAULT_TIMEOUT_INTERVAL: number = 5000
describe('Default App behaviours', () => {
  let pageHome: HomePage;
  browser.ignoreSynchronization = true
  browser.waitForAngularEnabled(true)
  beforeAll(() => {
    pageHome = new HomePage();
  })

  it('giveApplication_whenLaunched_shouldUseDefaultUrl', () => {
    pageHome.launch();
    expect(browser.baseUrl).toContain(pageHome.baseUrl);
  });

  it('giveApplication_whenLaunchedAngNavigateToHome_shouldShowApplicationTitle', () => {
    pageHome.launch().then(function () {
        expect(pageHome.getPage().title).toEqual(pageHome.title);
    });
  });

  it('givenApplication_whenLaunched_shouldShowTwoSideMenus', () => {
    pageHome.launch().then( ()=>{
      expect(pageHome.getPage().menus.count()).toEqual(2);
    });
    
  });

  it('givenApplication_whenLaunchedAndMenuShown_shouldShowRightLabels', () => {
    pageHome.launch().then(() =>{
      expect(pageHome.getPage().menus.get(0).getText()).toEqual(pageHome.menus[0]);
      expect(pageHome.getPage().menus.get(1).getText()).toEqual(pageHome.menus[1]);
    });
  });
});


describe('Report Dashboard', () => {
  let pageDashboard: DashboardPage;
  browser.ignoreSynchronization = true

  beforeAll(() => {
    pageDashboard = new DashboardPage();
  });


  it('givenApplicationLaunched_whenReportMenuClicked_shouldOpenTheDashboard', () => {
    pageDashboard.launch();
    expect(browser.getCurrentUrl()).toContain(pageDashboard.views.dashboard);
  });

  it('givenApplicationLaunchedAndDashboardOpen_whenNewReportButtonIsClicked_shouldOpenReportOptionsScreen', () => {
    pageDashboard.launch();
    pageDashboard.getPage().newReport.click()
    browser.sleep(1000)
    expect(browser.getCurrentUrl()).toContain(pageDashboard.views.options);
  });
});

describe('Report Options', () => {
  let pageOptions: ReportOptions;
  browser.ignoreSynchronization = true

  beforeAll(() => {
    pageOptions = new ReportOptions();
  });

it('givenApplication_whenCreatingNewReport_shouldShowAllOptions', () => {
    pageOptions.launch();
    expect(pageOptions.getPage().formFields.count()).toEqual(15);
  });


  it('givenApplication_whenVisializationTypeClicked_shouldShowAllChoices', () => {
    pageOptions.launch();
    pageOptions.getPage().selectViews.get(0).click()
    expect(pageOptions.getPage().selectViewsOption.get(0).getAttribute("value")).toEqual('0');
  });

  it('givenApplication_whenWhatSectionIsClicked_shouldOpenChoices', () => {
    pageOptions.launch();
    pageOptions.getPage().inputViews.get(0).click()
    browser.sleep(2000)
    expect(pageOptions.getPage().dialog.count()).toEqual(1);
  }); 

  it('givenApplication_whenDoneButtonIsClicked_shouldOpenGraphPreview', () => {
    pageOptions.launch();
    //Opt some values
    pageOptions.selectDropDown(2,1)
    pageOptions.selectDropDown(3,4).then(()=> {
      browser.sleep(2000)
      pageOptions.getPage().doneBtn.click()
      expect(browser.getCurrentUrl()).toContain(pageOptions.views.done)
    })
  });

});


describe('Report Details', () => {
  let pageDetails: ReportDetails;
  browser.ignoreSynchronization = true

  beforeAll(() => {
    pageDetails = new ReportDetails();
  });


  it('givenApplication_whenOpen_shouldShowGoodleChartsAndTables', () => {
    pageDetails.launch();
    expect(pageDetails.getPage().graph.count()).toEqual(1);
    expect(pageDetails.getPage().tableRows.count()).toBeGreaterThan(0);
  });

  it('givenApplication_whenAddToDashboardButtonIsClicked_shouldAddToDashboard', () => {
    pageDetails.launch();
    pageDetails.getPage().addBtn.click()
    browser.sleep(2000)
    //expect(browser.getCurrentUrl()).toContain(pageDetails.views.dashboard);
  });

});