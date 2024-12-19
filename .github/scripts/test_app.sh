#!/bin/bash

set -eo pipefail

XCFRAMEWORK_PATH="/Users/martyulrich/Repos/newm-mobile/shared/build/XCFrameworks/debug/shared.xcframework"

xcodebuild -project ../../iosApp/iosApp.xcodeproj \
            -scheme iosApp \
            -destination platform=iOS\ Simulator,OS=17.0,name=iPhone\ 11 \
            clean build test
