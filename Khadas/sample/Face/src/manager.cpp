#include "manager.hpp"


manager::manager(/* args */)
{
}

void manager::init()
{
    if (inited) return;
    inited = true;
    init_tengine();
    // set_log_level(LOG_INFO);
    if (request_tengine_version("1.0") < 0)
    {
        std::cout << "Version no Correct " << std::endl;
        return;
    }
    if (load_tengine_plugin(VXDEVICE, "libvxplugin.so", "vx_plugin_init") < 0) {
        std::cout << "Load vx plugin failed.\n";
        return ;
    }    
    vx_context = create_context("vx", 1);
    add_context_device(vx_context, VXDEVICE);
    detect_handler = std::make_shared<face_detect>("./models/FaceDetect.tmfile", vx_context);
    landmark_handler = std::make_shared<face_landmark>("./models/FaceLandmark2d.tmfile", vx_context);
    image_handler = std::make_shared<face_image>();
}

void manager::runDetect(uint8_t* input, int iw, int ih, std::vector<FaceDetectInfo> &face_list)
{
    uint8_t* detect_data = new uint8_t[detect_handler->detect_w * detect_handler->detect_h * detect_handler->detect_bpp];
    image_handler->handDetectImage(input, iw, ih, detect_data, detect_handler->detect_w , detect_handler->detect_h);
    detect_handler->detect(detect_data, face_list);
}

void manager::runLandmark2d(uint8_t* img, int w, int h, std::vector<FaceDetectInfo> detectinfo, std::vector<FaceLandmarkInfo> &info)
{
    std::vector<uint8_t*> landmark_img;
    image_handler->handleLandmark(img, w, h, detectinfo, landmark_img, landmark_handler->landmark_w, landmark_handler->landmark_h);
    landmark_handler->landmark(landmark_img, info);
}



manager::~manager()
{
    release_tengine();
}