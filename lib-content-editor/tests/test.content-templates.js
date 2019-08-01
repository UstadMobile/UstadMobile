/**
 * Tes cases to make sure ustadmobile blankDocument templates works as expected
 * 1. Insert Multiple choice questions
 * 2. Insert in fill the blanks questions
 * 3. Insert multimedia blankDocument.
 */

chai.should();
describe('#ContentTemplates', function() {

    describe("-Questions",function () {

        describe('givenActiveEditor_whenMultipleChoiceTemplateIsSelected_thenShouldInsertMultipleChoiceNode', function() {
            it('Multi-choice question template node inserted', function() {
                UmEditorCore.insertMultipleChoiceWidget();
                setTimeout(() => {
                    const questionList = $('.um-editor').find('div[data-um-widget="multi-choice"]');
                    questionList.length.should.equal(1);
                },TEST_CASE_TIMEOUT);
            });
        });

        describe('givenActiveEditor_whenFillInTheBlanksTemplateIsSelected_thenShouldInsertFillInTheBlanksNode', function() {
            it('Fill in the blanks question template node inserted', function() {
                UmEditorCore.insertFillTheBlanksWidget();
                setTimeout(() =>{
                    const questionList = $('.um-editor').find('div[data-um-widget="fill-the-blanks"]');
                    questionList.length.should.equal(1);
                },TEST_CASE_TIMEOUT)
            });
        });
    });

    describe("-Multimedia",function () {

        describe('givenActiveEditor_whenMediaTypeImageIsSelected_thenShouldInsertImageNode', function() {
            it('Image node inserted', function() {
                let bannySource = "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/images/BigBuckBunny.jpg";
                UmEditorCore.insertMediaContent(bannySource,"image/jpg");
                setTimeout(() => {
                    const content = $('.um-editor').find('img');
                    const imageSrc = $(content).attr('src');
                    bannySource.should.equal(imageSrc);
                },TEST_CASE_TIMEOUT);
            });
        });

        describe('givenActiveEditor_whenMediaTypeAudioIsSelected_thenShouldInsertAudioNode', function() {
            it('Audio node inserted', function() {
                let audioSrc = "http://www.noiseaddicts.com/samples_1w72b820/280.mp3";
                UmEditorCore.insertMediaContent(audioSrc,"audio/mp3");
                setTimeout(() => {
                    const content = $('.um-editor').find('video.media-audio');
                    const audioUrl = $(content).find('Source:first').attr('src');
                    audioSrc.should.equal(audioUrl);
                },TEST_CASE_TIMEOUT);
            });
        });

        describe('#givenActiveEditor_whenMediaTypeVideoIsSelected_thenShouldInsertVideoNode', function() {
            it('Video node inserted', function() {

                let bannySource = "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4";
                UmEditorCore.insertMediaContent(bannySource,"video/mp4");
                setTimeout(() => {
                    const content = $('.um-editor').find('video.um-media');
                    const videoUrl = $(content).find('Source:first').attr('src');
                    bannySource.should.equal(videoUrl);
                },TEST_CASE_TIMEOUT);
            });
        });
    });


});