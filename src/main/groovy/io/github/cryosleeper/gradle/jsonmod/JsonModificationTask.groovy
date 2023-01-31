package io.github.cryosleeper.gradle.jsonmod

import com.jayway.jsonpath.Configuration
import com.jayway.jsonpath.DocumentContext
import com.jayway.jsonpath.JsonPath
import com.jayway.jsonpath.spi.json.JacksonJsonProvider
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

abstract class JsonModificationTask extends DefaultTask {
    @Input
    abstract Property<Boolean> getIsDeleting()
    @Input
    abstract Property<Boolean> getIsAdding()
    @Internal
    List<Modification> modifications

    JsonModificationTask() {}

    @TaskAction
    def modify() {
        for (def it : modifications) {
            if (it.input == null) {
                System.err.println('Modification without input file was encountered!')
                continue
            }
            if (it.output == null) {
                System.err.println('Modification without output file was encountered!')
                continue
            }
            if (!it.input.exists()) {
                System.err.println("Input file ${it.input.name} was not found!")
                continue
            }
            Configuration configuration = Configuration.defaultConfiguration().jsonProvider(new JacksonJsonProvider())
            DocumentContext parsedInput = JsonPath.using(configuration).parse(it.input.text)
            for (def diff : it.diffs) {
                if (diff == null) { continue }
                if (!diff.exists()) {
                    System.err.println("Diff file ${diff.name} was not found!")
                    continue
                }
                JsonModTools.applyDiff(parsedInput, diff.text, isAdding.get(), isDeleting.get())
            }
            String result = parsedInput.jsonString()
            it.output.delete()
            it.output.write(result)
            println result
        }
    }
}
