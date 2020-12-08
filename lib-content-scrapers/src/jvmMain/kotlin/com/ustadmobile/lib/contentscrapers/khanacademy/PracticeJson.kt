package com.ustadmobile.lib.contentscrapers.khanacademy

class PracticeJson {

    var taskJson: TaskJson? = null

    class TaskJson {

        var reservedItems: List<String> = listOf()

    }

}

class PracticeTask {

    var data: Data? = null

    class Data {

        var getOrCreatePracticeTask: Task? = null

        class Task {

            var result: Result? = null

            class Result {

                var userTask: UserTask? = null

                class UserTask {

                    var task: PracticeJson.TaskJson? = null

                }

            }

        }

    }

}