package com.huanli233.xtcdecrypter.extractor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import com.huanli233.xtcdecrypter.utils.XTCKeyUtil;

public class XtcinfoExtractor {
	public void run(String path) {
		// Check file
		File file = new File(path);
		if (!(file.exists() && file.isFile())) {
			System.err.println("! xtcinfo 文件不存在或不是文件。");
			return;
		}
		
		System.out.println("正在尝试从 xtcinfo 中提取 key。");
		
		// Start extract key
		String result = getSecurityKeyFromXtcInfo(file);
		if (result != null) {
			if (XTCKeyUtil.checkKey(result)) {
				System.out.println("提取成功。以下是提取内容: ");
				System.out.println(result);
			} else {
				System.out.println("提取完成，但经过检查发现提取结果不是正确的 key 格式，以下结果可能不正确，若不正确请检查你的 xtcinfo: ");
				System.out.println(result);
			}
		} else {
			System.out.println("提取失败。请检查你的 xtcinfo 文件是否有效或你是否确信其中保存了 key。");
		}
	}
	
	private String getSecurityKeyFromXtcInfo(File file) {
	    String str = null;
	    FileInputStream fileInputStream = null;
	    try {
	        fileInputStream = new FileInputStream(file);
	        byte[] bArr = new byte[3608];
	        boolean[] zArr = new boolean[3];
	        int i = -1;
	        fileInputStream.read(bArr, 0, 3608);
	        fileInputStream.close();
	        for (int i2 = 0; i2 < 3; i2++) {
	            int i3 = (i2 * 520) + 2048;
	            zArr[i2] = false;
	            int i4 = (((bArr[i3] & 255) | ((bArr[i3 + 1] & 255) << 8)) | ((bArr[i3 + 2] & 255) << 16)) | ((bArr[i3 + 3] & 255) << 24);
	            if (i4 <= 512) {
	                int i5 = (((bArr[i3 + 4] & 255) | ((bArr[i3 + 5] & 255) << 8)) | ((bArr[i3 + 6] & 255) << 16)) | ((bArr[i3 + 7] & 255) << 24);
	                byte[] bArr2 = new byte[i4];
	                System.arraycopy(bArr, i3 + 8, bArr2, 0, i4);
	                int calculateCRC = calculateCRC(bArr2, i4);
	                if (i5 == calculateCRC) {
	                    zArr[i2] = true;
	                    if (i == -1) {
	                        str = new String(bArr2);
	                        i = i2;
	                    }
	                }
	            }
	        }
	    } catch (IOException e) {
	        e.printStackTrace();
	        return null;
	    } finally {
	        if (fileInputStream != null) {
	            try {
	                fileInputStream.close();
	            } catch (IOException e) {
	                e.printStackTrace();
	            }
	        }
	    }
	    return str;
	}
	
	private int calculateCRC(byte[] bArr, int i) {
        int i2 = 65535;
        for (int i3 = 0; i3 < i; i3++) {
            i2 ^= bArr[i3] << 8;
            for (int i4 = 0; i4 < 8; i4++) {
                if ((32768 & i2) != 0) {
                    i2 = (i2 << 1) ^ 4129;
                } else {
                    i2 <<= 1;
                }
            }
        }
        return i2 & 65535;
    }

}
