import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'azure_sdk_voice_platform_interface.dart';

/// An implementation of [AzureSdkVoicePlatform] that uses method channels.
class MethodChannelAzureSdkVoice extends AzureSdkVoicePlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('azure_sdk_voice');

  @override
  Future<String?> init(String key, String region) async {
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
  Future<String?> speak(String content) async {
    final res =
        await methodChannel.invokeMethod<String>('speak', {'content': content});
    return res;
  }
}
