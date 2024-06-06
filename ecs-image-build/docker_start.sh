#!/bin/bash
#
# Start script for items-group-status-updater.api.ch.gov.uk
#
PORT=8080

exec java -jar -Dserver.port="${PORT}" "items-group-status-updater.jar"
