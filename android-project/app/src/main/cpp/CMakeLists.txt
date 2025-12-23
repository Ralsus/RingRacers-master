# Ring Racers Android - Full Native Build
cmake_minimum_required(VERSION 3.18)
project(ringracers LANGUAGES C CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_C_STANDARD 11)

# Android NDK libraries
find_library(log-lib log)
find_library(android-lib android)
find_library(gles2-lib GLESv2)
find_library(egl-lib EGL)
find_library(z-lib z)

include(FetchContent)

message(STATUS "=== Ring Racers Android Build ===")
message(STATUS "ABI: ${ANDROID_ABI}")

#######################################
# SDL2
#######################################
set(SDL2_DIR "${CMAKE_CURRENT_SOURCE_DIR}/SDL2")
if(EXISTS "${SDL2_DIR}/CMakeLists.txt")
    message(STATUS "Found SDL2: ${SDL2_DIR}")
    set(SDL_SHARED ON CACHE BOOL "")
    set(SDL_STATIC OFF CACHE BOOL "")
    set(SDL_TEST OFF CACHE BOOL "")
    add_subdirectory(${SDL2_DIR} SDL2_build EXCLUDE_FROM_ALL)
    set(SDL2_LIBRARIES SDL2)
else()
    message(STATUS "Fetching SDL2...")
    FetchContent_Declare(SDL2
        GIT_REPOSITORY https://github.com/libsdl-org/SDL.git
        GIT_TAG release-2.28.5
        GIT_SHALLOW TRUE
    )
    set(SDL_SHARED ON CACHE BOOL "")
    set(SDL_STATIC OFF CACHE BOOL "")
    set(SDL_TEST OFF CACHE BOOL "")
    FetchContent_MakeAvailable(SDL2)
    set(SDL2_LIBRARIES SDL2)
endif()

#######################################
# Source paths
#######################################
set(SRB2_SRC "${CMAKE_CURRENT_SOURCE_DIR}/../../../../../src")
set(THIRDPARTY "${CMAKE_CURRENT_SOURCE_DIR}/../../../../../thirdparty")

if(NOT EXISTS "${SRB2_SRC}/doomdef.h")
    message(FATAL_ERROR "Ring Racers source not found: ${SRB2_SRC}")
endif()
message(STATUS "Source: ${SRB2_SRC}")

#######################################
# Collect source files
#######################################

# Core game files
file(GLOB CORE_C_SOURCES "${SRB2_SRC}/*.c")
file(GLOB CORE_CPP_SOURCES "${SRB2_SRC}/*.cpp")

# Remove files we don't need for Android
list(FILTER CORE_C_SOURCES EXCLUDE REGEX ".*apng\\.c$")
list(FILTER CORE_C_SOURCES EXCLUDE REGEX ".*d_clisrv\\.c$")
list(FILTER CORE_C_SOURCES EXCLUDE REGEX ".*d_netfil\\.c$")
list(FILTER CORE_C_SOURCES EXCLUDE REGEX ".*d_netcmd\\.c$")
list(FILTER CORE_C_SOURCES EXCLUDE REGEX ".*mserv\\.c$")
list(FILTER CORE_C_SOURCES EXCLUDE REGEX ".*http-mserv\\.c$")
list(FILTER CORE_C_SOURCES EXCLUDE REGEX ".*i_tcp\\.c$")
list(FILTER CORE_C_SOURCES EXCLUDE REGEX ".*m_perfstats\\.c$")   # Uses ps_hw_* when NOHW
list(FILTER CORE_C_SOURCES EXCLUDE REGEX ".*discord\\.c$")       # Discord RPC
list(FILTER CORE_CPP_SOURCES EXCLUDE REGEX ".*d_net\\.cpp$")
list(FILTER CORE_CPP_SOURCES EXCLUDE REGEX ".*stun\\.cpp$")

# SDL implementation
set(SDL_SOURCES
    ${SRB2_SRC}/sdl/i_system.cpp
    ${SRB2_SRC}/sdl/i_video.cpp
    ${SRB2_SRC}/sdl/i_main.cpp
    ${SRB2_SRC}/sdl/i_threads.c
    ${SRB2_SRC}/sdl/new_sound.cpp
    ${SRB2_SRC}/sdl/dosstr.c
    ${SRB2_SRC}/sdl/endtxt.c
)

