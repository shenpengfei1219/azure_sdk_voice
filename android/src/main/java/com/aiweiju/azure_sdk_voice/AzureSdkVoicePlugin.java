package com.aiweiju.azure_sdk_voice;

import androidx.annotation.NonNull;

import android.content.Context;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

/** AzureSdkVoicePlugin */
public class AzureSdkVoicePlugin implements FlutterPlugin, MethodCallHandler {
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private MethodChannel channel;

  private Context context;

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
    channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "azure_sdk_voice");
    channel.setMethodCallHandler(this);

    context = flutterPluginBinding.getApplicationContext();
    recorderPlayer = new AudioRecorderPlayer(context);
  }

  private String key;
  private String region;

  private AudioRecorderPlayer recorderPlayer;

  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
    if (call.method.equals("init")) {
      Log.d("flutter_spf", "init");
      key = call.argument("key");
      region = call.argument("region");
      result.success("OK");
    } else if (call.method.equals("startRecording")) {
      Log.d("flutter_spf", "startRecording");
      String fileName = call.argument("name");
      recorderPlayer.startRecording(fileName);
      result.success("OK");
    } else if (call.method.equals("stopRecording")) {
      Log.d("flutter_spf", "stopRecording");
      recorderPlayer.stopRecording();
      result.success("OK");
    } else if (call.method.equals("playRecordedAudio")) {
      Log.d("flutter_spf", "playRecordedAudio");
      String fileName = call.argument("name");
      recorderPlayer.startPlaying(fileName);
      result.success("OK");
    } else if (call.method.equals("pronunciationScore")) {
      Log.d("flutter_spf", "pronunciationScore");
      String fileName = call.argument("name");
      String language = call.argument("language");
      String topic = call.argument("topic");

      String text = AzureSdk.instance.pronunciationAssessmentWithContentAssessment(key,region,fileName,language,topic);
      result.success(text);
    } else if (call.method.equals("translate")) {
      Log.d("flutter_spf", "translate");
      String fileName = call.argument("name");
      String recLanguage = call.argument("recLanguage");
      String toLanguage = call.argument("toLanguage");
      try {
        String text = AzureSdk.instance.translateWav(key,region,fileName,recLanguage,toLanguage);
        result.success(text);
        return;
      } catch (InterruptedException e) {
        e.printStackTrace();
      } catch (ExecutionException e) {
        e.printStackTrace();
      }
      result.success("error");
    } else if (call.method.equals("speak")) {
      Log.d("flutter_spf", "speak");
      String content = call.argument("content");
      int code = call.argument("code");
      try {
        AzureSdk.instance.synthesisToSpeaker(key,region,content,new Callback() {
          @Override
          public void onCallback(String result,boolean is_last) {
            Map<String, Object> arguments = new HashMap<>();
            arguments.put("code", code);
            arguments.put("res", result);
            arguments.put("is_last", is_last);
            channel.invokeMethod("callback", arguments);
          }
        });
      } catch (InterruptedException e) {
        e.printStackTrace();
      } catch (ExecutionException e) {
        e.printStackTrace();
      }
      result.success("OK");
    } else if (call.method.equals("speak_stop")) {
      Log.d("flutter_spf", "speak_stop");
      AzureSdk.instance.stopSynthesisToSpeaker();
      result.success("OK");
    } else {
      result.notImplemented();
    }
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    channel.setMethodCallHandler(null);
  }
}