import { browser, element, by } from 'protractor';

export class HomePage {

  menus = ['Libraries', 'Reports']
  launch() {
    return browser.get(browser.baseUrl+"/Home/ContentEntryList?entryid=1311236") as Promise<any>;
  }

  getTitle() {
    return browser.getTitle() as Promise<any>;
  }
}

export class ElementUtils{
   getPageElts() {
    return {
      appHomeMenu: element.all(by.css('app-root app-home > header mz-sidenav mz-sidenav-link span'))
    };
  }
}