# Lua (blua)
file(GLOB BLUA_SOURCES "${SRB2_SRC}/blua/*.c")

# Audio subsystem
file(GLOB AUDIO_SOURCES "${SRB2_SRC}/audio/*.c" "${SRB2_SRC}/audio/*.cpp")

# Core utilities
file(GLOB CORE_DIR_SOURCES "${SRB2_SRC}/core/*.c" "${SRB2_SRC}/core/*.cpp")

# ACS
file(GLOB ACS_SOURCES "${SRB2_SRC}/acs/*.c" "${SRB2_SRC}/acs/*.cpp")

# Objects
file(GLOB OBJECTS_SOURCES "${SRB2_SRC}/objects/*.c" "${SRB2_SRC}/objects/*.cpp")

# Menus
file(GLOB MENUS_SOURCES "${SRB2_SRC}/menus/*.c" "${SRB2_SRC}/menus/*.cpp")

# HUD
file(GLOB HUD_SOURCES "${SRB2_SRC}/hud/*.c" "${SRB2_SRC}/hud/*.cpp")

# Monocypher
file(GLOB MONOCYPHER_SOURCES "${SRB2_SRC}/monocypher/*.c")

# Modp_b64
file(GLOB MODP_SOURCES "${SRB2_SRC}/modp_b64/*.c")

# IO
file(GLOB IO_SOURCES "${SRB2_SRC}/io/*.c" "${SRB2_SRC}/io/*.cpp")

# JNI bridge
set(ANDROID_JNI_SOURCES
    ${CMAKE_CURRENT_SOURCE_DIR}/android_jni.cpp
    ${CMAKE_CURRENT_SOURCE_DIR}/touch_input.cpp
)

#######################################
# Create library
#######################################
add_library(ringracers SHARED
    ${CORE_C_SOURCES}
    ${CORE_CPP_SOURCES}
    ${SDL_SOURCES}
    ${BLUA_SOURCES}
    ${AUDIO_SOURCES}
    ${CORE_DIR_SOURCES}
    ${ACS_SOURCES}
    ${OBJECTS_SOURCES}
    ${MENUS_SOURCES}
    ${HUD_SOURCES}
    ${MONOCYPHER_SOURCES}
    ${MODP_SOURCES}
    ${IO_SOURCES}
    ${ANDROID_JNI_SOURCES}
)

#######################################
# Compile definitions
#######################################
target_compile_definitions(ringracers PRIVATE
    ANDROID
    LINUX
    UNIXCOMMON
    
    HAVE_SDL
    HAVE_MIXER
    SOUND=SOUND_MIXER
    DIRECTFULLSCREEN
    
    # Disable features for Android
    NONX86
    NOASM
    NOEXECINFO
    NOTERMIOS
    NOMD5
    NOHW
    NONET
    NOMUMBLE
    NOPNG
    NOVOICE
    NOCURL
    NOPOSTPROCESSING
    NO_DISCORD
    
    HAVE_ZLIB
    HAVE_THREADS
    
    CMAKECONFIG
    _LARGEFILE64_SOURCE
)

#######################################
# Include directories  
#######################################
target_include_directories(ringracers PRIVATE
    ${SRB2_SRC}
    ${SRB2_SRC}/blua
    ${SRB2_SRC}/sdl
    ${CMAKE_CURRENT_SOURCE_DIR}
    ${THIRDPARTY}/tcbrindle_span/include
    ${THIRDPARTY}/fmt/include
    ${THIRDPARTY}/nlohmann-json/include
    ${THIRDPARTY}/stb/include
    $<TARGET_PROPERTY:SDL2,INTERFACE_INCLUDE_DIRECTORIES>
)

#######################################
# Link libraries
#######################################
target_link_libraries(ringracers PRIVATE
    ${log-lib}
    ${android-lib}
    ${gles2-lib}
    ${egl-lib}
    ${z-lib}
    ${SDL2_LIBRARIES}
)

#######################################
# Compiler options
#######################################
target_compile_options(ringracers PRIVATE
    -fPIC
    -fexceptions
    -frtti
    -Wno-all
    -Wno-error
)

message(STATUS "Ring Racers Android configured")
