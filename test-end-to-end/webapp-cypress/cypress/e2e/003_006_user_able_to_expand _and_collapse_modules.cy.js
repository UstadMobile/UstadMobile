describe('003_006_user_able_to_expand _and_collapse_modules', () => {
it('Start Ustad Test Server ', () => {
      // Start Test Server
        cy.ustadStartTestServer()
  })

 it('User able to expand and collapse the module blocks', () => {
      // Admin user login
        cy.ustadClearDbAndLogin('admin','testpass')

      // Add a new course
        cy.ustadAddCourse('003_002')

      // Add module block
         cy.contains('button','Edit').click()
         cy.ustadAddModuleBlock('module 1')

      // Add text block
         cy.contains('button','Edit').click()
         cy.ustadAddTextBlock('text 1')
         cy.get('[aria-label="More options"]').eq(1).click()
         cy.contains("li","Indent").click()
         cy.contains("button","Save").click()

      // Add module block
         cy.contains("button","Edit").click()
         cy.ustadAddModuleBlock('module 2')


     // Add text block
        cy.contains("button","Edit").click()
        cy.ustadAddTextBlock('text 2')
        cy.get('[aria-label="More options"]').eq(1).click()
        cy.contains("li","Indent").click()
        cy.contains("button","Save").click()

     //Testing Module Collapse icon working or not
        cy.get('svg[data-testid="KeyboardArrowUpIcon"]').eq(0).click()   // collapse module 1
        cy.contains('text 1').should('not.exist');
        cy.get('svg[data-testid="KeyboardArrowUpIcon"]').click({force: true})
        cy.contains("text 2").should('not.exist')

     //Testing Module Expand icon working or not
        cy.get('svg[data-testid="KeyboardArrowDownIcon"]').eq(0).click()
        cy.contains('text 1').should('exist');
        cy.get('svg[data-testid="KeyboardArrowDownIcon"]').click()
        cy.contains("text 2").should('exist')
    })

})