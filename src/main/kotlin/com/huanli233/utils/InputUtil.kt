package java.com.huanli233.utils

import java.util.*


object InputUtil {
    private val scanner = Scanner(System.`in`)

    fun promptUser(message: String?): String {
        print(message)
        var input = scanner.nextLine()
        if (input.startsWith("\"") || input.startsWith("'")) {
            input = input.substring(1)
        }
        if (input.endsWith("\"") || input.endsWith("'")) {
            input = input.substring(0, input.length - 1)
        }
        return input
    }

    fun promptUser(message: String?, mode: Int): String {
        print(message)
        return scanner.nextLine()
    }
}
