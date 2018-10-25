## Oculi - a mini graphics accelerator for basic wlroots/wayland requirements

Written in SpinalHDL featuring a custom VexRiscv core.

## Required extensions
 - eglGetPlatformDisplayEXT
 - eglCreatePlatformWindowSurfaceEXT
 - GLES2 & some trivial extensions
 - scaling, cropping, pixel format conversion and blending instructions

## Improved performance extensions
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

## Kernel drivers
 - DRM
 - KMS
 - GBM

## Investigate

https://github.com/swaywm/wlroots/tree/master/render
 
