describe('WEB_003_002_add_module_text_blocks_and_perform_indent_hide_delete_actions', () => {
  before(() => {
    // Start Test Server
    cy.ustadStartTestServer(6000);
  });

  it('Admin add module and text blocks, then perform indent_hide_delete actions', () => {
    // Admin user login
    cy.ustadClearDbAndLogin('admin', 'testpass');

    // Add a new course
    cy.ustadAddCourse('Test Course Block');

    // Add module block
    cy.contains('button', 'Edit').click();
    cy.contains('Add block').click();
    cy.contains('Module').click();

    // Testing Module block - title as blank
    cy.contains('button', 'Done').click();
    cy.get('.MuiInputBase-colorPrimary.Mui-error').should('exist');
    cy.get('input[id="title"]').type('module 1');
    cy.contains('button', 'Done').click();
    cy.contains('module 1').should('be.visible');
    cy.contains('button', 'Save').click();

    // Add text block
    cy.contains('button', 'Edit').click();
    cy.contains('Add block').click();
    cy.contains('Text').click();

    // Testing Text block - title as blank
    cy.contains('button', 'Done').click();
    cy.get('.MuiInputBase-colorPrimary.Mui-error').should('exist');
    cy.get('input[id="title"]').type('text 1');
    cy.contains('button', 'Done').click();
    cy.contains('text 1').should('be.visible');

    cy.get('[aria-label="More options"]').eq(1).click(); // Click the second element

    // Hide the text block
    cy.contains('li', 'Hide').click();
    cy.contains('button', 'Save').click();
    cy.get('text 1').should('not.exist');

    // Unhide and Indent the text block
    cy.contains('button', 'Edit').click();
    cy.get('[aria-label="More options"]').eq(1).should('be.visible').click();
    cy.contains('li', 'Unhide').click();
    cy.get('[aria-label="More options"]').eq(1).should('be.visible').click();
    cy.contains('li', 'Indent').click();
    cy.contains('button', 'Save').click();

    // Add module block
    cy.contains('button', 'Edit').click();
    cy.ustadAddModuleBlock('module 2');

    // Add Assignment
    cy.contains('button', 'Edit').click();
    cy.contains('Add block').should('be.visible').click();
    cy.contains('Assignment').click();

    // Testing Assignment block - title as blank
    cy.contains('button', 'Done').click();
    cy.get('.MuiInputBase-colorPrimary.Mui-error').should('exist');
    cy.get('input[id="title"]').type('Assignment 1');
    cy.contains('button', 'Done').click();
    cy.contains('Assignment 1').should('be.visible');

    cy.get('[aria-label="More options"]').eq(3).should('be.visible').click();
    cy.contains('li', 'Unindent').click();
    cy.contains('button', 'Save').click();

    // Delete Assignment block
    cy.contains('button', 'Edit').click();
    cy.get('[aria-label="More options"]').eq(3).should('be.visible').click();
    cy.contains('li', 'Delete').click();
    cy.contains('button', 'Save').click();
    cy.contains('Assignment 1').should('not.exist');

    // Add Discussion board
    cy.contains('button', 'Edit').click();
    cy.contains('Add block').click();
    cy.contains('Discussion board').click();

    // Testing Discussion board - title as blank
    cy.contains('button', 'Done').click();
    cy.get('.MuiInputBase-colorPrimary.Mui-error').should('exist');
    cy.get('input[id="title"]').type('discussionTitle');
    cy.contains('button', 'Done').click();

    // Add Content Block
    cy.contains('Add block').click();
    cy.get('#add_content_block').click();
    cy.contains('Import from file').click();
    cy.get('input[type="file"]').selectFile('../test-files/content/Epub_Content1.epub', { force: true });
    cy.get('input[id="content_title"]').clear();
    cy.get('#actionBarButton').click();
    cy.get('.MuiInputBase-colorPrimary.Mui-error').should('exist');
    cy.get('input[id="content_title"]').click();
    cy.get('input[id="content_title"]').clear().type('Content_002', { timeout: 2000 });
    cy.get('#actionBarButton').click();
    cy.contains('button', 'Done').click();
    cy.contains('button', 'Save').click();
    cy.contains('Content_002').should('be.visible');
  });

  after(() => {
    // Stop Test Server after tests are complete
    cy.ustadStopTestServer();
  })
});
