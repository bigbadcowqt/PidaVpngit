cmake_minimum_required(VERSION 3.4.1)

add_library(v2ray SHARED
            native-lib.cpp)

find_library(log-lib log)

target_link_libraries(v2ray
                      ${log-lib})
