package java.com.huanli233.xtcdecrypter.main

import com.github.unidbg.AndroidEmulator
import com.github.unidbg.Emulator
import com.github.unidbg.Module
import com.github.unidbg.arm.backend.BackendFactory
import com.github.unidbg.arm.backend.DynarmicFactory
import com.github.unidbg.arm.backend.Unicorn2Factory
import com.github.unidbg.file.linux.AndroidFileIO
import com.github.unidbg.linux.ARM32SyscallHandler
import com.github.unidbg.linux.android.AndroidARMEmulator
import com.github.unidbg.linux.android.AndroidResolver
import com.github.unidbg.linux.android.dvm.*
import com.github.unidbg.memory.SvcMemory
import com.github.unidbg.unix.UnixSyscallHandler
import java.io.File
import java.io.IOException
import java.util.*

class EncryptUtilsJni(soFilePath: String?, classPath: String?) : AbstractJni() {
    // ARM模拟器
    private val emulator: AndroidEmulator = object : AndroidARMEmulator(
        File(soFilePath).name,
        File("en/rootfs"),
        Arrays.asList<BackendFactory>(DynarmicFactory(true), Unicorn2Factory(true))
    ) {
        override fun createSyscallHandler(svcMemory: SvcMemory): UnixSyscallHandler<AndroidFileIO> {
            return MyARMSyscallHandler(svcMemory)
        }
    }

    // vm
    private val vm: VM

    // 载入的模块
    @Suppress("unused")
    private val module: Module

    private val TTEncryptUtils: DvmClass

    private class MyARMSyscallHandler(svcMemory: SvcMemory) : ARM32SyscallHandler(svcMemory) {
        override fun fork(emulator: Emulator<*>): Int {
            return emulator.pid
        }
    }

    /**
     * 调用so文件中的指定函数
     * @param methodSign 传入你要执行的函数信息，需要完整的smali语法格式的函数签名
     * @param args       是即将调用的函数需要的参数
     * @return 函数调用结果
     */
    private fun myJni(methodSign: String, vararg args: Any): String {
        // 使用jni调用传入的函数签名对应的方法（）
        val ret = TTEncryptUtils.callStaticJniMethodObject<DvmObject<*>>(emulator, methodSign, *args)

        // ret存放返回调用结果存放的地址，获得函数执行后返回值
        return ret.value.toString()
    }

    /**
     * 关闭模拟器
     * @throws IOException
     */
    @Suppress("unused")
    @Throws(IOException::class)
    private fun destroy() {
        emulator.close()
        println("emulator destroy...")
    }

    fun decryptKey(key: String): String {
        var key = key
        key = key.replace("&lt;", "<").replace("&gt;", ">")
        if (key.startsWith("KeyEncrypt")) {
            key = key.substring(10)
        }
        return myJni(methodSign, key)
    }

    fun encryptKey(key: String?): String {
        return "KeyEncrypt" + myJni(methodSign1, key!!)
    }

    /**
     *
     * @param soFilePath   需要执行的so文件路径
     * @param classPath    需要执行的函数所在的Java类路径
     * @throws IOException
     */
    init {
        val memory = emulator.memory
        memory.setLibraryResolver(AndroidResolver(23))
        vm = (emulator as AndroidARMEmulator).createDalvikVM()
        val dm = vm.loadLibrary(File(soFilePath), false)
        dm.callJNI_OnLoad(emulator)
        module = dm.module
        TTEncryptUtils = vm.resolveClass(classPath)
    }

    companion object {
        const val methodSign: String = "secretKeyDecrypt(Ljava/lang/String;)Ljava/lang/String;"
        const val methodSign1: String = "secretKeyEncrypt(Ljava/lang/String;)Ljava/lang/String;"
        var instance: EncryptUtilsJni? = null
        fun getInstance(): EncryptUtilsJni? {
            if (instance == null) {
                val soFilePath = "libxtcSecurity.so"
                val classPath = "com/xtc/utils/security/XtcSecurity"
                try {
                    instance = EncryptUtilsJni(soFilePath, classPath)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            return instance
        }
    }
}