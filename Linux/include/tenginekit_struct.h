#ifndef TENGINEKIT_STRUCT
#define TENGINEKIT_STRUCT
//--------------------base struct ------------------------
typedef struct Box {
    float x1;
    float y1;
    float x2;
    float y2;
}Box;

typedef struct vector2 {
    float x;
    float y;
};
//--------------------------------------------------------

//--------------------function struct ------------------------
typedef struct FaceAttribution{
    int gender; // 0 man, 1 woman
    int glasses;
    int age;
    int smile;
    int beauty_man_look;
    int beauty_woman_look;
} FaceAttribution;

typedef struct EyeInfo{
    float eye_landmark[71 * 3];
    float eye_iris[5 * 3];
} EyeInfo;

typedef struct FaceInfo {
    Box face_box;
    float score;
    float head_x;
    float head_y;
    float head_z;
    float lefteye_close_state;
    float righteye_close_state;
    float mouth_close_state;
    float mouth_bigopen_state;
    float landmarks[212 * 2];
    float person_mark[512];
    FaceAttribution attribution;
} FaceInfo;

typedef struct FaceInfo3d {
    Box face_box;
    float landmarks3d[468 * 3];
    EyeInfo eye[2];
} FaceInfo3d;

typedef struct BodyInfo
{
    Box body_box;
    float score;
    float landmark[25 * 4];
} BodyInfo;

typedef struct HandInfo3d
{
    Box hand_box;
    float landmark[21 * 3];
};


//--------------------------------------------------------

//--------------------output struct ------------------------
typedef struct sdkFaces {
    int face_count = 0;
    FaceInfo* info;
} sdkFaces;

typedef struct sdkFaces3d {
    int face3d_count;
    FaceInfo3d* info;
} sdkFaces3d;

typedef struct sdkBody {
    int body_count = 0;
    BodyInfo* info;
} sdkBody;


typedef struct sdkHand3d
{
    int hand_count = 0;
    HandInfo3d* info;
} sdkHand3d;


//--------------------------------------------------------
#endif