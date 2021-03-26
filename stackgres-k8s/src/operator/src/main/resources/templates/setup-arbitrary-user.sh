#!/bin/sh

USER_NAME=postgres
USER_ID="$(id -u)"
GROUP_ID="$(id -g)"
USER_SHELL=/bin/sh
cp "$TEMPLATES_PATH/passwd" /local/etc/.
cp "$TEMPLATES_PATH/group" /local/etc/.
cp "$TEMPLATES_PATH/shadow" /local/etc/.
cp "$TEMPLATES_PATH/gshadow" /local/etc/.
echo "$USER_NAME:x:$USER_ID:$GROUP_ID::$PG_BASE_PATH:$USER_SHELL" >> /local/etc/passwd
chmod 644 /local/etc/passwd
echo "$USER_NAME:x:$GROUP_ID:" >> /local/etc/group
chmod 644 /local/etc/group
echo "$USER_NAME"':!!:18179:0:99999:7:::' >> /local/etc/shadow
chmod 600 /local/etc/shadow
echo "$USER_NAME"':!::' >> /local/etc/gshadow
chmod 600 /local/etc/gshadow
