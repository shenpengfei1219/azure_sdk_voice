import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'azure_sdk_voice_method_channel.dart';

abstract class AzureSdkVoicePlatform extends PlatformInterface {
  /// Constructs a AzureSdkVoicePlatform.
  AzureSdkVoicePlatform() : super(token: _token);

  static final Object _token = Object();

  static AzureSdkVoicePlatform _instance = MethodChannelAzureSdkVoice();

  /// The default instance of [AzureSdkVoicePlatform] to use.
  ///
  /// Defaults to [MethodChannelAzureSdkVoice].
  static AzureSdkVoicePlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [AzureSdkVoicePlatform] when
  /// they register themselves.
  static set instance(AzureSdkVoicePlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<String?> init(String key, String region) {
    throw UnimplementedError(
        'init(String key,String region) has not been implemented.');
  }

  Future<String?> startRecording(String name) {
    throw UnimplementedError(
        'startRecording(String name) has not been implemented.');
  }

  Future<String?> stopRecording() {
    throw UnimplementedError('stopRecording() has not been implemented.');
  }

  Future<String?> playRecordedAudio(String name) {
    throw UnimplementedError(
        'playRecordedAudio(String name) has not been implemented.');
  }

  Future<String?> pronunciationScore(
      String name, String language, String topic) {
    throw UnimplementedError(
        'pronunciationScore(String name,String language,String topic) has not been implemented.');
  }

  Future<String?> translate(
      String name, String recLanguage, String toLanguage) {
    throw UnimplementedError(
        'translate(String name,String recLanguage,String toLanguage) has not been implemented.');
  }

  Future<String?> speak(String content) {
    throw UnimplementedError('speak(String content) has not been implemented.');
  }
}
