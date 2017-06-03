LOCAL_MODULE    := libscrypt
LOCAL_MODULE_FILENAME := libscrypt
LOCAL_PATH := $(NDK_PROJECT_PATH)

LOCAL_SRC_FILES := $(wildcard $(LOCAL_PATH)/c/*.c)
LOCAL_C_INCLUDES := $(LOCAL_PATH)/include/

LOCAL_CFLAGS := -std=c99 -Wall -O2

LOCAL_LDFLAGS   := -shared
LOCAL_CFLAGS += -DHAVE_CONFIG_H -I $(LOCAL_PATH)/include
CC      := arm-linux-androideabi-gcc
LOCAL_CFLAGS  += --sysroot=$(SYSROOT)

include $(BUILD_SHARED_LIBRARY)
