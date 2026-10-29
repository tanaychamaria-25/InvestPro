#!/bin/zsh
set -e
cd "$(dirname "$0")"
echo "InvestPro — local Java web app"
echo "Project folder: $PWD"
if ! command -v java >/dev/null 2>&1; then
  echo "Java was not found. Install/use Java 21 or later, then run this again."
  read "?Press Return to close..."
  exit 1
fi
if ! command -v mvn >/dev/null 2>&1; then
  echo "Maven was not found. Run 'brew install maven' in Terminal, then run this again."
  read "?Press Return to close..."
  exit 1
fi
if ! /opt/homebrew/opt/mysql@8.4/bin/mysqladmin --socket=/tmp/mysql84.sock ping >/dev/null 2>&1; then
  echo "MySQL 8.4 is not responding on its existing socket."
  echo "Start the known-good mysql84 data directory using the commands in README.md."
  read "?Press Return to close..."
  exit 1
fi
echo "MySQL 8.4 is responding. Starting InvestPro..."
mvn spring-boot:run
