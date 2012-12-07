#
# Jitsi, the OpenSource Java VoIP and Instant Messaging client.
#
# Distributable under LGPL license.
# See terms of license at gnu.org.
#
 
LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_LDLIBS    := -lEGL -lGLESv1_CM -llog
LOCAL_MODULE    := jnawtrenderer
LOCAL_SRC_FILES := JAWTRenderer_Android.c net_java_sip_communicator_impl_neomedia_jmfext_media_renderer_video_JAWTRenderer.c
include $(BUILD_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := jnffmpeg
LOCAL_SRC_FILES := libjnffmpeg.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := jng722
LOCAL_SRC_FILES := libjng722.so
include $(PREBUILT_SHARED_LIBRARY)

# include $(CLEAR_VARS)
# LOCAL_CFLAGS    := -I../android/platform/frameworks/base/include/media/stagefright/openmax
# LOCAL_LDLIBS    := -llog
# LOCAL_MODULE    := jnopenmax
# LOCAL_SRC_FILES := org_jitsi_impl_neomedia_codec_video_h264_OMXDecoder.c
# include $(BUILD_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := jnspeex
LOCAL_SRC_FILES := libjnspeex.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_LDLIBS    := -lOpenSLES -llog
LOCAL_MODULE    := jnopensles
LOCAL_SRC_FILES := org_jitsi_impl_neomedia_device_OpenSLESSystem.c org_jitsi_impl_neomedia_jmfext_media_protocol_opensles_DataSource.c org_jitsi_impl_neomedia_jmfext_media_renderer_audio_OpenSLESRenderer.c
include $(BUILD_SHARED_LIBRARY)

#include $(CLEAR_VARS)
#LOCAL_CFLAGS    := -I${ffmpeg} -D_XOPEN_SOURCE=600
#LOCAL_LDLIBS    := -L${ffmpeg}/libavcodec -L${ffmpeg}/libavfilter -L${ffmpeg}/libavformat -L${ffmpeg}/libavutil -L${ffmpeg}/libswscale -L${x264} -lavformat -lavcodec -lavfilter -lavutil -lswscale -lx264
#LOCAL_MODULE    := jnffmpeg
#LOCAL_SRC_FILES := org_jitsi_impl_neomedia_codec_FFmpeg.c
#include $(BUILD_SHARED_LIBRARY)

#include $(CLEAR_VARS)
#LOCAL_MODULE    := jnopus
#LOCAL_C_INCLUDES := opus/include opus/celt opus/silk
#include opus/opus_sources.mk
#LOCAL_SRC_FILES := $(OPUS_SOURCES:%=/opus/%)  org_jitsi_impl_neomedia_codec_audio_opus_Opus.c
#include $(BUILD_SHARED_LIBRARY)

