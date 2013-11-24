LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := equations
LOCAL_SRC_FILES := equations.c

include $(BUILD_SHARED_LIBRARY)
