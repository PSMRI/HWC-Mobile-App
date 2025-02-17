cmake_minimum_required(VERSION 3.11)
project(CHO LANGUAGES CXX)

# Fetch environment variables
set(BASE_TMC_URL "$ENV{BASE_TMC_URL}")
set(BASE_AMRIT_URL "$ENV{BASE_AMRIT_URL}")
set(BASE_FLW_URL "$ENV{BASE_FLW_URL}")
set(BASE_ABHA_URL "$ENV{BASE_ABHA_URL}")
set(SANJEEVANI_API_URL "$ENV{SANJEEVANI_API_URL}")

# Pass the values to the compiler using add_definitions
add_definitions(
        -DBASE_TMC_URL=\"${BASE_TMC_URL}\"
        -DBASE_AMRIT_URL=\"${BASE_AMRIT_URL}\"
        -DBASE_FLW_URL=\"${BASE_FLW_URL}\"
        -DBASE_ABHA_URL=\"${BASE_ABHA_URL}\"
        -DSANJEEVANI_API_URL=\"${SANJEEVANI_API_URL}\"
)

# Define the library name
set(LIBRARY_NAME "cho")

# Add the native-lib.cpp as the source file for the shared library
add_library(
        ${LIBRARY_NAME}
        SHARED
        native-lib.cpp
)

# Find the log library
find_library(
        log-lib
        log
)

# Specifies libraries CMake should link to your target library.
target_link_libraries(
        ${LIBRARY_NAME}
        ${log-lib}
)

# Include directories
# include_directories(${CMAKE_SOURCE_DIR}/include)
