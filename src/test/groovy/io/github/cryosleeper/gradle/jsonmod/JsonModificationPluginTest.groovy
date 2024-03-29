package io.github.cryosleeper.gradle.jsonmod

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
                id 'io.github.cryosleeper.gradle.json-modification'
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
            jsonsToModify {
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
            jsonsToModify {
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
            jsonsToModify {
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
            jsonsToModify {
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
            jsonsToModify {
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
        diff << '{"wrongkey": "some value", "key2": "new value 2"}'
        String output = 'output.json'

        buildFile << """
            jsonsToModify {
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
        result.output.contains('Modification failed for key $[\'wrongkey\'] with com.jayway.jsonpath.PathNotFoundException')
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
            jsonsToModify {
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
            jsonsToModify {
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
        result.output.contains('Deletion failed for key $[\'key1\'] - deletion forbidden!')
        result.output.contains('{"key1":"value1","key2":"value2"}')
        result.task(':modifyJsons').outcome == SUCCESS

        File outputFile = new File(testProjectDir, output)
        outputFile.text == '{"key1":"value1","key2":"value2"}'
    }

    def "Add item"() {
        given:
        File input = new File(testProjectDir, 'input.json')
        input << '{"key1": "value1", "key2": {"innerKey": "innerValue", "innerKey3": {}}}'
        File diff = new File(testProjectDir, 'diff.json')
        diff << '{"key3":  "value3", "key2.innerKey2": "innerValue2", "key2.innerKey3.innerInnerKey": "innerInnerValue"}'
        String output = 'output.json'

        buildFile << """
            jsonsToModify {
                allowAdd = true
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
        result.output.contains('{"key1":"value1","key2":{"innerKey":"innerValue","innerKey3":{"innerInnerKey":"innerInnerValue"},"innerKey2":"innerValue2"},"key3":"value3"}')
        result.task(':modifyJsons').outcome == SUCCESS

        File outputFile = new File(testProjectDir, output)
        outputFile.text == '{"key1":"value1","key2":{"innerKey":"innerValue","innerKey3":{"innerInnerKey":"innerInnerValue"},"innerKey2":"innerValue2"},"key3":"value3"}'
    }

    def "Add empty object"() {
        given:
        File input = new File(testProjectDir, 'input.json')
        input << '{"oldKey":"oldValue"}'
        File diff = new File(testProjectDir, 'diff.json')
        diff << '{"keyObject":{}}'
        String output = 'output.json'

        buildFile << """
            jsonsToModify {
                allowAdd = true
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
        result.output.contains('{"oldKey":"oldValue","keyObject":{}}')
        result.task(':modifyJsons').outcome == SUCCESS

        File outputFile = new File(testProjectDir, output)
        outputFile.text == '{"oldKey":"oldValue","keyObject":{}}'
    }

    def "Add non-empty object"() {
        given:
        File input = new File(testProjectDir, 'input.json')
        input << '{"oldKey":"oldValue"}'
        File diff = new File(testProjectDir, 'diff.json')
        diff << '{"keyObject":{"field":"value"}}'
        String output = 'output.json'

        buildFile << """
            jsonsToModify {
                allowAdd = true
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
        result.output.contains('{"oldKey":"oldValue","keyObject":{"field":"value"}}')
        result.task(':modifyJsons').outcome == SUCCESS

        File outputFile = new File(testProjectDir, output)
        outputFile.text == '{"oldKey":"oldValue","keyObject":{"field":"value"}}'
    }

    def "Add object with subobject"() {
        given:
        File input = new File(testProjectDir, 'input.json')
        input << '{"oldKey":"oldValue"}'
        File diff = new File(testProjectDir, 'diff.json')
        diff << '{"keyObject":{"internal":{"field":"value"}}}'
        String output = 'output.json'

        buildFile << """
            jsonsToModify {
                allowAdd = true
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
        result.output.contains('{"oldKey":"oldValue","keyObject":{"internal":{"field":"value"}}}')
        result.task(':modifyJsons').outcome == SUCCESS

        File outputFile = new File(testProjectDir, output)
        outputFile.text == '{"oldKey":"oldValue","keyObject":{"internal":{"field":"value"}}}'
    }

    def "Add empty array"() {
        given:
        File input = new File(testProjectDir, 'input.json')
        input << '{"oldKey":"oldValue"}'
        File diff = new File(testProjectDir, 'diff.json')
        diff << '{"keyArray":[]}'
        String output = 'output.json'

        buildFile << """
            jsonsToModify {
                allowAdd = true
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
        result.output.contains('{"oldKey":"oldValue","keyArray":[]}')
        result.task(':modifyJsons').outcome == SUCCESS

        File outputFile = new File(testProjectDir, output)
        outputFile.text == '{"oldKey":"oldValue","keyArray":[]}'
    }

    def "Add non-empty array"() {
        given:
        File input = new File(testProjectDir, 'input.json')
        input << '{"oldKey":"oldValue"}'
        File diff = new File(testProjectDir, 'diff.json')
        diff << '{"keyArray":[{"key":"value"}]}'
        String output = 'output.json'

        buildFile << """
            jsonsToModify {
                allowAdd = true
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
        result.output.contains('{"oldKey":"oldValue","keyArray":[{"key":"value"}]}')
        result.task(':modifyJsons').outcome == SUCCESS

        File outputFile = new File(testProjectDir, output)
        outputFile.text == '{"oldKey":"oldValue","keyArray":[{"key":"value"}]}'
    }

    def "Add an item to an existing array"() {
        given:
        File input = new File(testProjectDir, 'input.json')
        input << '{"oldKey":"oldValue","keyArray":[0, 1]}'
        File diff = new File(testProjectDir, 'diff.json')
        diff << '{"keyArray[2]":2}'
        String output = 'output.json'

        buildFile << """
            jsonsToModify {
                allowAdd = true
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
        result.output.contains('{"oldKey":"oldValue","keyArray":[0,1,2]}')
        result.task(':modifyJsons').outcome == SUCCESS

        File outputFile = new File(testProjectDir, output)
        outputFile.text == '{"oldKey":"oldValue","keyArray":[0,1,2]}'
    }

    def "Add an array and an item to it"() {
        given:
        File input = new File(testProjectDir, 'input.json')
        input << '{"oldKey":"oldValue"}'
        File diff = new File(testProjectDir, 'diff.json')
        diff << '{"keyArray":[0, 1],"keyArray[2]":2}'
        String output = 'output.json'

        buildFile << """
            jsonsToModify {
                allowAdd = true
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
        result.output.contains('{"oldKey":"oldValue","keyArray":[0,1,2]}')
        result.task(':modifyJsons').outcome == SUCCESS

        File outputFile = new File(testProjectDir, output)
        outputFile.text == '{"oldKey":"oldValue","keyArray":[0,1,2]}'
    }

    def "Add an array and an item too far ahead to it"() {
        given:
        File input = new File(testProjectDir, 'input.json')
        input << '{"oldKey":"oldValue"}'
        File diff = new File(testProjectDir, 'diff.json')
        diff << '{"keyArray":[0, 1],"keyArray[10]":10}'
        String output = 'output.json'

        buildFile << """
            jsonsToModify {
                allowAdd = true
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
        result.output.contains('{"oldKey":"oldValue","keyArray":[0,1,null,null,null,null,null,null,null,null,10]}')
        result.task(':modifyJsons').outcome == SUCCESS

        File outputFile = new File(testProjectDir, output)
        outputFile.text == '{"oldKey":"oldValue","keyArray":[0,1,null,null,null,null,null,null,null,null,10]}'
    }

    def "Remove items from an array"() {
        given:
        File input = new File(testProjectDir, 'input.json')
        input << '{"arrayKey":[1,"2",true]}'
        File diff = new File(testProjectDir, 'diff.json')
        diff << '{"arrayKey[2]":  null, "arrayKey[1]": null, "arrayKey[0]": null}'
        String output = 'output.json'

        buildFile << """
            jsonsToModify {
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
        result.output.contains('{"arrayKey":[]}')
        result.task(':modifyJsons').outcome == SUCCESS

        File outputFile = new File(testProjectDir, output)
        outputFile.text == '{"arrayKey":[]}'
    }

    def "Diff with different JsonPath formats"() {
        given:
        File input = new File(testProjectDir, 'input.json')
        input << '{"key1": "old value 1", "key2": "old value 2", "key3": {"inner_key": "old value 3"}, "key4": [1, "2", true]}'
        File diff = new File(testProjectDir, 'diff.json')
        diff << '{"$.key1":  "value1", "[\'key2\']": "value2", "$.[\'key3\'][\'inner_key\']": "value3", "$.[\'key4\'][2]": false}'
        String output = 'output.json'

        buildFile << """
            jsonsToModify {
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

    def "Diff with different JsonPath formats, adding allowed"() {
        given:
        File input = new File(testProjectDir, 'input.json')
        input << '{"key1": "old value 1", "key2": "old value 2", "key3": {"inner_key": "old value 3"}, "key4": [1, "2", true]}'
        File diff = new File(testProjectDir, 'diff.json')
        diff << '{"$.key1":  "value1", "[\'key2\']": "value2", "$[\'key3\'][\'inner_key\']": "value3", "$[\'key4\'][2]": false}'
        String output = 'output.json'

        buildFile << """
            jsonsToModify {
                allowAdd = true
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

    def "No file for input"() {
        given:
        File diff = new File(testProjectDir, 'diff.json')
        diff << '{"key":"value"}'
        String output = 'output.json'

        buildFile << """
            jsonsToModify {
                allowAdd = true
                modification {
                    input = file('input.json')
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
        result.output.contains('Input file input.json was not found!')
        result.task(':modifyJsons').outcome == SUCCESS
    }

    def "Null file for input"() {
        given:
        File diff = new File(testProjectDir, 'diff.json')
        diff << '{"key":"value"}'
        String output = 'output.json'

        buildFile << """
            jsonsToModify {
                allowAdd = true
                modification {
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
        result.output.contains('Modification without input file was encountered!')
        result.task(':modifyJsons').outcome == SUCCESS
    }

    def "One input bugged"() {
        given:
        File input1 = new File(testProjectDir, 'input1.json')
        input1 << '{}'
        File input3 = new File(testProjectDir, 'input3.json')
        input3 << '{}'
        File diff = new File(testProjectDir, 'diff.json')
        diff << '{"key":"value"}'
        String output1 = 'output1.json'
        String output2 = 'output2.json'
        String output3 = 'output3.json'

        buildFile << """
            jsonsToModify {
                allowAdd = true
                modification {
                    input = file('${input1.name}')
                    diffs = [file('${diff.getName()}')]
                    output = file('$output1')
                }
                modification {
                    input = file('input2.json')
                    diffs = [file('${diff.getName()}')]
                    output = file('$output2')
                }
                modification {
                    input = file('${input3.name}')
                    diffs = [file('${diff.getName()}')]
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
        File outputFile1 = new File(testProjectDir, output1)
        outputFile1.text == '{"key":"value"}'
        File outputFile3 = new File(testProjectDir, output3)
        outputFile3.text == '{"key":"value"}'

        result.output.contains('Input file input2.json was not found!')
        result.task(':modifyJsons').outcome == SUCCESS
    }

    def "No file for diff"() {
        given:
        File input = new File(testProjectDir, 'input.json')
        input << '{"key":"value"}'
        String output = 'output.json'

        buildFile << """
            jsonsToModify {
                allowAdd = true
                modification {
                    input = file('${input.getName()}')
                    diffs = [file('diff.json')]
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
        File outputFile = new File(testProjectDir, output)
        outputFile.text == '{"key":"value"}'

        result.output.contains('Diff file diff.json was not found!')
        result.task(':modifyJsons').outcome == SUCCESS
    }

    def "Having one diff bugged"() {
        given:
        File input = new File(testProjectDir, 'input.json')
        input << '{"key":"value"}'
        File diff1 = new File(testProjectDir, "diff1.json")
        diff1 << '{"key1":"value1"}'
        File diff3 = new File(testProjectDir, "diff3.json")
        diff3 << '{"key3":"value3"}'
        String output = 'output.json'

        buildFile << """
            jsonsToModify {
                allowAdd = true
                modification {
                    input = file('${input.getName()}')
                    diffs = [file('${diff1.name}'), file('diff2.json'), file('${diff3.name}')]
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
        File outputFile = new File(testProjectDir, output)
        outputFile.text == '{"key":"value","key1":"value1","key3":"value3"}'

        result.output.contains('Diff file diff2.json was not found!')
        result.task(':modifyJsons').outcome == SUCCESS
    }

    def "Null file for output"() {
        given:
        File input = new File(testProjectDir, 'input.json')
        input << "{}"
        File diff = new File(testProjectDir, 'diff.json')
        diff << '{"key":"value"}'

        buildFile << """
            jsonsToModify {
                allowAdd = true
                modification {
                    input = file('${input.name}')
                    diffs = [file('${diff.getName()}')]
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
        result.output.contains('Modification without output file was encountered!')
        result.task(':modifyJsons').outcome == SUCCESS
    }

    def 'Multi variant test with no structure change'() {
        given:
        File input = new File(testProjectDir, 'input.json')
        input << inputValues[0]
        File diff = new File(testProjectDir, 'diff.json')
        diff << inputValues[1]
        String output = 'output.json'

        buildFile << """
            jsonsToModify {
                modification {
                    input = file('${input.getName()}')
                    diffs = [file('${diff.getName()}')]
                    output = file('$output')
                }
            }
        """

        expect:
        GradleRunner.create()
                .withProjectDir(testProjectDir)
                .withArguments('modifyJsons')
                .withPluginClasspath()
                .build()
        File outputFile = new File(testProjectDir, output)
        outputFile.text == inputValues[2]

        where:
        inputValues <<  [
                [
                        '{"key1":"value1"}',
                        '{"key2": "value2"}',
                        '{"key1":"value1"}'
                ],
                [
                        '{"key1":[null]}',
                        '{"key1[0]":"5"}',
                        '{"key1":["5"]}'
                ],
                [
                        '{"key1":["5"]}',
                        '{"key1[0]":null}',
                        '{"key1":["5"]}'
                ],
                [
                        '{"key1":[]}',
                        '{"key1[5]":"5","key2":[1, "2", true]}',
                        '{"key1":[]}'
                ],
                [
                        '{}',
                        '{"key1":[],"key1[1]":[],"key1[1][2]":["0"],"key1[1][2][3]":4}',
                        '{}'
                ],
                [
                        '{"key1": "value1", "objectKey":{"innerKey": "innerValue"}, "keyToDelete": 2}',
                        '{"key1": "new value 1", "objectKey.innerKey": "new inner value", "objectKey.innerObject": {"arrayKey": []}, "objectKey.innerObject.arrayKey[1]":true, "keyToDelete":null}',
                        '{"key1":"new value 1","objectKey":{"innerKey":"new inner value"},"keyToDelete":2}'
                ]
        ]
    }

    def 'Multi variant test with addition'() {
        given:
        File input = new File(testProjectDir, 'input.json')
        input << inputValues[0]
        File diff = new File(testProjectDir, 'diff.json')
        diff << inputValues[1]
        String output = 'output.json'

        buildFile << """
            jsonsToModify {
                allowAdd = true
                modification {
                    input = file('${input.getName()}')
                    diffs = [file('${diff.getName()}')]
                    output = file('$output')
                }
            }
        """

        expect:
        GradleRunner.create()
                .withProjectDir(testProjectDir)
                .withArguments('modifyJsons')
                .withPluginClasspath()
                .build()
        File outputFile = new File(testProjectDir, output)
        outputFile.text == inputValues[2]

        where:
        inputValues <<  [
                            [
                                    '{"key1":"value1"}',
                                    '{"key2": "value2"}',
                                    '{"key1":"value1","key2":"value2"}'
                            ],
                            [
                                    '{"key1":[]}',
                                    '{"key1[5]":"5","key2":[1, "2", true]}',
                                    '{"key1":[null,null,null,null,null,"5"],"key2":[1,"2",true]}'
                            ],
                            [
                                    '{}',
                                    '{"key1":[],"key1[0]":[],"key1[0][0]":[],"key1[0][0][0]":0}',
                                    '{"key1":[[[0]]]}'
                            ],
                            [
                                    '{}',
                                    '{"key1":[],"key1[1]":[],"key1[1][2]":[],"key1[1][2][3]":4}',
                                    '{"key1":[null,[null,null,[null,null,null,4]]]}'
                            ],
                            [
                                    '{}',
                                    '{"key1":[],"key1[1]":[],"key1[1][2]":["0"],"key1[1][2][3]":4}',
                                    '{"key1":[null,[null,null,["0",null,null,4]]]}'
                            ],
//                            [
//                                    '{"key1": "value1", "objectKey":{"innerKey": "innerValue"}, "keyToDelete": 2}',
//                                    '{"key1": "new value 1", "objectKey.innerKey": "new inner value", "objectKey.innerObject": {"arrayKey": []}, "objectKey.innerObject.arrayKey[1]":true, "keyToDelete":null}',
//                                    '{"key1":"new value 1","objectKey":{"innerKey":"new inner value","innerObject":{"arrayKey":[null,true]}},"keyToDelete":2}'
//                            ],
                            [
                                    '{"key1": "value1", "objectKey":{"innerKey": "innerValue"}, "keyToDelete": 2}',
                                    '{"key1": "new value 1", "objectKey.innerKey": "new inner value", "objectKey.innerObject": {"arrayKey": [null, true]}, "keyToDelete":null}',
                                    '{"key1":"new value 1","objectKey":{"innerKey":"new inner value","innerObject":{"arrayKey":[null,true]}},"keyToDelete":2}'
                            ]
                        ]
    }

    def 'Multi variant test with deletion'() {
        given:
        File input = new File(testProjectDir, 'input.json')
        input << inputValues[0]
        File diff = new File(testProjectDir, 'diff.json')
        diff << inputValues[1]
        String output = 'output.json'

        buildFile << """
            jsonsToModify {
                modification {
                    allowDelete = true
                    input = file('${input.getName()}')
                    diffs = [file('${diff.getName()}')]
                    output = file('$output')
                }
            }
        """

        expect:
        GradleRunner.create()
                .withProjectDir(testProjectDir)
                .withArguments('modifyJsons')
                .withPluginClasspath()
                .build()
        File outputFile = new File(testProjectDir, output)
        outputFile.text == inputValues[2]

        where:
        inputValues <<  [
                [
                        '{"key1":"value1"}',
                        '{"key2": "value2"}',
                        '{"key1":"value1"}'
                ],
                [
                        '{"key1":[null]}',
                        '{"key1[0]":"5"}',
                        '{"key1":["5"]}'
                ],
                [
                        '{"key1":["5"]}',
                        '{"key1[0]":null}',
                        '{"key1":[]}'
                ],
                [
                        '{"key1": "value1", "objectKey":{"innerKey": "innerValue"}, "keyToDelete": 2}',
                        '{"key1": "new value 1", "objectKey.innerKey": "new inner value", "objectKey.innerObject": {"arrayKey": []}, "objectKey.innerObject.arrayKey[1]":true, "keyToDelete":null}',
                        '{"key1":"new value 1","objectKey":{"innerKey":"new inner value"}}'
                ]
        ]
    }

    def 'Multi variant test with full modification'() {
        given:
        File input = new File(testProjectDir, 'input.json')
        input << inputValues[0]
        File diff = new File(testProjectDir, 'diff.json')
        diff << inputValues[1]
        String output = 'output.json'

        buildFile << """
            jsonsToModify {
                modification {
                    allowAdd = true
                    allowDelete = true
                    input = file('${input.getName()}')
                    diffs = [file('${diff.getName()}')]
                    output = file('$output')
                }
            }
        """

        expect:
        GradleRunner.create()
                .withProjectDir(testProjectDir)
                .withArguments('modifyJsons')
                .withPluginClasspath()
                .build()
        File outputFile = new File(testProjectDir, output)
        outputFile.text == inputValues[2]

        where:
        inputValues <<  [
                [
                        '{"key1":"value1"}',
                        '{"key1":null,"key2": "value2"}',
                        '{"key2":"value2"}'
                ],
                [
                        '{"key1":[]}',
                        '{"key1[0]":"5"}',
                        '{"key1":["5"]}'
                ],
                [
                        '{"key1":["5"]}',
                        '{"key1[0]":null}',
                        '{"key1":[]}'
                ],
//                [
//                        '{"key1": "value1", "objectKey":{"innerKey": "innerValue"}, "keyToDelete": 2}',
//                        '{"key1": "new value 1", "objectKey.innerKey": "new inner value", "objectKey.innerObject": {"arrayKey": []}, "objectKey.innerObject.arrayKey[1]":true, "keyToDelete":null}',
//                        '{"key1":"new value 1","objectKey":{"innerKey":"new inner value","innerObject":{"arrayKey":[null,true]}}}'
//                ],
                [
                        '{"key1": "value1", "objectKey":{"innerKey": "innerValue"}, "keyToDelete": 2}',
                        '{"key1": "new value 1", "objectKey.innerKey": "new inner value", "objectKey.innerObject": {"arrayKey": [null, true]}, "keyToDelete":null}',
                        '{"key1":"new value 1","objectKey":{"innerKey":"new inner value","innerObject":{"arrayKey":[null,true]}}}'
                ]
        ]
    }
}
