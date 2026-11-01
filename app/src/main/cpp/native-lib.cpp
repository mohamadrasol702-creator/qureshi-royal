#include <jni.h>
#include <string.h>
#include <stdlib.h>
#include "node.h"

extern "C" JNIEXPORT jint JNICALL
Java_com_qureshisahib_royal_NodeBridge_startNodeWithArguments(JNIEnv *env, jclass, jobjectArray arguments) {
    jsize argc = env->GetArrayLength(arguments);
    int total = 0;
    for (int i = 0; i < argc; i++) {
        jstring s = (jstring) env->GetObjectArrayElement(arguments, i);
        const char *c = env->GetStringUTFChars(s, 0);
        total += strlen(c) + 1;
        env->ReleaseStringUTFChars(s, c);
    }
    char *buf = (char *) calloc(total, sizeof(char));
    char **argv = (char **) malloc(sizeof(char *) * argc);
    char *cur = buf;
    for (int i = 0; i < argc; i++) {
        jstring s = (jstring) env->GetObjectArrayElement(arguments, i);
        const char *c = env->GetStringUTFChars(s, 0);
        strcpy(cur, c);
        argv[i] = cur;
        cur += strlen(c) + 1;
        env->ReleaseStringUTFChars(s, c);
    }
    return jint(node::Start(argc, argv));
}
