// config.h - Ring Racers Android build configuration
// Generated for Android NDK build

#ifndef __CONFIG_H__
#define __CONFIG_H__

// Version info
#define SRB2VERSION "2"
#define SRB2VERSIONSTRING "v2.3"
#define VERSIONSTRING "v2.3"
#define VERSIONSTRINGW L"v2.3"

// Build info used by comptime.c
#define SRB2_COMP_BRANCH "android-port"
#define SRB2_COMP_REVISION "android"
#define SRB2_COMP_NOTE ""
#define CMAKE_BUILD_TYPE "Release"
#define SRB2_COMP_OPTIMIZED 1

// Legacy defines
#define COMPREVISION "android"
#define COMPBRANCH "android-port"
#define COMPNOTE ""
#define COMPDATE __DATE__
#define COMPTIME __TIME__

// Asset hashes (empty for Android)
#define ASSET_HASH_BIOS_PK3 ""
#define ASSET_HASH_CHARS_PK3 ""
#define ASSET_HASH_MAPS_PK3 ""
#define ASSET_HASH_TEXTURES_PK3 ""

// Git info
#define COMPGITBRANCH ""
#define COMPGITHASH ""

// Stub for OpenGL variables when NOHW is defined
#ifdef NOHW
// cv_glshearing is used in p_user.c but only for OpenGL mode
// Create a dummy struct to prevent compile errors
typedef struct { int value; } consvar_stub_t;
static const consvar_stub_t cv_glshearing = {0};
#endif

#endif // __CONFIG_H__

