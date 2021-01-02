package com.mulgundkar.wallpaper_manager

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.WallpaperManager
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.os.Build
import io.flutter.embedding.engine.loader.FlutterLoader
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry.Registrar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.InputStream

/**
 * WallpaperManagerPlugin
 */
class WallpaperManagerPlugin : FlutterPlugin, MethodCallHandler {

    private val coroutineScope = MainScope()
    private lateinit var channel: MethodChannel
    private var context: Context? = null

    override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        context = flutterPluginBinding.applicationContext
        channel = MethodChannel(flutterPluginBinding.getFlutterEngine().getDartExecutor(), "wallpaper_manager")
        channel.setMethodCallHandler(this);
    }

    override fun onMethodCall(call: MethodCall, result: Result) {
        when (call.method) {
            "getPlatformVersion" -> result.success("Android ${android.os.Build.VERSION.RELEASE}")
            "setWallpaperFromFile" -> {
                coroutineScope.launch {
                    withContext(Dispatchers.IO) {
                        val res = setWallpaperFromFile(call.argument<String>("filePath") as String, call.argument<Int>("wallpaperLocation") as Int)
                        withContext(Dispatchers.Main) {
                            result.success(res);
                        }
                    }
                }
            }
            "setWallpaperFromFileWithCrop" -> {
                coroutineScope.launch {
                    withContext(Dispatchers.IO) {
                        val res = setWallpaperFromFileWithCrop(call.argument<String>("filePath") as String, call.argument<Int>("wallpaperLocation") as Int, call.argument<Int>("left") as Int, call.argument<Int>("top") as Int, call.argument<Int>("right") as Int, call.argument<Int>("bottom") as Int)
                        withContext(Dispatchers.Main) {
                            result.success(res);
                        }
                    }
                }
            }
            "setWallpaperFromAsset" -> {
                coroutineScope.launch {
                    withContext(Dispatchers.IO) {
                        val res = setWallpaperFromAsset(call.argument<String>("assetPath") as String, call.argument<Int>("wallpaperLocation") as Int)
                        withContext(Dispatchers.Main) {
                            result.success(res);
                        }
                    }
                }
            }
            "setWallpaperFromAssetWithCrop" -> {
                coroutineScope.launch {
                    withContext(Dispatchers.IO) {
                        val res = setWallpaperFromAssetWithCrop(call.argument<String>("assetPath") as String, call.argument<Int>("wallpaperLocation") as Int, call.argument<Int>("left") as Int, call.argument<Int>("top") as Int, call.argument<Int>("right") as Int, call.argument<Int>("bottom") as Int)
                        withContext(Dispatchers.Main) {
                            result.success(res);
                        }
                    }
                }
            }
            else -> result.notImplemented()
        }
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
        context = null;
    }

    @SuppressLint("MissingPermission")
    private fun setWallpaperFromFile(filePath: String, wallpaperLocation: Int): Int {
        var result = -1
        val bitmap = BitmapFactory.decodeFile(filePath)
        val wm = WallpaperManager.getInstance(context)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                result = wm.setBitmap(bitmap, null, false, wallpaperLocation)
            } else {
                wm.setBitmap(bitmap)
                result = 1
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return result
    }

    @SuppressLint("MissingPermission")
    private fun setWallpaperFromFileWithCrop(filePath: String, wallpaperLocation: Int, left: Int, top: Int, right: Int, bottom: Int): Int {
        var result = -1
        val bitmap = BitmapFactory.decodeFile(filePath)
        val wm = WallpaperManager.getInstance(context)
        try {
            result = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                wm.setBitmap(bitmap, Rect(left, top, right, bottom), false, wallpaperLocation)
            } else {
                wm.setBitmap(bitmap)
                1
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return result
    }

    @TargetApi(Build.VERSION_CODES.ECLAIR)
    @SuppressLint("MissingPermission")
    private fun setWallpaperFromAsset(assetPath: String, wallpaperLocation: Int): Int {
        var result = -1
        try {
            val wm = WallpaperManager.getInstance(context)
            result = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val inputStream = context!!.assets.open("flutter_assets/$assetPath")
                val bitmap = BitmapFactory.decodeStream(inputStream)
                wm.setBitmap(bitmap, null, false, wallpaperLocation)
            } else {
                val assetLookupKey: String = FlutterLoader.getInstance().getLookupKeyForAsset(assetPath)
                val assetManager = context!!.assets
                val assetFileDescriptor = assetManager.openFd(assetLookupKey)
                val inputStream: InputStream = assetFileDescriptor.createInputStream()
                wm.setStream(inputStream)
                1
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return result
    }

    @SuppressLint("MissingPermission")
    private fun setWallpaperFromAssetWithCrop(assetPath: String, wallpaperLocation: Int, left: Int, top: Int, right: Int, bottom: Int): Int {
        var result = -1
        try {
            val wm = WallpaperManager.getInstance(context)
            result = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val inputStream = context!!.assets.open("flutter_assets/$assetPath")
                val bitmap = BitmapFactory.decodeStream(inputStream)
                wm.setBitmap(bitmap, Rect(left, top, right, bottom), false, wallpaperLocation)
            } else {
                val assetLookupKey: String = FlutterLoader.getInstance().getLookupKeyForAsset(assetPath)
                val assetManager = context!!.assets
                val assetFileDescriptor = assetManager.openFd(assetLookupKey)
                val inputStream: InputStream = assetFileDescriptor.createInputStream()
                wm.setStream(inputStream)
                1
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return result
    }


    companion object {
        private var context: Context? = null

        @JvmStatic
        fun registerWith(pluginRegistrar: Registrar) {
            context = pluginRegistrar.context()
            val channel = MethodChannel(pluginRegistrar.messenger(), "wallpaper_manager")
            channel.setMethodCallHandler(WallpaperManagerPlugin())
        }
    }
}