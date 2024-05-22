
# Quickly Translate Your Android App with AI!

## Usage
To translate your Android app's strings.xml file using AI, run the following command:

```bash
java -jar LocalizeTranslate.jar your_path\strings.xml -server=openai -exeMode=incremental -lang=fr,ja
```

## Parameters

### `-server`
Specifies the translation service to use. Options are `openai` or `azure`. Configuration is required in the `env.json` file.

### `-exeMode`
Defines the execution mode. Options are `incremental` for incremental updates or `renew` for full updates.

### `-lang`
Specifies the languages to translate into, separated by commas.

### `-reorganize`
Reorganizes all translated files, removes unnecessary lines, and generates a new `strings_converted.json` file.


## Automatically created files
### `strings_converted.json`
According to string.xml, it is automatically created. You can edit the prompt field in it to make the AI translation more accurate.

### `env.json`
When running for the first time, env.json will be created. Please edit this file and fill in the corresponding key and URL.

--- 
By following these instructions, you can efficiently translate your Android app into multiple languages using AI services.

Feel free to modify any sections to better suit your needs or to add more detailed instructions as necessary.