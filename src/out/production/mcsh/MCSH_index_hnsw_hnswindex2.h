/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class MCSH_index_hnsw_hnswindex2 */

#ifndef _Included_MCSH_index_hnsw_hnswindex2
#define _Included_MCSH_index_hnsw_hnswindex2
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     MCSH_index_hnsw_hnswindex2
 * Method:    search
 * Signature: (IIILjava/lang/String;Ljava/lang/String;[FII)[I
 */
JNIEXPORT jintArray JNICALL Java_MCSH_index_hnsw_hnswindex2_search__IIILjava_lang_String_2Ljava_lang_String_2_3FII
  (JNIEnv *, jobject, jint, jint, jint, jstring, jstring, jfloatArray, jint, jint);

/*
 * Class:     MCSH_index_hnsw_hnswindex2
 * Method:    search
 * Signature: ([FIILjava/lang/String;[FII)[I
 */
JNIEXPORT jintArray JNICALL Java_MCSH_index_hnsw_hnswindex2_search___3FIILjava_lang_String_2_3FII
  (JNIEnv *, jobject, jfloatArray, jint, jint, jstring, jfloatArray, jint, jint);

/*
 * Class:     MCSH_index_hnsw_hnswindex2
 * Method:    build
 * Signature: (Ljava/lang/String;Ljava/lang/String;[FIIII)V
 */
JNIEXPORT void JNICALL Java_MCSH_index_hnsw_hnswindex2_build__Ljava_lang_String_2Ljava_lang_String_2_3FIIII
  (JNIEnv *, jobject, jstring, jstring, jfloatArray, jint, jint, jint, jint);

/*
 * Class:     MCSH_index_hnsw_hnswindex2
 * Method:    increbuild
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;I[FIIII)V
 */
JNIEXPORT void JNICALL Java_MCSH_index_hnsw_hnswindex2_increbuild
  (JNIEnv *, jobject, jstring, jstring, jstring, jint, jfloatArray, jint, jint, jint, jint);

/*
 * Class:     MCSH_index_hnsw_hnswindex2
 * Method:    build
 * Signature: (Ljava/util/List;Ljava/lang/String;[FIIII)V
 */
JNIEXPORT void JNICALL Java_MCSH_index_hnsw_hnswindex2_build__Ljava_util_List_2Ljava_lang_String_2_3FIIII
  (JNIEnv *, jobject, jobject, jstring, jfloatArray, jint, jint, jint, jint);

#ifdef __cplusplus
}
#endif
#endif
