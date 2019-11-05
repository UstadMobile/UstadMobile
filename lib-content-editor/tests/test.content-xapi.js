/**
 * Test cases to make sure when editor is in preview mode and edit mode behaves as expected
 */
chai.should();
describe('#XAPI Create statement)', function () {
    describe('givenPreviewIsOn_whenQuestionIsAnswered_thenXapiStatementShouldBeCreated', function () {
        it('Xapi Statement was created', function () {
            $($(".um-editor").find(".question-choice-body").get(0)).click()
            setTimeout(() => {
                //assert that new statmemt was added
                const expectedNumberOfStatement = 1;
                expectedNumberOfStatement.should.equal(UmXapiManager.xapiStatement.length);
            }, TEST_CASE_TIMEOUT);
        });
    });
});
