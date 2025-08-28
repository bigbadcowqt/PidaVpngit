#include <jni.h>
#include <string>
#include <android/log.h>

#define LOG_TAG "V2RayNative"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

extern "C" JNIEXPORT jstring JNICALL
Java_com_pidavpn_app_V2RayVpnService_startV2Ray(
        JNIEnv* env,
        jobject thiz,
        jstring configJson) {
    const char *config = env->GetStringUTFChars(configJson, nullptr);
    
    LOGI("Starting V2Ray with config: %s", config);
    
    // اینجا کد اصلی V2Ray اجرا می‌شود
    // برای سادگی، فقط یک پیام برمی‌گردانیم
    
    env->ReleaseStringUTFChars(configJson, config);
    return env->NewStringUTF("V2Ray started");
}
