#!/usr/bin/env bash
set -euo pipefail

# Back up the MySQL database used by the local FlashGo profile.
project_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
backup_dir="${FLASHGO_BACKUP_DIR:-$project_root/datagrip}"
db_host="${FLASHGO_DB_HOST:-127.0.0.1}"
db_port="${FLASHGO_DB_PORT:-3306}"
db_user="${FLASHGO_DB_USER:-root}"
db_name="${FLASHGO_DB_NAME:-flashgo}"

if command -v mysqldump >/dev/null 2>&1; then
  mysqldump_bin="$(command -v mysqldump)"
elif [[ -x /usr/local/mysql/bin/mysqldump ]]; then
  mysqldump_bin=/usr/local/mysql/bin/mysqldump
else
  echo 'mysqldump was not found; set PATH to your MySQL bin directory.' >&2
  exit 1
fi

umask 077
mkdir -p "$backup_dir"
backup_file="$backup_dir/${db_name}_$(date +%Y%m%d_%H%M%S).sql"
if [[ -e "$backup_file" ]]; then
  echo "Backup already exists: $backup_file" >&2
  exit 1
fi

temp_file="$(mktemp "$backup_file.tmp.XXXXXX")"
trap 'rm -f "$temp_file"' EXIT

"$mysqldump_bin" --no-defaults \
  --protocol=tcp --host="$db_host" --port="$db_port" --user="$db_user" -p \
  --single-transaction --quick --routines --events --triggers \
  --hex-blob --set-gtid-purged=OFF --default-character-set=utf8mb4 \
  --no-tablespaces --column-statistics=0 --databases "$db_name" \
  --result-file="$temp_file"

mv "$temp_file" "$backup_file"
echo "Backup saved to: $backup_file"
