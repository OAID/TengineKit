cmake_minimum_required(VERSION 3.10)

project(${BUILD_FILE})
set (CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++11 -fdata-sections -fPIE -fstrict-aliasing -ffunction-sections -ffast-math -flax-vector-conversions -Wall -fsigned-char -fno-stack-protector -fopenmp -g")

set(distribution_DIR ${CMAKE_CURRENT_LIST_DIR}/libs)

find_package(OpenCV REQUIRED)
include_directories(${CMAKE_CURRENT_LIST_DIR}/src/include/)
include_directories(${CMAKE_CURRENT_LIST_DIR}/src/include/Opencv)

add_library(tenginelite SHARED IMPORTED)
set_target_properties(tenginelite PROPERTIES IMPORTED_LOCATION ${distribution_DIR}/libtengine-lite.so)

link_directories(libs)
set(CMAKE_RUNTIME_OUTPUT_DIRECTORY_DEBUG ${PROJECT_SOURCE_DIR}/bin/)
set(CMAKE_RUNTIME_OUTPUT_DIRECTORY_RELEASE ${PROJECT_SOURCE_DIR}/bin/)

file(GLOB srcpp src/*.cpp src/*.c)
add_executable(${BUILD_FILE} ${srcpp})

target_link_libraries(
    ${BUILD_FILE}
    ${OpenCV_LIBS}
    tenginelite)