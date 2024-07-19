# XTCInfoManager

Management tool for XTC's SelfRsaPpublicKey

可从字符串对桌面数据库中的 key 进行解密或对 xtcinfo 中的 key 进行读写。

是 [XTCDecrypter](https://github.com/huanli233/XTCDecrypter) 的 Kotlin 版本（原项目现不再维护）

# Usage
````
Decrypt from string:
xim str
xim str <your_key>
xim str -f <file_contains_key>
Manage xtcinfo img:
xim img dec <path_to_img_file>
xim img wrt <path_to_img_file> <your_key>
````