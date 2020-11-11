cmake_minimum_required(VERSION 3.10)

project(FaceSDKDemo)
set (CMAKE_CXX_FLAGS  " -std=c++11 -O3 -Wall -fno-stack-protector -fopenmp")

find_package(OpenCV REQUIRED)

link_directories(libs)
set(CMAKE_RUNTIME_OUTPUT_DIRECTORY_DEBUG ${PROJECT_SOURCE_DIR}/bin/)
set(CMAKE_RUNTIME_OUTPUT_DIRECTORY_RELEASE ${PROJECT_SOURCE_DIR}/bin/)

add_executable(FaceSDKDemo src/demo.cpp src/tenginekit_api.h)
target_link_libraries(
    FaceSDKDemo
    TengineKitSDK
    ${OpenCV_LIBS})