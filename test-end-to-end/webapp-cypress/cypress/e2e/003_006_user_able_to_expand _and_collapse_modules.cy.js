describe('Ustad mobile course tests', () => {
  beforeEach(() => {
    cy.clearIndexedDb('localhost_8087');

    cy.visit('http://localhost:8087/')
  });

 it('003_006_user_able_to_expand _and_collapse_modules', () => {
   // cy.visit('http://localhost:8087/')
    cy.get('input[id="username"]').type("admin")
    cy.get('input[id="password"]').type("testpass")
    cy.get('button[id="login_button"]').click()

    // Add new course
    cy.contains("Courses").click()
    cy.contains("button","Course").click()
    cy.contains("Add a new course").click()
    cy.get('input[id="clazz_name"]').type("class 003_007")
    cy.get('div[data-placeholder="Description"]').type("simple class")
    // Add module block
    cy.contains("Add block").click()
    cy.contains("Module").click()
    cy.get('input[id="title"]').type("module 1")
    cy.contains("button","Done").click()
    cy.contains("button","Save").click()
    // Add text block
    cy.contains("button","Edit").click()
    cy.contains("Add block").click()
    cy.contains("Text").click()
    cy.get('input[id="title"]').type("Text 1")
    cy.get('div[data-placeholder="Description"]').type("a simple block test")
    cy.contains("button","Done").click()
    cy.wait(2000) // wait for 2 seconds
    cy.get('svg[data-testid="MoreVertIcon"]').last().click()
    cy.contains("li","Indent").click()
    cy.wait(2000) // wait for 2 seconds
    cy.contains("button","Save").click()
  // Add module block
    cy.contains("button","Edit").click()
    cy.contains("Add block").click()
    cy.contains("Module").click()
    cy.get('input[id="title"]').type("module 2")
    cy.contains("button","Done").click()
    cy.wait(2000) // wait for 2 seconds
    cy.contains("button","Save").click()
    cy.wait(2000) // wait for 2 seconds

     // Add text block
        cy.contains("button","Edit").click()
        cy.contains("Add block").click()
        cy.contains("Block that will display formatted text").click({force: true})
        cy.get('input[id="title"]').type("Text 2")
        cy.get('div[data-placeholder="Description"]').type("a simple block test")
        cy.contains("button","Done").click()
        cy.wait(2000) // wait for 2 seconds
        cy.get('svg[data-testid="MoreVertIcon"]').last().click()
        cy.contains("li","Indent").click()
        cy.wait(2000) // wait for 2 seconds
        cy.contains("button","Save").click()

        //Testing Module Collapse icon working or not
    cy.get('svg[data-testid="KeyboardArrowUpIcon"]').first().click()
    cy.contains('Text 1').should('not.exist');


    cy.get('svg[data-testid="KeyboardArrowUpIcon"]').click({force: true})
    cy.contains("Text 2").should('not.exist')
    //cy.wait(2000) // wait for 2 seconds
      //Testing Module Expand icon working or not

    cy.get('svg[data-testid="KeyboardArrowDownIcon"]',{ timeout: 5000 }).first().click()
        cy.contains('Text 1').should('exist');
    cy.get('svg[data-testid="KeyboardArrowDownIcon"]' ,{ timeout: 5000 }).last().click()
    cy.contains("Text 2").should('exist')
   // cy.wait(2000) // wait for 2 seconds

    })

})