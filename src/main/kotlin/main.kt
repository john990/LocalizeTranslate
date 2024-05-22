import com.sapiens.localize.translate.doc.reorganizeFiles
import com.sapiens.localize.translate.env.ConvertedStrings
import com.sapiens.localize.translate.env.Env
import com.sapiens.localize.translate.translate.TranslateTask
import com.sapiens.localize.translate.utils.logd
import com.sapiens.localize.translate.utils.logi
import com.sapiens.localize.translate.utils.toCommand


fun main(args: Array<String>) {
    args.forEach { logd("args", it) }

    reorganizeFiles(args[0])

    // 只是整理翻译文件，不执行翻译任务
    if (args.contains("-reorganize")) {
        return
    }

    logd("", args.toCommand())

    val command = args.toCommand()

    Env.setupCommand(command)

    val strings = ConvertedStrings.read(command.file)

    TranslateTask(command, strings).execute()
}
