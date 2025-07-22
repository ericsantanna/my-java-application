#!/bin/bash
set -e

# Get the directory where this script is located
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

rm -rf "${SCRIPT_DIR}/build" || true
mkdir -p "${SCRIPT_DIR}/build"
(cd "${SCRIPT_DIR}/build" && cmake .. && make)

echo "Build completed in $SCRIPT_DIR/build"
