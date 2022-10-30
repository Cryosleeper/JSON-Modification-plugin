package com.cryosleeper.gradle.jsonmod

import org.gradle.testkit.runner.GradleRunner
import spock.lang.Specification
import spock.lang.TempDir

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class JsonModificationPluginTest extends Specification {
    @TempDir
    File testProjectDir
    File buildFile

    def setup() {
        buildFile = new File(testProjectDir, 'build.gradle')
        buildFile << """
            plugins {
                id 'com.cryosleeper.gradle.json-modification'
            }
        """
    }

    def "One input"() {
        given:
        File input = new File(testProjectDir, 'input.json')
        input << '{"key1": "old value 1", "key2": "old value 2", "key3": {"inner_key": "old value 3"}}'
        File diff = new File(testProjectDir, 'diff.json')
        diff << '{"key1":  "value1", "key2": "value2", "key3.inner_key": "value3"}'
        String output = 'output.json'

        buildFile << """
            modifyJsons {
                modification {
                    input = file('${input.getName()}')
                    diffs = [file('${diff.getName()}')]
                    output = file('$output')
                }
            }
        """

        when:
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir)
                .withArguments('modifyJsons')
                .withPluginClasspath()
                .build()

        then:
        result.output.contains('{"key1":"value1","key2":"value2","key3":{"inner_key":"value3"}}')
        result.task(':modifyJsons').outcome == SUCCESS

        File outputFile = new File(testProjectDir, output)
        outputFile.text == '{"key1":"value1","key2":"value2","key3":{"inner_key":"value3"}}'
    }

    def "Three inputs"() {
        given:
        File input1 = new File(testProjectDir, 'input1.json')
        input1 << '{"key1": "old value 1", "key2": "old value 2", "key3": {"inner_key": "old value 3"}}'
        File diff1 = new File(testProjectDir, 'diff1.json')
        diff1 << '{"key1":  "value1", "key2": "value2", "key3.inner_key": "value3"}'
        String output1 = 'output1.json'

        File input2 = new File(testProjectDir, 'input2.json')
        input2 << '{"root": {"firstKey": "first value", "secondKey": "second value", "thirdKey": { "inner-1": "inner-value-1", "inner-2": "inner-value-2" }, "fourthKey": [1, "2", true] }}'
        File diff2 = new File(testProjectDir, 'diff2.json')
        diff2 << '{"root.firstKey":  "new first value", "root.thirdKey.inner-2": "new-inner-value-2", "root.fourthKey[0]": "new 0", "root.fourthKey[1]": 222, "root.fourthKey[2]": false}'
        String output2 = 'output2.json'

        File input3 = new File(testProjectDir, 'input3.json')
        input3 << '{"key1": "old value 1", "key2": "old value 2", "key3": {"inner_key": "old value 3"}}'
        File diff3 = new File(testProjectDir, 'diff3.json')
        diff3 << '{"key1": 1}'
        String output3 = 'output3.json'

        buildFile << """
            modifyJsons {
                modification {
                    input = file('${input1.getName()}')
                    diffs = [file('${diff1.getName()}')]
                    output = file('$output1')
                }
                
                modification {
                    input = file('${input2.getName()}')
                    diffs = [file('${diff2.getName()}')]
                    output = file('$output2')
                }
                
                modification {
                    input = file('${input3.getName()}')
                    diffs = [file('${diff3.getName()}')]
                    output = file('$output3')
                }
            }
        """

        when:
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir)
                .withArguments('modifyJsons')
                .withPluginClasspath()
                .build()

        then:
        result.output.contains('{"key1":"value1","key2":"value2","key3":{"inner_key":"value3"}}')
        result.output.contains('{"root":{"firstKey":"new first value","secondKey":"second value","thirdKey":{"inner-1":"inner-value-1","inner-2":"new-inner-value-2"},"fourthKey":["new 0",222,false]}}')
        result.output.contains('{"key1":1,"key2":"old value 2","key3":{"inner_key":"old value 3"}}')
        result.task(':modifyJsons').outcome == SUCCESS

        File resultFile1 = new File(testProjectDir, output1)
        resultFile1.text == '{"key1":"value1","key2":"value2","key3":{"inner_key":"value3"}}'
        File resultFile2 = new File(testProjectDir, output2)
        resultFile2.text == '{"root":{"firstKey":"new first value","secondKey":"second value","thirdKey":{"inner-1":"inner-value-1","inner-2":"new-inner-value-2"},"fourthKey":["new 0",222,false]}}'
        File resultFile3 = new File(testProjectDir, output3)
        resultFile3.text == '{"key1":1,"key2":"old value 2","key3":{"inner_key":"old value 3"}}'
    }

    def "Three diffs on a single input"() {
        given:
        File input = new File(testProjectDir, 'input1.json')
        input << '{"key1": "old value 1", "key2": "old value 2", "key3": {"inner_key": "old value 3"}}'
        File diff1 = new File(testProjectDir, 'diff1.json')
        diff1 << '{"key1":  "value2"}'
        File diff2 = new File(testProjectDir, 'diff2.json')
        diff2 << '{"key1":  "value1", "key2": "value2"}'
        File diff3 = new File(testProjectDir, 'diff3.json')
        diff3 << '{"key3.inner_key": "value3"}'
        String output = 'output3.json'

        buildFile << """
            modifyJsons {
                modification {
                    input = file('${input.getName()}')
                    diffs = [file('${diff1.getName()}'), file('${diff2.getName()}'), file('${diff3.getName()}')]
                    output = file('$output')
                }
            }
        """

        when:
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir)
                .withArguments('modifyJsons')
                .withPluginClasspath()
                .build()

        then:
        result.output.contains('{"key1":"value1","key2":"value2","key3":{"inner_key":"value3"}}')
        result.task(':modifyJsons').outcome == SUCCESS

        File outputFile = new File(testProjectDir, output)
        outputFile.text == '{"key1":"value1","key2":"value2","key3":{"inner_key":"value3"}}'
    }

    def "One input and no diff files"() {
        given:
        File input = new File(testProjectDir, 'input1.json')
        input << '{"key1": "old value 1", "key2": "old value 2", "key3": {"inner_key": "old value 3"}}'
        String output = 'output3.json'

        buildFile << """
            modifyJsons {
                modification {
                    input = file('${input.getName()}')
                    diffs = []
                    output = file('$output')
                }
            }
        """

        when:
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir)
                .withArguments('modifyJsons')
                .withPluginClasspath()
                .build()

        then:
        result.output.contains('{"key1":"old value 1","key2":"old value 2","key3":{"inner_key":"old value 3"}}')
        result.task(':modifyJsons').outcome == SUCCESS

        File outputFile = new File(testProjectDir, output)
        outputFile.text == '{"key1":"old value 1","key2":"old value 2","key3":{"inner_key":"old value 3"}}'
    }

    def "One input and an empty diff file"() {
        given:
        File input = new File(testProjectDir, 'input.json')
        input << '{"key1": "old value 1", "key2": "old value 2", "key3": {"inner_key": "old value 3"}}'
        File diff = new File(testProjectDir, 'diff.json')
        diff << '{}'
        String output = 'output.json'

        buildFile << """
            modifyJsons {
                modification {
                    input = file('${input.getName()}')
                    diffs = [file('${diff.getName()}')]
                    output = file('$output')
                }
            }
        """

        when:
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir)
                .withArguments('modifyJsons')
                .withPluginClasspath()
                .build()

        then:
        result.output.contains('{"key1":"old value 1","key2":"old value 2","key3":{"inner_key":"old value 3"}}')
        result.task(':modifyJsons').outcome == SUCCESS

        File outputFile = new File(testProjectDir, output)
        outputFile.text == '{"key1":"old value 1","key2":"old value 2","key3":{"inner_key":"old value 3"}}'
    }

    def "One input and a diff file that contains mistakes"() {
        given:
        File input = new File(testProjectDir, 'input.json')
        input << '{"key1": "old value 1", "key2": "old value 2", "key3": {"inner_key": "old value 3"}}'
        File diff = new File(testProjectDir, 'diff.json')
        diff << '{"wrongkey": "some value", "key1": {"key": "value"}, "key2": "new value 2"}'
        String output = 'output.json'

        buildFile << """
            modifyJsons {
                modification {
                    input = file('${input.getName()}')
                    diffs = [file('${diff.getName()}')]
                    output = file('$output')
                }
            }
        """

        when:
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir)
                .withArguments('modifyJsons')
                .withPluginClasspath()
                .build()

        then:
        result.output.contains('Modification failed for key wrongkey with com.jayway.jsonpath.PathNotFoundException')
        result.output.contains('Modification failed for key key1 due to using an unsupported value type')
        result.output.contains('{"key1":"old value 1","key2":"new value 2","key3":{"inner_key":"old value 3"}}')
        result.task(':modifyJsons').outcome == SUCCESS

        File outputFile = new File(testProjectDir, output)
        outputFile.text == '{"key1":"old value 1","key2":"new value 2","key3":{"inner_key":"old value 3"}}'
    }

    def "Delete item"() {
        given:
        File input = new File(testProjectDir, 'input.json')
        input << '{"key1": "value1", "key2": "value2"}'
        File diff = new File(testProjectDir, 'diff.json')
        diff << '{"key1":  null}'
        String output = 'output.json'

        buildFile << """
            modifyJsons {
                allowDelete = true
                modification {
                    input = file('${input.getName()}')
                    diffs = [file('${diff.getName()}')]
                    output = file('$output')
                }
            }
        """

        when:
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir)
                .withArguments('modifyJsons')
                .withPluginClasspath()
                .build()

        then:
        result.output.contains('{"key2":"value2"}')
        result.task(':modifyJsons').outcome == SUCCESS

        File outputFile = new File(testProjectDir, output)
        outputFile.text == '{"key2":"value2"}'
    }

    def "Item deletion is blocked"() {
        given:
        File input = new File(testProjectDir, 'input.json')
        input << '{"key1": "value1", "key2": "value2"}'
        File diff = new File(testProjectDir, 'diff.json')
        diff << '{"key1":  null}'
        String output = 'output.json'

        buildFile << """
            modifyJsons {
                modification {
                    input = file('${input.getName()}')
                    diffs = [file('${diff.getName()}')]
                    output = file('$output')
                }
            }
        """

        when:
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir)
                .withArguments('modifyJsons')
                .withPluginClasspath()
                .build()

        then:
        result.output.contains('Deletion failed for key key1 - deletion forbidden!')
        result.output.contains('{"key1":"value1","key2":"value2"}')
        result.task(':modifyJsons').outcome == SUCCESS

        File outputFile = new File(testProjectDir, output)
        outputFile.text == '{"key1":"value1","key2":"value2"}'
    }

    def "Diff with different JsonPath formats"() {
        given:
        File input = new File(testProjectDir, 'input.json')
        input << '{"key1": "old value 1", "key2": "old value 2", "key3": {"inner_key": "old value 3"}, "key4": [1, "2", true]}'
        File diff = new File(testProjectDir, 'diff.json')
        diff << '{"$.key1":  "value1", "[\'key2\']": "value2", "$[\'key3\'].[\'inner_key\']": "value3", "\$[\'key4\'][2]": false}'
        String output = 'output.json'

        buildFile << """
            modifyJsons {
                modification {
                    input = file('${input.getName()}')
                    diffs = [file('${diff.getName()}')]
                    output = file('$output')
                }
            }
        """

        when:
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir)
                .withArguments('modifyJsons')
                .withPluginClasspath()
                .build()

        then:
        result.output.contains('{"key1":"value1","key2":"value2","key3":{"inner_key":"value3"},"key4":[1,"2",false]}')
        result.task(':modifyJsons').outcome == SUCCESS

        File outputFile = new File(testProjectDir, output)
        outputFile.text == '{"key1":"value1","key2":"value2","key3":{"inner_key":"value3"},"key4":[1,"2",false]}'
    }
}
