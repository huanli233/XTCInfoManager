package java.com.huanli233.xtcdecrypter.utils

import java.util.regex.Pattern

object XTCKeyUtil {
    fun checkKey(key: String?): Boolean {
        val regex = "^[a-zA-Z0-9]{32}:[a-zA-Z0-9=+/]{128}$"
        val pattern = Pattern.compile(regex)
        val matcher = pattern.matcher(key)
        return matcher.matches()
    }
}
