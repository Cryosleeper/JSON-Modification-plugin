# JSON-Modification-plugin

This plugin allows you to make changes to JSON files as Gradle tasks. Changes to apply are described using [JsonPath](https://jsonpath.com/).

## Installation and configuration

To add the plugin to your project:
```
plugins {
    ...
    id("com.cryosleeper.gradle.json-modification") version "0.1.1"
}
```

To configure the conversion:
```
modifyJsons {
    modification {
        input = file('input.json')
        diffs = [file('diff1.json'), file('diff2.json'), file('diff3.json')] //more if needed
        output = file('output.json')
    }
    //more modifications if needed
}
```

## Input data format

Input files might be any valid JSON documents:
```
{
  "key1": "value1",
  "key2": "value2",
  "key3": {
    "innerKey": "innerValue"
  },
  "key4": [1, "2", true]
}
```

Diff files are a single level JSON files where keys are JsonPath and values are new values to apply on that path:
```
{
  "key1": "new value 1",
  "key3.innerKey": "new inner value",
  "key4[0]": "1",
  "key4[1]": 2,
  "key4[2]": false
}
```
***Please note that at the moment those strings should not start with $.***

Multiple diffs might be applied, in which case they are used one by one in order of listing. Any illegal or unsupported change is skipped, with a corresponding message in a log.

Output file contains results of applying diffs to the input file:
```
{
  "key1": "new value 1",
  "key2": "value2",
  "key3": {
    "innerKey": "new inner value"
  },
  "key4": [
    "1",
    2,
    false
  ]
}
```
Output file doesn't need to exist initially. Output might be set to the same file as input, resulting in overwriting. If modification is impossible for some reason, the unchanged input file is written to the output.

## Additional options
You can list a _null_ value in the diff to delete an entry:
```
{
  "key2": null
}
```
By default it will fail with
> Deletion failed for key key2 - deletion forbidden!

To allow deleting, add _allowDelete_ to the config:
```
modifyJsons {
    allowDelete true
    modification {
        input = file('input.json')
        diffs = [file('diff.json')]
        output = file('output.json')
    }
}
```

## Acknowledgements

This plugin uses [Jayway JsonPath](https://github.com/json-path/JsonPath) and [Jackson](https://github.com/FasterXML/jackson). JsonPath [was proposed by Stefan Goessner](https://goessner.net/articles/JsonPath/)
