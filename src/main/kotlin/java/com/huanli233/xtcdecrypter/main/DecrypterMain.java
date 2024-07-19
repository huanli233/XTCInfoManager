package com.huanli233.xtcdecrypter.main;

import java.io.File;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.huanli233.utils.FileUtil;
import com.huanli233.utils.InputUtil;
import com.huanli233.xtcdecrypter.extractor.XtcinfoExtractor;
import com.huanli233.xtcdecrypter.utils.XTCKeyUtil;

public class DecrypterMain {
	
	public static void main(String[] args) {
		System.out.println("XTCDecrypter V1.1.0");
		System.out.println("https://github.com/huanli233/XTCDecrypter");
		
		// Extract from xtcinfo mode
		if (args.length == 2 && args[0].equals("-img")) {
			new XtcinfoExtractor().run(args[1]);
			return;
		}
		
		// Init File
		File soFile = new File("libxtcSecurity.so");
		if (!soFile.exists()) {
			FileUtil.writeInputStreamToFile(soFile, DecrypterMain.class.getResourceAsStream("/libxtcSecurity.so"));
		}
		if (!soFile.exists()) {
			System.err.println("无法初始化 SO 文件。Unable to init so file.");
			return;
		}
		
		// Configure logger
		BasicConfigurator.configure();
		Logger.getRootLogger().setLevel(Level.OFF);
		
		// Get input data
		String input;
		if (args.length == 0) {
			input = InputUtil.promptUser("请输入备份的数据中，“selfRsaPublicKey=”等于号后面一直到下一个逗号前的内容：\n");
		} else if (args.length == 2 && args[0].equals("-f")) {
			try {
				input = FileUtil.readFileToString(args[1]);
			} catch (Exception e) {
				System.err.println("读取文件发生了错误 Error: ");
				e.printStackTrace();
				return;
			}
		} else {
			input = args[0];
		}
		
		// Handle input data
		if (input.contains("selfRsaPublicKey=")) {
		    input = input.replaceAll(".*selfRsaPublicKey=(.*?)(?:, |$)", "$1");
		}
		input.replace(", ", "");
		input.replace(" ", "");
		input.replace("selfRsaPublicKey=", "");
		
		// Decrypt data
		try {
			String result = EncryptUtilsJni.getInstance().decryptKey(input);
			if (XTCKeyUtil.checkKey(result)) {
				System.out.println("解密成功: ");
				System.out.println(result);
			} else {
				System.out.println("解密完成，但经过检查发现解密结果不是正确的 key 格式，以下结果可能不正确，若不正确请检查你的输入内容: ");
				System.out.println(result);
			}
		} catch (Exception e) {
			System.err.println("发生了错误 Error: ");
			e.printStackTrace();
		}
	}
	
}
