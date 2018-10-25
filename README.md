## Oculi - a mini gpu targeting basic wlroots/wayland acceleration

Written in SpinalHDL featuring a custom VexRiscv core.

## Required extentions
 - eglGetPlatformDisplayEXT
 - eglCreatePlatformWindowSurfaceEXT
 - GLES2 & some trivial extensions
 - scaling, cropping, pixel format conversion and blending instructions

## Imporved Performance extensions
 - eglCreateImageKHR
 - eglDestroyImageKHR
 - eglQueryWaylandBufferWL
 - eglBindWaylandDisplayWL
 - eglUnbindWaylandDisplayWL
 - glEGLImageTargetTexture2DOES
 - eglSwapBuffersWithDamageEXT
 - eglSwapBuffersWithDamageKHR
 - eglQueryDmaBufFormatsEXT
 - eglQueryDmaBufModifiersEXT
 - eglExportDMABUFImageQueryMESA
 - eglExportDMABUFImageMESA
 - eglDebugMessageControlKHR
 - glDebugMessageCallbackKHR
 - glDebugMessageControlKHR
 - glPopDebugGroupKHR
 - glPushDebugGroupKHR

## Kernel Drivers
 - DRM
 - KMS
 - GBM
