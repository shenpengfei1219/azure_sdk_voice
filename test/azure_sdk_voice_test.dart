// import 'package:flutter_test/flutter_test.dart';
// import 'package:azure_sdk_voice/azure_sdk_voice.dart';
// import 'package:azure_sdk_voice/azure_sdk_voice_platform_interface.dart';
// import 'package:azure_sdk_voice/azure_sdk_voice_method_channel.dart';
// import 'package:plugin_platform_interface/plugin_platform_interface.dart';

// class MockAzureSdkVoicePlatform
//     with MockPlatformInterfaceMixin
//     implements AzureSdkVoicePlatform {
//
//   @override
//   Future<String?> getPlatformVersion() => Future.value('42');
// }
//
// void main() {
//   final AzureSdkVoicePlatform initialPlatform = AzureSdkVoicePlatform.instance;
//
//   test('$MethodChannelAzureSdkVoice is the default instance', () {
//     expect(initialPlatform, isInstanceOf<MethodChannelAzureSdkVoice>());
//   });
//
//   test('getPlatformVersion', () async {
//     AzureSdkVoice azureSdkVoicePlugin = AzureSdkVoice();
//     MockAzureSdkVoicePlatform fakePlatform = MockAzureSdkVoicePlatform();
//     AzureSdkVoicePlatform.instance = fakePlatform;
//
//     expect(await azureSdkVoicePlugin.getPlatformVersion(), '42');
//   });
// }
