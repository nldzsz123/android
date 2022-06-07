# Sets the minimum version of CMake required to build the native
# library. You should either keep the default value or only pass a
# value of 3.4.0 or lower.

cmake_minimum_required(VERSION 3.4.1)
project(panoLib)
set(CMAKE_VERBOSE_MAKEFILE on)
set(libs "${CMAKE_SOURCE_DIR}/src/main/jniLibs")

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds it for you.
# Gradle automatically packages shared libraries with your APK.

set(SRC_DIR ${PROJECT_SOURCE_DIR}/src/main/cpp/)
set(SRC_LIST ${SRC_DIR}/CVWrapper.cpp ${SRC_DIR}/opencv.cpp)
add_library( # Sets the name of the library.
        panoLib

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        # Associated headers in the same location as their source
        # file are automatically included.
        ${SRC_LIST})

# Searches for a specified prebuilt library and stores the path as a
# variable. Because system libraries are included in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in the
# build script, prebuilt third-party libraries, or system libraries.

include_directories(${CMAKE_SOURCE_DIR}/src/main/cpp/include)

add_library(libopencv_bgsegm SHARED IMPORTED)
set_target_properties(libopencv_bgsegm PROPERTIES IMPORTED_LOCATION "${libs}/${ANDROID_ABI}/libopencv_bgsegm.so")

add_library(libopencv_calib3d SHARED IMPORTED)
set_target_properties(libopencv_calib3d PROPERTIES IMPORTED_LOCATION "${libs}/${ANDROID_ABI}/libopencv_calib3d.so")

add_library(libopencv_ccalib SHARED IMPORTED)
set_target_properties(libopencv_ccalib PROPERTIES IMPORTED_LOCATION "${libs}/${ANDROID_ABI}/libopencv_ccalib.so")

add_library(libopencv_core SHARED IMPORTED)
set_target_properties(libopencv_core PROPERTIES IMPORTED_LOCATION "${libs}/${ANDROID_ABI}/libopencv_core.so")

add_library(libopencv_features2d SHARED IMPORTED)
set_target_properties(libopencv_features2d PROPERTIES IMPORTED_LOCATION "${libs}/${ANDROID_ABI}/libopencv_features2d.so")

add_library(libopencv_flann SHARED IMPORTED)
set_target_properties(libopencv_flann PROPERTIES IMPORTED_LOCATION "${libs}/${ANDROID_ABI}/libopencv_flann.so")

add_library(libopencv_highgui SHARED IMPORTED)
set_target_properties(libopencv_highgui PROPERTIES IMPORTED_LOCATION "${libs}/${ANDROID_ABI}/libopencv_highgui.so")

add_library(libopencv_img_hash SHARED IMPORTED)
set_target_properties(libopencv_img_hash PROPERTIES IMPORTED_LOCATION "${libs}/${ANDROID_ABI}/libopencv_img_hash.so")

add_library(libopencv_imgcodecs SHARED IMPORTED)
set_target_properties(libopencv_imgcodecs PROPERTIES IMPORTED_LOCATION "${libs}/${ANDROID_ABI}/libopencv_imgcodecs.so")

add_library(libopencv_imgproc SHARED IMPORTED)
set_target_properties(libopencv_imgproc PROPERTIES IMPORTED_LOCATION "${libs}/${ANDROID_ABI}/libopencv_imgproc.so")

add_library(libopencv_ml SHARED IMPORTED)
set_target_properties(libopencv_ml PROPERTIES IMPORTED_LOCATION "${libs}/${ANDROID_ABI}/libopencv_ml.so")

add_library(libopencv_objdetect SHARED IMPORTED)
set_target_properties(libopencv_objdetect PROPERTIES IMPORTED_LOCATION "${libs}/${ANDROID_ABI}/libopencv_objdetect.so")

add_library(libopencv_photo SHARED IMPORTED)
set_target_properties(libopencv_photo PROPERTIES IMPORTED_LOCATION "${libs}/${ANDROID_ABI}/libopencv_photo.so")

add_library(libopencv_shape SHARED IMPORTED)
set_target_properties(libopencv_shape PROPERTIES IMPORTED_LOCATION "${libs}/${ANDROID_ABI}/libopencv_shape.so")

add_library(libopencv_stitching SHARED IMPORTED)
set_target_properties(libopencv_stitching PROPERTIES IMPORTED_LOCATION "${libs}/${ANDROID_ABI}/libopencv_stitching.so")

add_library(libopencv_superres SHARED IMPORTED)
set_target_properties(libopencv_superres PROPERTIES IMPORTED_LOCATION "${libs}/${ANDROID_ABI}/libopencv_superres.so")

add_library(libopencv_video SHARED IMPORTED)
set_target_properties(libopencv_video PROPERTIES IMPORTED_LOCATION "${libs}/${ANDROID_ABI}/libopencv_video.so")

add_library(libopencv_videoio SHARED IMPORTED)
set_target_properties(libopencv_videoio PROPERTIES IMPORTED_LOCATION "${libs}/${ANDROID_ABI}/libopencv_videoio.so")

add_library(libopencv_videostab SHARED IMPORTED)
set_target_properties(libopencv_videostab PROPERTIES IMPORTED_LOCATION "${libs}/${ANDROID_ABI}/libopencv_videostab.so")

add_library(libopencv_xfeatures2d SHARED IMPORTED)
set_target_properties(libopencv_xfeatures2d PROPERTIES IMPORTED_LOCATION "${libs}/${ANDROID_ABI}/libopencv_xfeatures2d.so")

add_library(libopencv_xobjdetect SHARED IMPORTED)
set_target_properties(libopencv_xobjdetect PROPERTIES IMPORTED_LOCATION "${libs}/${ANDROID_ABI}/libopencv_xobjdetect.so")

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=gnu++11 -fexceptions -frtti")

target_link_libraries(panoLib
        libopencv_bgsegm
        libopencv_calib3d
        libopencv_ccalib
        libopencv_core
        libopencv_features2d
        libopencv_flann
        libopencv_highgui
        libopencv_img_hash
        libopencv_imgcodecs
        libopencv_imgproc
        libopencv_ml
        libopencv_objdetect
        libopencv_photo
        libopencv_shape
        libopencv_stitching
        libopencv_superres
        libopencv_video
        libopencv_videoio
        libopencv_videostab
        libopencv_xfeatures2d
        libopencv_xobjdetect
        ${log-lib}
        )
