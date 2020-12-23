# Tengine-Lite NPU 

# 介绍
## 功能 
在khadas板子上运行人脸检测及人脸关键点

## 性能
人脸检测 ：< 1ms
人脸关键点 ： < 1ms

## 所需内容
- khadas板子一块
- khadas npu sdk[如何获取](#获取khadas npu sdk)

# 快速使用
## 编译
你可以在```Khadas/sample/Face```文件夹下：
```bash
rm -rf ./build
mkdir build
cd build

rm -rf ./bin
mkdir bin

build_file="Face"

cmake ../ \
-DBUILD_FILE=$build_file \
-DCMAKE_BUILD_TYPE=Release

make -j4
```
或者直接运行```make.sh```bash文件
```bash
./make.sh
```

## 运行
运行测试图片：
```
./bin/Face ./resources/4.jpg
```

## 运行结果
![Output](sample/Face/resources/FaceOutput.jpg)

## 获取khads npu sdk
1. ) 跳转到[khads官网](https://www.khadas.cn/)
2. ) 联系客服要npu，sdk
3. ) 放到khadas板子位置，并export VIVANTE_SDK_DIR=$sdk路径