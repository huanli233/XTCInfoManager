package com.huanli233.xtcdecrypter.main;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import com.github.unidbg.AndroidEmulator;
import com.github.unidbg.Emulator;
import com.github.unidbg.Module;
import com.github.unidbg.arm.backend.DynarmicFactory;
import com.github.unidbg.arm.backend.Unicorn2Factory;
import com.github.unidbg.file.linux.AndroidFileIO;
import com.github.unidbg.linux.ARM32SyscallHandler;
import com.github.unidbg.linux.android.AndroidARMEmulator;
import com.github.unidbg.linux.android.AndroidResolver;
import com.github.unidbg.linux.android.dvm.*;
import com.github.unidbg.memory.Memory;
import com.github.unidbg.memory.SvcMemory;
import com.github.unidbg.unix.UnixSyscallHandler;

public class EncryptUtilsJni extends AbstractJni {
    // ARM模拟器
    private final AndroidEmulator emulator;
    // vm
    private final VM vm;
    // 载入的模块
    @SuppressWarnings("unused")
	private final Module module;

    private final DvmClass TTEncryptUtils;

    private static class MyARMSyscallHandler extends ARM32SyscallHandler {
        private MyARMSyscallHandler(SvcMemory svcMemory) {
            super(svcMemory);
        }
        @Override
        protected int fork(Emulator<?> emulator) {
            return emulator.getPid();
        }
    }
    
    /**
     *
     * @param soFilePath   需要执行的so文件路径
     * @param classPath    需要执行的函数所在的Java类路径
     * @throws IOException
     */
    public EncryptUtilsJni(String soFilePath, String classPath) throws IOException {
        emulator = new AndroidARMEmulator(new File(soFilePath).getName(), new File("en/rootfs"), Arrays.asList(new DynarmicFactory(true), new Unicorn2Factory(true))) {
            @Override
            protected UnixSyscallHandler<AndroidFileIO> createSyscallHandler(SvcMemory svcMemory) {
                return new MyARMSyscallHandler(svcMemory);
            }};
        Memory memory = emulator.getMemory();
        memory.setLibraryResolver(new AndroidResolver(23));
        vm = ((AndroidARMEmulator) emulator).createDalvikVM();
        DalvikModule dm = vm.loadLibrary(new File(soFilePath), false);
        dm.callJNI_OnLoad(emulator);
        module = dm.getModule();
        TTEncryptUtils = vm.resolveClass(classPath);
    }

    /**
     * 调用so文件中的指定函数
     * @param methodSign 传入你要执行的函数信息，需要完整的smali语法格式的函数签名
     * @param args       是即将调用的函数需要的参数
     * @return 函数调用结果
     */
    private String myJni(String methodSign, Object ...args) {
        // 使用jni调用传入的函数签名对应的方法（）
        DvmObject<?> ret = TTEncryptUtils.callStaticJniMethodObject(emulator, methodSign, args);
        // ret存放返回调用结果存放的地址，获得函数执行后返回值
        
        return ret.getValue().toString();
    }

    /**
     * 关闭模拟器
     * @throws IOException
     */
    @SuppressWarnings("unused")
	private void destroy() throws IOException {
        emulator.close();
        System.out.println("emulator destroy...");
    }
    
    static final String methodSign = "secretKeyDecrypt(Ljava/lang/String;)Ljava/lang/String;";
    public String decryptKey(String key) {
    	key = key.replace("&lt;", "<").replace("&gt;", ">");
		if (key.startsWith("KeyEncrypt")) {
			key = key.substring(10);
		}
		return myJni(methodSign, key);
	} 
    
    static final String methodSign1 = "secretKeyEncrypt(Ljava/lang/String;)Ljava/lang/String;";
    public String encryptKey(String key) {
		return "KeyEncrypt" + myJni(methodSign1, key);
	}
    
    static EncryptUtilsJni instance;
    public static EncryptUtilsJni getInstance() {
    	if (instance == null) {
	        String soFilePath = "libxtcSecurity.so";
	        String classPath = "com/xtc/utils/security/XtcSecurity";
			try {
				instance = new EncryptUtilsJni(soFilePath, classPath);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
    	return instance;
    }
    
}