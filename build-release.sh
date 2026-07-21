#!/usr/bin/env bash
#
# build-release.sh - Build a GmkSplitter release zip on Linux without Eclipse/Ant.
#
# Produces a release zip with the same layout as the historical GitHub releases
# (e.g. GmkSplitter.v0.18.zip): a single top-level directory containing
# gmksplit.jar, gmksplit.exe (optional, via launch4j), README, LICENSE, COPYING.
#
# Usage:
#   ./build-release.sh [version]
#
# [version] defaults to "v0.19-dev". The output is written to
# release/GmkSplitter.<version>.zip, built from a staging directory
# release/GmkSplitter.<version>/.
#
# Dependencies handled automatically (no root/apt/ant required):
#   - Needs a JDK (javac/jar). If none is found on PATH, a Temurin 21 JDK
#     tarball is downloaded and cached under .build-cache/jdk.
#   - GmkSplitter depends on parts of the LateralGM project (org.lateralgm.*),
#     which live in a sibling Eclipse project not included in this repo. This
#     script fetches the reference v0.18 release, whose gmksplit.jar bundles
#     the LateralGM .java sources it was built from (jardesc export included
#     sources), and vendors the org/lateralgm tree from there. Three small
#     patches are applied to that vendored copy so it compiles on a modern
#     JDK (see patch_vendor_sources below) - upstream used internal
#     com.sun.* JDK APIs that no longer exist, plus one apparent typo'd
#     import (com.sun.corba's unrelated "Action" class instead of
#     org.lateralgm.resources.sub.Action) and one raw-generic cast that
#     older javac/ecj accepted but modern javac rejects.
#   - Optionally wraps the jar into a Windows gmksplit.exe using launch4j.
#     A launch4j Linux tarball is downloaded and run headless
#     (java -Djava.awt.headless=true -jar launch4j.jar config.xml, no GUI/X
#     server needed). If this step fails for any reason, the script skips it
#     with a clear warning rather than failing the whole build.
#
# Nothing is installed system-wide and no git state is touched.

set -euo pipefail

# ---------------------------------------------------------------------------
# Configuration
# ---------------------------------------------------------------------------

VERSION="${1:-v0.19-dev}"
REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
CACHE_DIR="$REPO_ROOT/.build-cache"
RELEASE_DIR="$REPO_ROOT/release"
STAGE_NAME="GmkSplitter.${VERSION}"
STAGE_DIR="$RELEASE_DIR/$STAGE_NAME"
BUILD_DIR="$RELEASE_DIR/.build"
ZIP_PATH="$RELEASE_DIR/${STAGE_NAME}.zip"

REF_ZIP_URL="https://github.com/Medo42/Gmk-Splitter/releases/download/V0.18/GmkSplitter.v0.18.zip"
REF_ZIP_CACHE="$CACHE_DIR/GmkSplitter.v0.18.zip"
REF_SRC_DIR="$CACHE_DIR/ref-v0.18-src"

TEMURIN_JDK_URL="https://api.adoptium.net/v3/binary/latest/21/ga/linux/x64/jdk/hotspot/normal/eclipse?project=jdk"
JDK_CACHE_DIR="$CACHE_DIR/jdk"

LAUNCH4J_URL="https://sourceforge.net/projects/launch4j/files/launch4j-3/3.50/launch4j-3.50-linux-x64.tgz/download"
LAUNCH4J_CACHE_DIR="$CACHE_DIR/launch4j"

MAIN_CLASS="com.ganggarrison.gmdec.GmkSplitter"

log() { echo "[build-release] $*" >&2; }
warn() { echo "[build-release] WARNING: $*" >&2; }

# download_gzip_retry <url> <dest> - fetch a gzip-based archive, retrying a
# few times if the server hands back a truncated file or an HTML interstitial
# page instead of the real archive (observed intermittently from SourceForge).
download_gzip_retry() {
    local url="$1" dest="$2" attempt
    for attempt in 1 2 3 4 5; do
        curl -sL -o "$dest" "$url" || true
        if [ -s "$dest" ] && gzip -t "$dest" 2>/dev/null; then
            return 0
        fi
        warn "Download attempt $attempt of '$url' produced an invalid archive, retrying..."
        sleep 2
    done
    return 1
}

mkdir -p "$CACHE_DIR"

# ---------------------------------------------------------------------------
# Step 1: Locate or fetch a JDK (javac + jar)
# ---------------------------------------------------------------------------

