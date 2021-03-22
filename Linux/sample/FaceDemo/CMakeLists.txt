cmake_minimum_required(VERSION 3.10)

project(FaceSDKDemo)
set (CMAKE_CXX_FLAGS  " -std=c++11 -O3 -Wall -fno-stack-protector -fopenmp")

find_package(OpenCV REQUIRED)

link_directories(libs)

add_executable(FaceSDKDemo src/demo.cpp src/tenginekit_api.h)
target_link_libraries(
    FaceSDKDemo
    TengineKitSDK
    ${OpenCV_LIBS})