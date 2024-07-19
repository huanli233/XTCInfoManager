package com.huanli233.xtcinfomanager.main

import com.huanli233.utils.FileUtil
import com.huanli233.utils.InputUtil
import com.huanli233.xtcinfomanager.xtcinfo.XtcinfoManager
import com.huanli233.xtcinfomanager.utils.XTCKeyUtil
import org.apache.log4j.BasicConfigurator
import org.apache.log4j.Level
import org.apache.log4j.Logger
import java.io.File

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        println("XTCInfoManager V1.2.0")
        println("https://github.com/huanli233/XTCInfoManager")

        // Init File
        val soFile = File("libxtcSecurity.so")
        if (!soFile.exists()) {
            FileUtil.writeInputStreamToFile(soFile, Main::class.java.getResourceAsStream("/libxtcSecurity.so")!!)
        }
        if (!soFile.exists()) {
            System.err.println("无法初始化 SO 文件。Unable to init so file.")
            return
        }

        // Configure logger
        BasicConfigurator.configure()
        Logger.getRootLogger().level = Level.OFF

        if (args.isEmpty()) {
            printHelp()
        } else if (args[0] == "str") {
            var input: String? = null
            if (args.size == 1) {
                input = InputUtil.promptUser("请输入备份的数据中，“selfRsaPublicKey=”等于号后面一直到下一个逗号前的内容：\n")
            } else if (args.size == 2) {
                input = args[1]
            } else if (args.size == 3 && args[1] == "-f") {
                input = try {
                    FileUtil.readFileToString(args[1])
                } catch (e: Exception) {
                    System.err.println("读取文件发生了错误 Error: ")
                    e.printStackTrace()
                    return
                }
            }
            if (input != null) {
                // Handle input data
                if (input.contains("selfRsaPublicKey=")) {
                    input = input.replace(".*selfRsaPublicKey=(.*?)(?:, |$)".toRegex(), "$1")
                }
                input.replace(", ", "")
                input.replace(" ", "")
                input.replace("selfRsaPublicKey=", "")

                // Decrypt data
                try {
                    val result: String = EncryptUtilsJni.getInstance().decryptKey(input)
                    if (XTCKeyUtil.checkKey(result)) {
                        println("解密成功: ")
                        println(result)
                    } else {
                        println("解密完成，但经过检查发现解密结果不是正确的 key 格式，以下结果可能不正确，若不正确请检查你的输入内容: ")
                        println(result)
                    }
                } catch (e: Exception) {
                    System.err.println("发生了错误 Error: ")
                    e.printStackTrace()
                }
            } else {
                printHelp()
            }
        } else if (args[0] == "img") {
            if (args.size < 2) {
                printHelp()
            } else if (args[1] == "ext" && args.size == 3) {
                XtcinfoManager().runExtract(args[2])
            } else if (args[1] == "wrt" && args.size == 4) {
                XtcinfoManager().runWrite(args[2], args[3])
            } else {
                printHelp()
            }
        } else {
            printHelp()
        }
    }

    private fun printHelp() {
        println("Decrypt from string: ")
        println("  xim str")
        println("  xim str <your_key>")
        println("  xim str -f <file_contains_key>")
        println("Manage xtcinfo img: ")
        println("  xim img ext <path_to_img_file>")
        println("  xim img wrt <path_to_img_file> <your_key>")
    }
}
