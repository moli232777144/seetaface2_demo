#1.cmake verson，指定cmake的最小版本号 
cmake_minimum_required(VERSION 2.8)

#2.project name，指定项目的名称，一般和项目的文件夹名称对应
project(seetaface2_demo C CXX)

#3.set environment variable，设置环境变量
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++11")

#4.include头文件目录 
include_directories(${CMAKE_CURRENT_LIST_DIR}/3rdparty/include/Opencv
					${CMAKE_CURRENT_LIST_DIR}/3rdparty/include/Opencv/opencv
					${CMAKE_CURRENT_LIST_DIR}/3rdparty/include/Opencv/opencv2
					${CMAKE_CURRENT_LIST_DIR}/3rdparty/include/seeta
					${CMAKE_CURRENT_LIST_DIR}/3rdparty/include
                    ${CMAKE_CURRENT_LIST_DIR}/src)

#5.library目录及name名称
link_directories(${CMAKE_CURRENT_LIST_DIR}/3rdparty/lib
				 ${CMAKE_CURRENT_LIST_DIR}/3rdparty/lib/x64)
list(APPEND SEETA2_LINKER_LIBS opencv_world320 SeetaFaceDetector200 SeetaFaceRecognizer200 SeetaPointDetector200)

#6.source directory源文件目录
file(GLOB MTCNN_SRC ${CMAKE_CURRENT_LIST_DIR}/src/*.h
                    ${CMAKE_CURRENT_LIST_DIR}/src/*.cpp)
set(SEETA2_COMPILE_CODE ${MTCNN_SRC})

#7.1.add executable file，编译为可执行文件
add_executable(seetaface2_demo ${SEETA2_COMPILE_CODE})
#7.2.add library file，编译为动态库
# add_library(seetaface2_demo SHARED ${SEETA2_COMPILE_CODE})

#8.add link library，添加工程所依赖的库
target_link_libraries(seetaface2_demo ${SEETA2_LINKER_LIBS})