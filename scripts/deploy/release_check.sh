#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"

echo "Checking release artifacts under ${ROOT_DIR}"

if git -C "${ROOT_DIR}" rev-parse --is-inside-work-tree >/dev/null 2>&1; then
  tracked_forbidden="$(
    git -C "${ROOT_DIR}" ls-files | while IFS= read -r path; do
      [ -e "${ROOT_DIR}/${path}" ] || continue
      base_name="$(basename "${path}")"
      dir_name="/$(dirname "${path}")/"
      if [ "${base_name}" = ".DS_Store" ] || [ "${path}" = "logs" ] || [ "${path#logs/}" != "${path}" ] \
        || [ "${path}" = "uploadPath" ] || [ "${path#uploadPath/}" != "${path}" ] \
        || [ "${dir_name#*/logs/}" != "${dir_name}" ] || [ "${dir_name#*/uploadPath/}" != "${dir_name}" ]; then
        printf '%s\n' "${path}"
      fi
    done
  )"
  if [ -n "${tracked_forbidden}" ]; then
    echo "Forbidden runtime artifacts are still tracked by Git:" >&2
    echo "${tracked_forbidden}" >&2
    exit 1
  fi
fi

candidate_dirs=()
if [ "$#" -gt 0 ]; then
  for dir in "$@"; do
    candidate_dirs+=("${dir}")
  done
elif [ -d "${ROOT_DIR}/release" ]; then
  candidate_dirs+=("${ROOT_DIR}/release")
fi

if [ "${#candidate_dirs[@]}" -gt 0 ]; then
  for dir in "${candidate_dirs[@]}"; do
    if [ ! -d "${dir}" ]; then
      echo "Release candidate directory does not exist: ${dir}" >&2
      exit 1
    fi
    for forbidden in ".DS_Store" "logs" "uploadPath"; do
      if find "${dir}" -name "${forbidden}" -print | grep -q .; then
        echo "Forbidden runtime artifact found in release candidate: ${forbidden}" >&2
        find "${dir}" -name "${forbidden}" -print >&2
        exit 1
      fi
    done
  done
fi

if find "${ROOT_DIR}/frontend/dist" -name "*.map" -print 2>/dev/null | grep -q .; then
  echo "Source maps found in frontend/dist" >&2
  find "${ROOT_DIR}/frontend/dist" -name "*.map" -print >&2
  exit 1
fi

echo "Release artifact check passed."
