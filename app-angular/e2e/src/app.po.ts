import { browser, element, by} from 'protractor';
export const sleepTime = 3000;
export const rootUid = "-4103245208651563007"

export function launchApp(){
  browser.get(browser.baseUrl) as Promise<any>;
  return browser.waitForAngularEnabled(true)
}

export class Entries{

  views = {details:"ContentEntryDetail", list: "ContentEntryList", content: "EpubContent"}
  
  launch(){
    launchApp()
    browser.sleep(sleepTime)
  }

  getPage() {
    return new ElementUtils().getPageElements().content
  }
}

export class Reports{
  views = {list: "ContentEntryList", preview:"ReportPreview"}

  launch(){
    launchApp()
    browser.sleep(sleepTime)
  }

  getPage(){
    return new ElementUtils().getPageElements().reports
  }

  selectDropDown(dropdownIndex, optionIndex){
    this.getPage().options.selectViews.get(dropdownIndex).click()
    return new ElementUtils().selectDropDown('app-xapi-report-options',dropdownIndex,optionIndex);
  }
}


export class ElementUtils{
   getPageElements() {
    return {
      reports:{
        profile: element.all(by.css('app-root app-home > header mz-sidenav mz-sidenav-header div i')),
        menus: element.all(by.css('app-root app-home > header mz-sidenav mz-sidenav-link span')),
        loginInputs: element.all(by.css('app-login > div div form div mz-input-container input')),
        loginBtn: element.all(by.css('app-login > div div form div div button')),
        options: {
          selectViews: element.all(by.css('app-xapi-report-options > div mz-select-container')),
          selectViewsOption: element.all(by.css('app-xapi-report-options > div option')),
          doneBtn: element.all(by.css('app-xapi-report-options > div div.fixed-action-btn-right')),
          graph: element.all(by.css('app-xapi-report-details > div google-chart'))
        }
      },

      content: {
        entries: element.all(by.css('app-root app-content-entry-list > div ul li.open-content')),
        details: element.all(by.css('app-content-entry-detail > div div.fixed-action-btn-right')),
        controller: element.all(by.css('app-epub-content > div div button.orange')),
        content: element.all(by.css('app-epub-content > div div iframe'))
      },
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
