#!/usr/bin/env bash
set -euo pipefail

project_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
source_dir="${1:-$project_dir/work/winlator}"
upstream_url=https://github.com/brunodev85/winlator-app.git
upstream_rev=a030f552f452158a2db64fdb32b490fa19c0b48d

if [[ -e "$source_dir" ]]; then
    printf 'Source destination already exists: %s\n' "$source_dir" >&2
    exit 1
fi

git init --quiet "$source_dir"
git -C "$source_dir" fetch --quiet --depth 1 "$upstream_url" "$upstream_rev"
git -C "$source_dir" checkout --quiet --detach "$upstream_rev"
git -C "$source_dir" apply --check "$project_dir/patches/vex-runtime.patch"
git -C "$source_dir" apply "$project_dir/patches/vex-runtime.patch"
install -Dm644 "$project_dir/LICENSE" "$source_dir/app/src/main/assets/licenses/Winlator-LGPL-2.1.txt"