find_jdk_bin_dir() {
    if command -v javac >/dev/null 2>&1 && command -v jar >/dev/null 2>&1; then
        dirname "$(command -v javac)"
        return 0
    fi
    if [ -n "${JAVA_HOME:-}" ] && [ -x "$JAVA_HOME/bin/javac" ]; then
        echo "$JAVA_HOME/bin"
        return 0
    fi
    if [ -d "$JDK_CACHE_DIR" ]; then
        local found
        found="$(find "$JDK_CACHE_DIR" -maxdepth 2 -type d -name bin 2>/dev/null | head -1)"
        if [ -n "$found" ] && [ -x "$found/javac" ]; then
            echo "$found"
            return 0
        fi
    fi
    return 1
}

JDK_BIN="$(find_jdk_bin_dir || true)"
if [ -z "$JDK_BIN" ]; then
    log "No JDK (javac/jar) found on PATH or JAVA_HOME; downloading Temurin 21 JDK..."
    mkdir -p "$JDK_CACHE_DIR"
    if ! download_gzip_retry "$TEMURIN_JDK_URL" "$JDK_CACHE_DIR/temurin21.tar.gz"; then
        log "ERROR: failed to download a valid Temurin JDK archive."
        exit 1
    fi
    tar xzf "$JDK_CACHE_DIR/temurin21.tar.gz" -C "$JDK_CACHE_DIR"
    rm -f "$JDK_CACHE_DIR/temurin21.tar.gz"
    JDK_BIN="$(find_jdk_bin_dir)"
fi
log "Using JDK: $("$JDK_BIN/javac" -version 2>&1)"

JAVAC="$JDK_BIN/javac"
JAR="$JDK_BIN/jar"
JAVA_BIN="$JDK_BIN/java"

# ---------------------------------------------------------------------------
# Step 2: Fetch reference release and vendor the org.lateralgm sources from it
# ---------------------------------------------------------------------------

if [ ! -f "$REF_ZIP_CACHE" ] || ! unzip -tqq "$REF_ZIP_CACHE" >/dev/null 2>&1; then
    log "Downloading reference release (v0.18) to vendor LateralGM sources from..."
    for attempt in 1 2 3 4 5; do
        curl -sL -o "$REF_ZIP_CACHE" "$REF_ZIP_URL" || true
        if unzip -tqq "$REF_ZIP_CACHE" >/dev/null 2>&1; then
            break
        fi
        warn "Download attempt $attempt of reference zip was invalid, retrying..."
        sleep 2
    done
    if ! unzip -tqq "$REF_ZIP_CACHE" >/dev/null 2>&1; then
        log "ERROR: failed to download a valid reference release zip."
        exit 1
    fi
fi

if [ ! -d "$REF_SRC_DIR" ]; then
    log "Extracting org.lateralgm sources bundled in the reference gmksplit.jar..."
    ref_extract_tmp="$CACHE_DIR/ref-extract-tmp"
    rm -rf "$ref_extract_tmp"
    mkdir -p "$ref_extract_tmp"
    unzip -q "$REF_ZIP_CACHE" -d "$ref_extract_tmp"
    ref_jar="$(find "$ref_extract_tmp" -name gmksplit.jar | head -1)"
    mkdir -p "$REF_SRC_DIR"
    (cd "$REF_SRC_DIR" && unzip -q -o "$ref_jar" 'org/*')
    rm -rf "$ref_extract_tmp"
fi

