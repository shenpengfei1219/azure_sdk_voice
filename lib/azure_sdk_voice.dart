import 'azure_sdk_voice_platform_interface.dart';

class AzureSdkVoice {
  Future<String?> init(String key, String region) {
    if (key.isEmpty) {
      return Future.value('key can not be blank');
    }
    if (region.isEmpty) {
      return Future.value('region can not be blank');
    }
    return AzureSdkVoicePlatform.instance.init(key, region);
  }

  Future<String?> startRecording(String name) {
    return AzureSdkVoicePlatform.instance.startRecording(name);
  }

  Future<String?> stopRecording() {
    return AzureSdkVoicePlatform.instance.stopRecording();
  }

  Future<String?> playRecordedAudio(String name) {
    return AzureSdkVoicePlatform.instance.playRecordedAudio(name);
  }

  Future<String?> pronunciationScore(
      String name, String language, String topic) {
    return AzureSdkVoicePlatform.instance
        .pronunciationScore(name, language, topic);
  }

  Future<String?> translate(
      String name, String recLanguage, String toLanguage) {
    return AzureSdkVoicePlatform.instance
        .translate(name, recLanguage, toLanguage);
  }

  Future<String?> speak(String content,Function callback) {
    return AzureSdkVoicePlatform.instance.speak(content,callback);
  }

  Future<String?> speakStop() {
    return AzureSdkVoicePlatform.instance.speakStop();
  }
}
