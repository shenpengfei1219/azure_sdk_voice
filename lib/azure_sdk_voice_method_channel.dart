import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'azure_sdk_voice_platform_interface.dart';

/// An implementation of [AzureSdkVoicePlatform] that uses method channels.
class MethodChannelAzureSdkVoice extends AzureSdkVoicePlatform {
  static Map<int, Function> _callbacks = {};

  static int code = 0;

  // 注册一个回调函数到 Map
  static void registerCallback(Function callback) {
    if (code == 65535) {
      code = 0;
    } else {
      code++;
    }
    _callbacks[code] = callback;
  }

  // 处理原生代码的回调
  static Future<void> _handleNativeCall(MethodCall call) async {
    int code = call.arguments['code'];  // 假设原生代码会传回一个 'code'
    if (_callbacks.containsKey(code)) {
      _callbacks[code]?.call(call.arguments['res']);  // 调用回调，并传入数据
      bool is_last = call.arguments['is_last'];
      if (is_last) {
        _callbacks.remove(code);
      }

    } else {
      print('No callback registered for code $code');
    }
  }

  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('azure_sdk_voice');

  @override
  Future<String?> init(String key, String region) async {
    methodChannel.setMethodCallHandler(_handleNativeCall);
    final res = await methodChannel
        .invokeMethod<String>('init', {'key': key, 'region': region});
    return res;
  }

  @override
  Future<String?> startRecording(String name) async {
    final res = await methodChannel
        .invokeMethod<String>('startRecording', {'name': name});
    return res;
  }

  @override
  Future<String?> stopRecording() async {
    final res = await methodChannel.invokeMethod<String>('stopRecording');
    return res;
  }

  @override
  Future<String?> playRecordedAudio(String name) async {
    final res = await methodChannel
        .invokeMethod<String>('playRecordedAudio', {'name': name});
    return res;
  }

  @override
  Future<String?> pronunciationScore(
      String name, String language, String topic) async {
    final res = await methodChannel.invokeMethod<String>('pronunciationScore',
        {'name': name, 'language': language, 'topic': topic});
    return res;
  }

  @override
  Future<String?> translate(
      String name, String recLanguage, String toLanguage) async {
    final res = await methodChannel.invokeMethod<String>('translate',
        {'name': name, 'recLanguage': recLanguage, 'toLanguage': toLanguage});
    return res;
  }

  @override
  Future<String?> speak(String content,Function callback) async {
    registerCallback(callback);
    final res =
        await methodChannel.invokeMethod<String>('speak', {'code': code,'content': content});
    return res;
  }

  @override
  Future<String?> speakStop() async {
    final res =
    await methodChannel.invokeMethod<String>('speak_stop');
    return res;
  }
}
