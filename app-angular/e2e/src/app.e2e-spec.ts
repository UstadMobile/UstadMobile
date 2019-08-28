import {HomePage, ElementUtils } from './app.po';
import { browser, logging } from 'protractor';

describe('Default App behaviours', () => {
  let pageHome: HomePage;
  let elementUtils: ElementUtils;
  browser.ignoreSynchronization = true

  beforeEach(() => {
    pageHome = new HomePage();
    elementUtils = new ElementUtils();
  });

  it('giveApplication_whenLaunched_shouldUseDefaultUrl', () => {
    pageHome.launch();
    expect(browser.baseUrl).toContain('http://localhost:4200');
  });

  it('giveApplicationLaunched_whenNavigateToHome_shouldShowApplicationTitle', () => {
    pageHome.launch();
    expect(pageHome.getTitle()).toEqual('Ustad Mobile');
  });

  it('givenApplication_whenLaunched_shouldShowTwoSideMenus', () => {
    pageHome.launch();
    expect(elementUtils.getPageElts().appHomeMenu.count()).toEqual(2);
  });

  it('givenApplication_whenLaunchedAndMenuShown_shouldShowRightLabels', () => {
    pageHome.launch();
    expect(elementUtils.getPageElts().appHomeMenu.get(0).getText()).toEqual(pageHome.menus[0]);
    expect(elementUtils.getPageElts().appHomeMenu.get(1).getText()).toEqual(pageHome.menus[1]);
  });
});