patch_vendor_sources() {
    # Apply minimal patches to the vendored org.lateralgm tree so it compiles
    # on a modern JDK. These files use internal com.sun.* APIs removed since
    # JDK 9, and one raw-generics cast rejected by modern javac. None of the
    # affected code paths are exercised by the GmkSplitter CLI tool itself.
    local dir="$1"

    # (a) Wrong "Action" import: com.sun.corba's unrelated internal FSM
    # Action class instead of org.lateralgm.resources.sub.Action - looks
    # like an IDE auto-import mistake in upstream LateralGM. The call site
    # (GmObjectFrame.areResourceFieldsEqual) is clearly comparing
    # org.lateralgm.resources.sub.Action/Argument fields.
    sed -i \
        's|import com\.sun\.corba\.se\.spi\.orbutil\.fsm\.Action;|import org.lateralgm.resources.sub.Action;|' \
        "$dir/subframes/GmObjectFrame.java"

    # (b) Util.tweakIIORegistry() de-registers the JDK's built-in WBMP image
    # reader SPI using an internal com.sun.imageio class, replacing it with
    # LateralGM's own WBMPImageReaderSpiFix. That internal class no longer
    # exists on modern JDKs. Drop the two lines and the now-unused fix class;
    # this only affects loading of .wbmp image files in the Swing UI, which
    # GmkSplitter's CLI does not use.
    python3 - "$dir/main/Util.java" <<'PYEOF'
import sys
p = sys.argv[1]
s = open(p, encoding='utf-8').read()
s = s.replace('import com.sun.imageio.plugins.wbmp.WBMPImageReaderSpi;\n', '')
s = s.replace('import org.lateralgm.file.iconio.WBMPImageReaderSpiFix;\n', '')
s = s.replace(
    '\t\treg.deregisterServiceProvider(reg.getServiceProviderByClass(WBMPImageReaderSpi.class));\n'
    '\t\treg.registerServiceProvider(new WBMPImageReaderSpiFix());\n',
    '\t\t// WBMP re-registration removed: relied on an internal com.sun.imageio\n'
    '\t\t// API unavailable on modern JDKs; unused by the command-line tool.\n')
open(p, 'w', encoding='utf-8').write(s)
PYEOF
    rm -f "$dir/file/iconio/WBMPImageReaderSpiFix.java"

    # (c) Unchecked cast from a raw Class<PieceVisual> to Class<V> that older
    # javac/ecj accepted but modern javac rejects as "incompatible types".
    # Route through Class<?> first, which is what the code always intended
    # (the method is already @SuppressWarnings("unchecked")).
    sed -i \
        -e 's|return (Class<V>) PieceVisual\.class;|return (Class<V>) (Class<?>) PieceVisual.class;|' \
        -e 's|return (Class<V>) InstanceVisual\.class;|return (Class<V>) (Class<?>) InstanceVisual.class;|' \
        -e 's|return (Class<V>) TileVisual\.class;|return (Class<V>) (Class<?>) TileVisual.class;|' \
        "$dir/ui/swing/visuals/RoomVisual.java"
}

# ---------------------------------------------------------------------------
# Step 3: Assemble combined source tree and compile
# ---------------------------------------------------------------------------

rm -rf "$BUILD_DIR"
mkdir -p "$BUILD_DIR/src" "$BUILD_DIR/classes"

log "Assembling combined source tree (repo src + vendored org.lateralgm)..."
cp -r "$REPO_ROOT/src/." "$BUILD_DIR/src/"

# Vendor org.lateralgm files (java sources + resources) that aren't already
# provided by this repo (this repo only overrides org/lateralgm/file/PostponeRunner.java).
(cd "$REF_SRC_DIR" && find org -type f ! -name '*.class') | while IFS= read -r f; do
    dest="$BUILD_DIR/src/$f"
    if [ ! -f "$dest" ]; then
        mkdir -p "$(dirname "$dest")"
        cp "$REF_SRC_DIR/$f" "$dest"
    fi
done

patch_vendor_sources "$BUILD_DIR/src/org/lateralgm"

log "Compiling with javac --release 8..."
# Quote each path in the @-file: javac argument files split on whitespace,
# which breaks when the repo lives under a directory containing spaces.
find "$BUILD_DIR/src" -name '*.java' -exec printf '"%s"\n' {} \; > "$BUILD_DIR/sources.txt"
if ! "$JAVAC" --release 8 -encoding UTF-8 -nowarn -d "$BUILD_DIR/classes" @"$BUILD_DIR/sources.txt" 2>"$BUILD_DIR/javac.log"; then
    cat "$BUILD_DIR/javac.log" >&2
    log "Compilation with --release 8 failed, retrying with --release 11..."
    if ! "$JAVAC" --release 11 -encoding UTF-8 -nowarn -d "$BUILD_DIR/classes" @"$BUILD_DIR/sources.txt" 2>"$BUILD_DIR/javac.log"; then
        cat "$BUILD_DIR/javac.log" >&2
        log "ERROR: compilation failed. See above."
        exit 1
    fi
    warn "Built with --release 11 instead of --release 8 (8 failed to compile)."
fi

# ---------------------------------------------------------------------------
# Step 4: Package gmksplit.jar (bundles .class, .java sources, and resources
# for both com.ganggarrison and org.lateralgm, matching the reference jar's
# packaging, which included sources per its Eclipse jardesc export options)
# ---------------------------------------------------------------------------

log "Packaging gmksplit.jar..."
(cd "$BUILD_DIR/src" && find . -type f ! -name '*.class') | while IFS= read -r f; do
    dest="$BUILD_DIR/classes/$f"
    mkdir -p "$(dirname "$dest")"
    cp "$BUILD_DIR/src/$f" "$dest"
done

