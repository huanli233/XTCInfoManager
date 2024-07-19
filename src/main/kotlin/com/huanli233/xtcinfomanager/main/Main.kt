package com.huanli233.xtcdecrypter.main

import com.huanli233.utils.FileUtil
import org.apache.log4j.Level
import org.apache.log4j.Logger

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        println("XTCDecrypter V1.1.0")
        println("https://github.com/huanli233/XTCDecrypter")


        // Extract from xtcinfo mode
        if (args.size == 2 && args[0] == "-img") {
            XtcinfoExtractor().run(args[1])
            return
        }


        // Init File
        val soFile: File = File("libxtcSecurity.so")
        if (!soFile.exists()) {
            FileUtil.writeInputStreamToFile(soFile, DecrypterMain::class.java.getResourceAsStream("/libxtcSecurity.so"))
        }
        if (!soFile.exists()) {
            System.err.println("无法初始化 SO 文件。Unable to init so file.")
            return
        }


        // Configure logger
        BasicConfigurator.configure()
        Logger.getRootLogger().level = Level.OFF


        // Get input data
        var input: String
        input = if (args.size == 0) {
            InputUtil.promptUser("请输入备份的数据中，“selfRsaPublicKey=”等于号后面一直到下一个逗号前的内容：\n")
        } else if (args.size == 2 && args[0] == "-f") {
            try {
                FileUtil.readFileToString(args[1])
            } catch (e: Exception) {
                System.err.println("读取文件发生了错误 Error: ")
                e.printStackTrace()
                return
            }
        } else {
            args[0]
        }


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
    }
}
