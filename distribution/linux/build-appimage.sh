#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"
TARGET_DIR="${REPO_ROOT}/target"
APPDIR="${TARGET_DIR}/AppDir"
DIST_DIR="${TARGET_DIR}/digital-dist"
DIST_ROOT="${DIST_DIR}/Digital"
APPIMAGE_TOOL="${APPIMAGETOOL:-${TARGET_DIR}/appimagetool-x86_64.AppImage}"
APPIMAGE_OUTPUT="${TARGET_DIR}/Digital-Dark-x86_64.AppImage"

require_cmd() {
    if ! command -v "$1" >/dev/null 2>&1; then
        echo "error: required command '$1' is not installed" >&2
        exit 1
    fi
}

require_cmd mvn
require_cmd unzip
require_cmd jlink

cd "${REPO_ROOT}"

mvn --batch-mode -DskipTests -Dcheckstyle.skip=true clean install

if [[ ! -f "${TARGET_DIR}/Digital.zip" ]]; then
    echo "error: expected '${TARGET_DIR}/Digital.zip' after Maven build" >&2
    exit 1
fi

rm -rf "${APPDIR}" "${DIST_DIR}" "${APPIMAGE_OUTPUT}"
mkdir -p "${APPDIR}" "${DIST_DIR}"

unzip -q -o "${TARGET_DIR}/Digital.zip" -d "${DIST_DIR}"

if [[ ! -d "${DIST_ROOT}" ]]; then
    echo "error: expected '${DIST_ROOT}' inside extracted distribution zip" >&2
    exit 1
fi

mkdir -p "${APPDIR}/usr/lib/digital"
cp "${DIST_ROOT}/Digital.jar" "${APPDIR}/usr/lib/digital/"
if [[ -d "${DIST_ROOT}/lib" ]]; then
    cp -a "${DIST_ROOT}/lib" "${APPDIR}/usr/lib/digital/"
fi
if [[ -d "${DIST_ROOT}/examples" ]]; then
    cp -a "${DIST_ROOT}/examples" "${APPDIR}/usr/lib/digital/"
fi

jlink \
    --add-modules ALL-MODULE-PATH \
    --strip-debug \
    --no-header-files \
    --no-man-pages \
    --compress=2 \
    --output "${APPDIR}/usr/lib/runtime"

install -Dm755 "${SCRIPT_DIR}/appimage/AppRun" "${APPDIR}/AppRun"
install -Dm644 "${SCRIPT_DIR}/appimage/digital-dark.desktop" "${APPDIR}/digital-dark.desktop"
install -Dm644 "${REPO_ROOT}/src/main/resources/icons/icon128.png" "${APPDIR}/usr/share/icons/hicolor/128x128/apps/digital-dark.png"
install -Dm644 "${SCRIPT_DIR}/digital-simulator.xml" "${APPDIR}/usr/share/mime/packages/digital-simulator.xml"
cp "${APPDIR}/usr/share/icons/hicolor/128x128/apps/digital-dark.png" "${APPDIR}/digital-dark.png"
cp "${APPDIR}/usr/share/icons/hicolor/128x128/apps/digital-dark.png" "${APPDIR}/.DirIcon"

if [[ ! -x "${APPIMAGE_TOOL}" ]]; then
    require_cmd curl
    curl -fsSL "https://github.com/AppImage/AppImageKit/releases/download/continuous/appimagetool-x86_64.AppImage" -o "${APPIMAGE_TOOL}"
    chmod +x "${APPIMAGE_TOOL}"
fi

ARCH=x86_64 APPIMAGE_EXTRACT_AND_RUN=1 "${APPIMAGE_TOOL}" "${APPDIR}" "${APPIMAGE_OUTPUT}"

echo "Created ${APPIMAGE_OUTPUT}"
