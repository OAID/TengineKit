cmake_minimum_required(VERSION 3.4.1)

option(TENGINE_KIT_DEBUG_IMAGE "use for tengine kit print pipeline yuv or rgb image" off)

unset(TENGINEKIT_SRCS)
list(APPEND TENGINEKIT_SRCS src/main/jni/core/manager.cpp)
list(APPEND TENGINEKIT_SRCS src/main/jni/core/faceService.cpp)
list(APPEND TENGINEKIT_SRCS src/main/jni/face/faceDetect.cpp)
list(APPEND TENGINEKIT_SRCS src/main/jni/face/faceLandmark.cpp)
list(APPEND TENGINEKIT_SRCS src/main/jni/face/faceLandmark3d.cpp)
list(APPEND TENGINEKIT_SRCS src/main/jni/face/eyeLandmark.cpp)
list(APPEND TENGINEKIT_SRCS src/main/jni/utils/utils.cpp)
list(APPEND TENGINEKIT_SRCS src/main/jni/face_engine_jni.cpp)
list(APPEND TENGINEKIT_SRCS src/main/jni/image/ImageResizeHelper.cpp)
list(APPEND TENGINEKIT_SRCS src/main/jni/image/RGBConverterHelper.cpp)
list(APPEND TENGINEKIT_SRCS src/main/jni/image/YuvConverterHelper.cpp)
list(APPEND TENGINEKIT_SRCS src/main/jni/image/ImageRotateHelper.cpp)
list(APPEND TENGINEKIT_SRCS src/main/jni/tengine/common/tengine_operations.c)

file(GLOB ARM64 src/main/jni/aarch64/*.S)
file(GLOB ARM32 src/main/jni/arm32/*.S)
if (CMAKE_SYSTEM_PROCESSOR MATCHES "^aarch64" OR ARCHS STREQUAL "arm64")
    list(APPEND TENGINEKIT_SRCS ${ARM64})
elseif (CMAKE_SYSTEM_PROCESSOR MATCHES "^armv7" OR ARCHS MATCHES "^armv7(;armv7s)?")
    list(APPEND TENGINEKIT_SRCS ${ARM32})
endif ()

add_library(
        tenginekit_engine
        SHARED
        ${TENGINEKIT_SRCS}
)

include_directories(./src/main/jni/tengine)
include_directories(./src/main/jni/utils)
include_directories(./src/main/jni/image)
include_directories(./src/main/jni/include)

add_library(tengine_lite SHARED IMPORTED)
set_target_properties(tengine_lite
        PROPERTIES IMPORTED_LOCATION
        ${CMAKE_CURRENT_SOURCE_DIR}/src/main/libs/${ANDROID_ABI}/libtengine-lite.so)


add_library(opencv_core SHARED IMPORTED)
set_target_properties(
        opencv_core
        PROPERTIES IMPORTED_LOCATION
        ${CMAKE_CURRENT_SOURCE_DIR}/src/main/libs/${ANDROID_ABI}/libopencv_core.so)

add_library(opencv_imgcodecs SHARED IMPORTED)
set_target_properties(
        opencv_imgcodecs
        PROPERTIES IMPORTED_LOCATION
        ${CMAKE_CURRENT_SOURCE_DIR}/src/main/libs/${ANDROID_ABI}/libopencv_imgcodecs.so)

add_library(opencv_imgproc SHARED IMPORTED)
set_target_properties(
        opencv_imgproc
        PROPERTIES IMPORTED_LOCATION
        ${CMAKE_CURRENT_SOURCE_DIR}/src/main/libs/${ANDROID_ABI}/libopencv_imgproc.so)

set(ANDROID_ARM_NEON ON)
set(CMAKE_ANDROID_ARM_MODE arm)
enable_language(ASM)
set_target_properties(tenginekit_engine PROPERTIES COMPILE_FLAGS -mfpu=neon)
set_target_properties(tenginekit_engine PROPERTIES COMPILE_FLAGS -O3)
#set(CMAKE_CXX_FLAGS "-std=c++11 ${CMAKE_CXX_FLAGS} -pthread")
#set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -fomit-frame-pointer -fstrict-aliasing -ffast-math")
#set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -fomit-frame-pointer -fstrict-aliasing -ffast-math")
#set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -fvisibility=hidden")
#set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -fvisibility=hidden -fvisibility-inlines-hidden")
## disable rtti and exceptions
#set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -fno-rtti -fno-exceptions")

find_library(log-lib log)


unset(TENGINEKIT_LIBS)
list(APPEND TENGINEKIT_LIBS android)
list(APPEND TENGINEKIT_LIBS tengine_lite)
list(APPEND TENGINEKIT_LIBS jnigraphics)
if (TENGINE_KIT_DEBUG_IMAGE)
    target_compile_definitions(tenginekit_engine PRIVATE $<$<OR:$<COMPILE_LANGUAGE:C>,$<COMPILE_LANGUAGE:CXX>>:DEBUG_IMAGE>)
    list(APPEND TENGINEKIT_LIBS opencv_core)
    list(APPEND TENGINEKIT_LIBS opencv_imgproc)
    list(APPEND TENGINEKIT_LIBS opencv_imgcodecs)
endif ()
list(APPEND TENGINEKIT_LIBS ${log-lib})

target_link_libraries( # Specifies the target library.
        tenginekit_engine
        ${TENGINEKIT_LIBS})

