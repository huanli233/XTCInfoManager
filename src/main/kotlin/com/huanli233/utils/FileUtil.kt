package com.huanli233.utils

import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths

object FileUtil {
    fun readFileToString(filePath: String?): String {
        var content = ""
        try {
            content = String(Files.readAllBytes(Paths.get(filePath)), StandardCharsets.UTF_8)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return content
    }

    /**
     * 从InputStream写入到文件
     * @param filePath 目标文件
     * @param inputStream InputStream
     */
    fun writeInputStreamToFile(filePath: File?, inputStream: InputStream) {
        try {
            FileOutputStream(filePath).use { outputStream ->
                val buffer = ByteArray(1024)
                var length: Int
                while ((inputStream.read(buffer).also { length = it }) != -1) {
                    outputStream.write(buffer, 0, length)
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}
