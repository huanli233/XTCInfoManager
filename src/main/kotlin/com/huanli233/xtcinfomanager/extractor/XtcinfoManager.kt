package com.huanli233.xtcinfomanager.extractor

import com.huanli233.xtcinfomanager.utils.XTCKeyUtil
import java.io.File
import java.io.FileInputStream
import java.io.IOException

class XtcinfoExtractor {
    fun run(path: String) {
        // Check file
        val file: File = File(path)
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
