#!/bin/bash
#
# Start script for items-group-status-updater
#
PORT=8080

exec java -jar -Dserver.port="${PORT}" "item-group-status-updater.jar"
