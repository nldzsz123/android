cmake_minimum_required(VERSION 3.4.1)
project(H264Player)
set(CMAKE_VERBOSE_MAKEFILE on)
set(libs ${CMAKE_SOURCE_DIR}/src/main/jniLibs)
set(CPP_DIC ${CMAKE_SOURCE_DIR}/src/main/cpp)
set(LIVE555_DIC ${CPP_DIC}/live555)
set(FFMPEG_DIC ${CPP_DIC}/ffmpeg)
set(RETRIVE_DIC ${CPP_DIC}/retrive)
set(FFMPEG_EXTRACTOR_DIR ${CPP_DIC}/ffmpegExtractor)
set(DECODER_DIR ${CPP_DIC}/Decoder)

aux_source_directory(${LIVE555_DIC}/BasicUsageEnvironment/ SRC_LIST_BasicUsageEnvironment)
aux_source_directory(${LIVE555_DIC}/groupsock/ SRC_LIST_groupsock)
aux_source_directory(${LIVE555_DIC}/liveMedia/ SRC_LIST_liveMedia)
aux_source_directory(${LIVE555_DIC}/ SRC_LIST_live555)
aux_source_directory(${RETRIVE_DIC}/ SRC_LIST_retrive)
aux_source_directory(${CPP_DIC}/ SRC_LIST)
aux_source_directory(${FFMPEG_EXTRACTOR_DIR} FFMPEG_EXTRACTOR_lib)
aux_source_directory(${DECODER_DIR} DECODER_lib)

include_directories(${LIVE555_DIC}/BasicUsageEnvironment)
include_directories(${LIVE555_DIC}/groupsock)
include_directories(${LIVE555_DIC}/liveMedia)
include_directories(${LIVE555_DIC}/)
include_directories(${FFMPEG_DIC}/)
include_directories(${RETRIVE_DIC}/)
include_directories(${CPP_DIC}/)
include_directories(${FFMPEG_EXTRACTOR_DIR})
include_directories(${DECODER_DIR})

find_library( # Sets the name of the path variable.
              log-lib

              # Specifies the name of the NDK library that
              # you want CMake to locate.
              log )

add_library(libffmpeg SHARED IMPORTED)
set_target_properties(libffmpeg PROPERTIES IMPORTED_LOCATION "${libs}/${ANDROID_ABI}/libffmpeg.so")

#add_library(libavfilter SHARED IMPORTED)
#set_target_properties(libavfilter PROPERTIES IMPORTED_LOCATION "${libs}/${ANDROID_ABI}/libavfilter.so")
#
#add_library(libavformat SHARED IMPORTED)
#set_target_properties(libavformat PROPERTIES IMPORTED_LOCATION "${libs}/${ANDROID_ABI}/libavformat.so")
#
#add_library(libavresample SHARED IMPORTED)
#set_target_properties(libavresample PROPERTIES IMPORTED_LOCATION "${libs}/${ANDROID_ABI}/libavresample.so")
#
#add_library(libavutil SHARED IMPORTED)
#set_target_properties(libavutil PROPERTIES IMPORTED_LOCATION "${libs}/${ANDROID_ABI}/libavutil.so")
#
#add_library(libpostproc SHARED IMPORTED)
#set_target_properties(libpostproc PROPERTIES IMPORTED_LOCATION "${libs}/${ANDROID_ABI}/libpostproc.so")
#
#add_library(libswresample SHARED IMPORTED)
#set_target_properties(libswresample PROPERTIES IMPORTED_LOCATION "${libs}/${ANDROID_ABI}/libswresample.so")
#
#add_library(libswscale SHARED IMPORTED)
#set_target_properties(libswscale PROPERTIES IMPORTED_LOCATION "${libs}/${ANDROID_ABI}/libswscale.so")

#add_library(libffmpeg SHARED IMPORTED)
#set_target_properties(libffmpeg PROPERTIES IMPORTED_LOCATION "${libs}/${ANDROID_ABI}/libffmpeg.so")

add_library(H264Player SHARED
    ${SRC_LIST_BasicUsageEnvironment}
    ${SRC_LIST_groupsock}
    ${SRC_LIST_liveMedia}
    ${SRC_LIST_live555}
    ${SRC_LIST_retrive}
    ${SRC_LIST}
        ${FFMPEG_EXTRACTOR_lib}
        ${DECODER_lib})
set(CMAKE_CXX_FLAGS "-std=c++11 -fexceptions -frtti -DNULL=0 -DSOCKLEN_T=socklen_t -DNO_SSTREAM -DBSD=1 -DNO_SSTREAM -fexceptions -DANDROID -DXLOCALE_NOT_USED")

if(${ANDROID_ABI} STREQUAL "armeabi-v7a")
    include_directories(${ANDROID_SYSROOT}/usr/include/arm-linux-androideabi)
elseif(${ANDROID_ABI} STREQUAL "arm64-v8a")
    include_directories(${ANDROID_SYSROOT}/usr/include/aarch64-linux-android)
else()
    include_directories(${ANDROID_SYSROOT}/usr/include/arm-linux-androideabi)
endif()

target_link_libraries(H264Player android log
#    libavcodec
#    libavformat
#        libavresample
#    libavutil
#        libpostproc
#    libswresample
#    libswscale
        libffmpeg
    ${log-lib}
    mediandk
   )