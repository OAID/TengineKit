rm -rf ./build
mkdir build
cd build

rm -rf ./bin
mkdir bin

build_file="Face"


cmake ../ \
-DBUILD_FILE=$build_file \
-DCMAKE_BUILD_TYPE=Release


make -j4