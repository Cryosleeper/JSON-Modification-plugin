package io.github.cryosleeper.gradle.jsonmod

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option

abstract class SingleJsonModificationTask extends DefaultTask {

    String input
    String diff
    String outputFileName

    @Input
    String getInput() {
        return input
    }

    @Option(option = "input", description = "String of JSON to modify")
    void setInput(String input) {
        this.input = input
    }

    @Input
    String getDiff() {
        return diff
    }

    @Option(option = "diff", description = "String of JSON to use as a diff")
    void setDiff(String diff) {
        this.diff = diff
    }

    @Input
    String getOutputFileName() {
        return outputFileName
    }

    @Option(option = "output", description = "Output file name and path")
    void setOutputFileName(String outputName) {
        this.outputFileName = outputName
    }

    @TaskAction
    def modify() {
        //TODO make multi file modification a chain of single modification tasks
        println("This function is not implemented yet")
    }
}
