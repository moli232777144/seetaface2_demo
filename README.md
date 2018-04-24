
---
# 前言

  seetaface的开源降低了人脸识别的门槛，时隔两年，seeta升级开源了seetaface2的sdk包，该教程主要实现了检测搭建和相关基础测试；

---

# 主要环境：

- git
- cmake
- vs2015
- android studio

---

下载本工程相关测试代码：

```
git clone https://github.com/moli232777144/seetaface2_demo.git
```

# win端测试


1. 移至win目录下；

2. 下载[seetaface2](https://github.com/seetaface/SeetaFaceEngine2)的[模型文件](https://pan.baidu.com/s/1HJj8PEnv3SOu6ZxVpAHPXg)，解压到本工程的bindata目录下；
3. 运行tools目录下的winBuild脚本，生成vs2015工程；
4. 运行VS2015目录的seetaface2_demo工程，编译x64的Release版本；
5. 运行时缺少的dll文件，从3rdparty的bin目录拷贝至vs2015的release下；


---

# 安卓端测试


1. 移至android目录下；

2. 下载[seetaface2](https://github.com/seetaface/SeetaFaceEngine2)的[模型文件](https://pan.baidu.com/s/1HJj8PEnv3SOu6ZxVpAHPXg)，解压到本工程的bindata目录下，复制bindata目录至手机SD卡下；
3. Anroid studio编译范例工程；