mkdir -p "$BUILD_DIR/classes/META-INF"
printf 'Manifest-Version: 1.0\nMain-Class: %s\n\n' "$MAIN_CLASS" > "$BUILD_DIR/classes/META-INF/MANIFEST.MF"

rm -rf "$STAGE_DIR"
mkdir -p "$STAGE_DIR"
(cd "$BUILD_DIR/classes" && "$JAR" cfm "$STAGE_DIR/gmksplit.jar" META-INF/MANIFEST.MF .)

cp "$REPO_ROOT/README" "$STAGE_DIR/README"
cp "$REPO_ROOT/LICENSE" "$STAGE_DIR/LICENSE"
cp "$REPO_ROOT/COPYING" "$STAGE_DIR/COPYING"

# ---------------------------------------------------------------------------
# Step 5: Optionally wrap gmksplit.jar into gmksplit.exe using launch4j
# ---------------------------------------------------------------------------

EXE_BUILT=0
build_exe() {
    # launch4j.jar needs its bundled lib/*.jar (xstream etc.) on the classpath;
    # a prior truncated download can leave launch4j.jar present but the lib
    # jars missing, so check for one of those too, not just launch4j.jar.
    if [ ! -f "$LAUNCH4J_CACHE_DIR/launch4j/launch4j.jar" ] || \
       ! ls "$LAUNCH4J_CACHE_DIR"/launch4j/lib/*.jar >/dev/null 2>&1; then
        log "Downloading launch4j (Linux, headless-capable)..."
        rm -rf "$LAUNCH4J_CACHE_DIR"
        mkdir -p "$LAUNCH4J_CACHE_DIR"
        if ! download_gzip_retry "$LAUNCH4J_URL" "$LAUNCH4J_CACHE_DIR/l4j.tgz"; then
            warn "Failed to download a valid launch4j archive; skipping gmksplit.exe."
            return 1
        fi
        tar xzf "$LAUNCH4J_CACHE_DIR/l4j.tgz" -C "$LAUNCH4J_CACHE_DIR"
        rm -f "$LAUNCH4J_CACHE_DIR/l4j.tgz"
    fi

    local l4j_jar="$LAUNCH4J_CACHE_DIR/launch4j/launch4j.jar"
    if [ ! -f "$l4j_jar" ] || ! ls "$LAUNCH4J_CACHE_DIR"/launch4j/lib/*.jar >/dev/null 2>&1; then
        warn "launch4j.jar or its lib/*.jar dependencies not found after download; skipping gmksplit.exe."
        return 1
    fi

    local cfg="$BUILD_DIR/l4j.xml"
    cat > "$cfg" <<EOF
<launch4jConfig>
  <dontWrapJar>false</dontWrapJar>
  <headerType>console</headerType>
  <jar>$STAGE_DIR/gmksplit.jar</jar>
  <outfile>$STAGE_DIR/gmksplit.exe</outfile>
  <errTitle></errTitle>
  <cmdLine></cmdLine>
  <chdir></chdir>
  <priority>normal</priority>
  <downloadUrl>http://java.com/download</downloadUrl>
  <supportUrl></supportUrl>
  <customProcName>false</customProcName>
  <stayAlive>false</stayAlive>
  <manifest></manifest>
  <icon></icon>
  <jre>
    <path></path>
    <minVersion>1.6.0</minVersion>
    <maxVersion></maxVersion>
    <jdkPreference>preferJre</jdkPreference>
  </jre>
</launch4jConfig>
EOF

    log "Running launch4j headlessly to build gmksplit.exe..."
    if "$JAVA_BIN" -Djava.awt.headless=true -jar "$l4j_jar" "$cfg"; then
        if [ -f "$STAGE_DIR/gmksplit.exe" ]; then
            return 0
        fi
    fi
    return 1
}

if build_exe; then
    EXE_BUILT=1
    log "gmksplit.exe built successfully."
else
    warn "Skipped gmksplit.exe: launch4j step failed or was unavailable."
    warn "The release zip will contain gmksplit.jar but no gmksplit.exe."
fi

# ---------------------------------------------------------------------------
# Step 6: Zip it up, matching the reference layout (single top-level dir)
# ---------------------------------------------------------------------------

log "Creating release zip..."
rm -f "$ZIP_PATH"
(cd "$RELEASE_DIR" && zip -r -q "$ZIP_PATH" "$STAGE_NAME")

log "Done."
log "Staging directory: $STAGE_DIR"
log "Release zip:       $ZIP_PATH"
if [ "$EXE_BUILT" -eq 0 ]; then
    warn "Reminder: gmksplit.exe was NOT included in this build."
fi
