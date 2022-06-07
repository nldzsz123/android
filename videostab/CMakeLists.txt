cmake_minimum_required(VERSION 3.4.1)
project(VideoStab)
set(CMAKE_VERBOSE_MAKEFILE on)
set(libs ${CMAKE_SOURCE_DIR}/src/main/jniLibs)
set(CPP_DIC ${CMAKE_SOURCE_DIR}/src/main/cpp)

set(SRC_LIST ${CPP_DIC}/Videostab.cpp ${CPP_DIC}/VideoStabBridge.cpp)

add_library(VideoStab SHARED
        ${SRC_LIST}
        )

include_directories(${CPP_DIC})
find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log )

add_library(libopencv_world SHARED IMPORTED)
set_target_properties(libopencv_world PROPERTIES IMPORTED_LOCATION "${libs}/${ANDROID_ABI}/libopencv_world.so")


set(CMAKE_CXX_FLAGS "-std=c++11 -fexceptions -frtti -DNULL=0 -DANDROID -DXLOCALE_NOT_USED")
if(${ANDROID_ABI} STREQUAL "armeabi-v7a")
    include_directories(${ANDROID_SYSROOT}/usr/include/arm-linux-androideabi)
elseif(${ANDROID_ABI} STREQUAL "arm64-v8a")
    include_directories(${ANDROID_SYSROOT}/usr/include/aarch64-linux-android)
else()
    include_directories(${ANDROID_SYSROOT}/usr/include/arm-linux-androideabi)
endif()
target_link_libraries(VideoStab android log
        libopencv_world
        )