package com.huanli233.xtcinfomanager.xtcinfo

import com.huanli233.xtcinfomanager.utils.XTCKeyUtil
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException


class XtcinfoManager {
    fun runExtract(path: String) {
        // Check file
        val file = File(path)
        if (!(file.exists() && file.isFile())) {
            System.err.println("! xtcinfo 文件不存在或不是文件。")
            return
        }

        println("正在尝试从 xtcinfo 中提取 key。")

        // Start extract key
        val result = getSecurityKeyFromXtcInfo(file)
        if (result != null) {
            if (XTCKeyUtil.checkKey(result)) {
                println("提取成功。以下是提取内容: ")
                println(result)
            } else {
                println("提取完成，但经过检查发现提取结果不是正确的 key 格式，以下结果可能不正确，若不正确请检查你的 xtcinfo: ")
                println(result)
            }
        } else {
            println("提取失败。请检查你的 xtcinfo 文件是否有效或你是否确信其中保存了 key。")
        }
    }

    fun runWrite(path: String, content: String) {
        // Check file
        val file = File(path)
        if (!(file.exists() && file.isFile())) {
            System.err.println("! xtcinfo 文件不存在或不是文件。")
            return
        }

        if (!XTCKeyUtil.checkKey(content)) {
            println("! 输入的 key 不合法。")
            return
        }

        println("正在尝试写入 key。")

        val result = try {
            setSecurityKeyToXtcInfo(file, content)
        } catch (e: Exception) {
            println("发生未知错误：")
            e.printStackTrace()
            return
        }

        if (result) {
            println("写入成功！")
        } else {
            println("写入失败！")
        }
    }

    fun setSecurityKeyToXtcInfo(file: File, str: String): Boolean {
        val bArr = ByteArray(3608)
        try {
            val fileInputStream = FileInputStream(file)
            fileInputStream.read(bArr, 0, 3608)
            fileInputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        try {
            val fileOutputStream = FileOutputStream(file)
            val bytes = str.toByteArray()
            val calculateCRC = calculateCRC(bytes, bytes.size)
            bArr[2048] = (bytes.size and 255).toByte()
            bArr[2049] = ((bytes.size shr 8) and 255).toByte()
            bArr[2050] = ((bytes.size shr 16) and 255).toByte()
            bArr[2051] = ((bytes.size shr 24) and 255).toByte()
            bArr[2052] = (calculateCRC and 255).toByte()
            bArr[2053] = ((calculateCRC shr 8) and 255).toByte()
            bArr[2054] = ((calculateCRC shr 16) and 255).toByte()
            bArr[2055] = ((calculateCRC shr 24) and 255).toByte()
            System.arraycopy(bytes, 0, bArr, 2056, bytes.size)
            var i = 0
            while (i < 2) {
                i++
                System.arraycopy(
                    bArr,
                    2048,
                    bArr,
                    (i * 520) + 2048,
                    520
                )
            }
            fileOutputStream.write(bArr)
            fileOutputStream.close()
        } catch (e2: IOException) {
            e2.printStackTrace()
        }
        val securityKeyFromXtcInfo = getSecurityKeyFromXtcInfo(file)
        if (securityKeyFromXtcInfo == null || securityKeyFromXtcInfo != str) {
            return false
        }
        return true
    }


    private fun getSecurityKeyFromXtcInfo(file: File): String? {
        var str: String? = null
        var fileInputStream: FileInputStream? = null
        try {
            fileInputStream = FileInputStream(file)
            val bArr = ByteArray(3608)
            val zArr = BooleanArray(3)
            var i = -1
            fileInputStream.read(bArr, 0, 3608)
            fileInputStream.close()
            for (i2 in 0..2) {
                val i3 = (i2 * 520) + 2048
                zArr[i2] = false
                val i4 =
                    (((bArr[i3].toInt() and 255) or ((bArr[i3 + 1].toInt() and 255) shl 8)) or ((bArr[i3 + 2].toInt() and 255) shl 16)) or ((bArr[i3 + 3].toInt() and 255) shl 24)
                if (i4 <= 512) {
                    val i5 =
                        (((bArr[i3 + 4].toInt() and 255) or ((bArr[i3 + 5].toInt() and 255) shl 8)) or ((bArr[i3 + 6].toInt() and 255) shl 16)) or ((bArr[i3 + 7].toInt() and 255) shl 24)
                    val bArr2 = ByteArray(i4)
                    System.arraycopy(bArr, i3 + 8, bArr2, 0, i4)
                    val calculateCRC = calculateCRC(bArr2, i4)
                    if (i5 == calculateCRC) {
                        zArr[i2] = true
                        if (i == -1) {
                            str = String(bArr2)
                            i = i2
                        }
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        return str
    }

    private fun calculateCRC(bArr: ByteArray, i: Int): Int {
        var i2 = 65535
        for (i3 in 0 until i) {
            i2 = i2 xor (bArr[i3].toInt() shl 8)
            for (i4 in 0..7) {
                i2 = if ((32768 and i2) != 0) {
                    i2 shl 1 xor 4129
                } else {
                    i2 shl 1
                }
            }
        }
        return i2 and 65535
    }
}
