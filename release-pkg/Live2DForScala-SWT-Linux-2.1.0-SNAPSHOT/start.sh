#!/bin/sh
if [ "$XDG_SESSION_TYPE" = "wayland" ] || [ -n "$WAYLAND_DISPLAY" ]; then
  export GDK_BACKEND=x11
fi
exec java -Xms256m -Xmx600m -XX:+UseG1GC -Dsun.java2d.opengl=true -jar Live2DForScala-SWT-Linux-2.1.0-SNAPSHOT.jar
