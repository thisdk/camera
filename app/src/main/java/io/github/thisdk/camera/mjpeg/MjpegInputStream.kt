package io.github.thisdk.camera.mjpeg

import java.io.DataInputStream
import java.io.InputStream

abstract class MjpegInputStream(inputStream: InputStream) : DataInputStream(inputStream)