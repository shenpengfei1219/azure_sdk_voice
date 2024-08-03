import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:azure_sdk_voice/azure_sdk_voice.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _statusText = 'Hold to record';

  final _azureSdkVoicePlugin = AzureSdkVoice();

  @override
  void initState() {
    super.initState();
    var key = "Your key";
    var region = "Your region";
    _azureSdkVoicePlugin
        .init(key, region)
        .then((value) => print("initState $value"));
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin azure sdk voice app'),
        ),
        body: Center(
            child: ListView(
          // mainAxisAlignment: MainAxisAlignment.center,
          children: [
            GestureDetector(
              onLongPressStart: (details) async {
                _azureSdkVoicePlugin.startRecording("testfile");
                setState(() {
                  _statusText = 'Recording...';
                  // Start recording logic here
                });
              },
              onLongPressEnd: (details) async {
                _azureSdkVoicePlugin.stopRecording();
                setState(() {
                  _statusText = 'Hold to record';
                  // Stop recording logic here
                });
              },
              child: Container(
                padding: const EdgeInsets.all(20.0),
                decoration: BoxDecoration(
                  color: Colors.blue,
                  borderRadius: BorderRadius.circular(10.0),
                ),
                child: Text(
                  _statusText,
                  style: const TextStyle(color: Colors.white, fontSize: 20.0),
                ),
              ),
            ),
            Container(
              height: 50,
            ),
            GestureDetector(
              onTap: () async {
                var res =
                    await _azureSdkVoicePlugin.playRecordedAudio("testfile");
                print(res);
              },
              child: Container(
                padding: const EdgeInsets.all(20.0),
                decoration: BoxDecoration(
                  color: Colors.blue,
                  borderRadius: BorderRadius.circular(10.0),
                ),
                child: const Text(
                  "play",
                  style: TextStyle(color: Colors.white, fontSize: 20.0),
                ),
              ),
            ),
            Container(
              height: 50,
            ),
            GestureDetector(
              onTap: () async {
                var res = await _azureSdkVoicePlugin.pronunciationScore(
                    "testfile", "en-US", "");
                print(res);
              },
              child: Container(
                padding: const EdgeInsets.all(20.0),
                decoration: BoxDecoration(
                  color: Colors.blue,
                  borderRadius: BorderRadius.circular(10.0),
                ),
                child: const Text(
                  "pronunciationScore",
                  style: TextStyle(color: Colors.white, fontSize: 20.0),
                ),
              ),
            ),

            Container(
              height: 50,
            ),
            GestureDetector(
              onTap: () async {
                var res = await _azureSdkVoicePlugin.translate(
                    "testfile", "zh-cn", "en");
                print(res);
              },
              child: Container(
                padding: const EdgeInsets.all(20.0),
                decoration: BoxDecoration(
                  color: Colors.blue,
                  borderRadius: BorderRadius.circular(10.0),
                ),
                child: const Text(
                  "translate",
                  style: TextStyle(color: Colors.white, fontSize: 20.0),
                ),
              ),
            ),

            Container(
              height: 50,
            ),
            GestureDetector(
              onTap: () async {
                var res = await _azureSdkVoicePlugin.startTranslateContinuous("zh-cn", "en",(res) {
                  print("Callback for trans received: $res");
                });
                print(res);
              },
              child: Container(
                padding: const EdgeInsets.all(20.0),
                decoration: BoxDecoration(
                  color: Colors.blue,
                  borderRadius: BorderRadius.circular(10.0),
                ),
                child: const Text(
                  "start translate continuous",
                  style: TextStyle(color: Colors.white, fontSize: 20.0),
                ),
              ),
            ),

            Container(
              height: 50,
            ),
            GestureDetector(
              onTap: () async {
                var res = await _azureSdkVoicePlugin.stopTranslateContinuous();
                print(res);
              },
              child: Container(
                padding: const EdgeInsets.all(20.0),
                decoration: BoxDecoration(
                  color: Colors.blue,
                  borderRadius: BorderRadius.circular(10.0),
                ),
                child: const Text(
                  "stop translate continuous",
                  style: TextStyle(color: Colors.white, fontSize: 20.0),
                ),
              ),
            ),

            Container(
              height: 50,
            ),
            GestureDetector(
              onTap: () async {
                String data = """
<speak version="1.0" xmlns="http://www.w3.org/2001/10/synthesis" xmlns:mstts="https://www.w3.org/2001/mstts" xml:lang="zh-cn">
    <voice name="zh-cn-XiaomoNeural">
        <mstts:express-as style="sad" styledegree="2">
            快走吧，路上一定要注意安全，早去早回。
        </mstts:express-as>
    </voice>
</speak>
                  """;
                var res = await _azureSdkVoicePlugin.speak(data,(res) {
                  print("Callback for task received: $res");
                });
                print(res);
              },
              child: Container(
                padding: const EdgeInsets.all(20.0),
                decoration: BoxDecoration(
                  color: Colors.blue,
                  borderRadius: BorderRadius.circular(10.0),
                ),
                child: const Text(
                  "speak",
                  style: TextStyle(color: Colors.white, fontSize: 20.0),
                ),
              ),
            ),
            Container(
              height: 50,
            ),
            GestureDetector(
              onTap: () async {
                var res = await _azureSdkVoicePlugin.speakStop();
                print(res);
              },
              child: Container(
                padding: const EdgeInsets.all(20.0),
                decoration: BoxDecoration(
                  color: Colors.blue,
                  borderRadius: BorderRadius.circular(10.0),
                ),
                child: const Text(
                  "speak stop",
                  style: TextStyle(color: Colors.white, fontSize: 20.0),
                ),
              ),
            ),

          ],
        )),
      ),
    );
  }
}
