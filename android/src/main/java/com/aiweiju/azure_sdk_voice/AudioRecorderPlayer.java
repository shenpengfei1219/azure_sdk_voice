package com.aiweiju.azure_sdk_voice;

import android.content.Context;
import android.media.MediaPlayer;

import java.io.IOException;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;

public class AudioRecorderPlayer {

    private MediaPlayer mediaPlayer;

    private static final String AUDIO_RECORDER_FILE_EXT_WAV = ".wav";

    private AudioRecord audioRecorder;
    private int bufferSize;
    private Thread recordingThread;
    private boolean isRecording = false;

    private PcmToWavUtil ptwUtil = new PcmToWavUtil();

    public void startRecording(String fileName) {
        bufferSize = AudioRecord.getMinBufferSize(44100, AudioFormat.CHANNEL_IN_STEREO,
                AudioFormat.ENCODING_PCM_16BIT);

        audioRecorder = new AudioRecord(MediaRecorder.AudioSource.MIC, 44100,
                AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);

        audioRecorder.startRecording();
        isRecording = true;

        recordingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                writeAudioDataToFile(fileName);
                audioDataToFile2Wav(fileName);
            }
        });
        recordingThread.start();
    }

    public void stopRecording() {
        isRecording = false;

        if (audioRecorder != null) {
            audioRecorder.stop();
            audioRecorder.release();
            audioRecorder = null;
        }

        if (recordingThread != null) {
            recordingThread = null;
        }
    }

    private void writeAudioDataToFile(String fileName) {
        byte data[] = new byte[bufferSize];

        File externalStorageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);

        if (externalStorageDir != null) {
            String filePath = externalStorageDir.getAbsolutePath() + "/" + fileName + "_pcm" + AUDIO_RECORDER_FILE_EXT_WAV;
            File file = new File(filePath);

            try {
                DataOutputStream dos = new DataOutputStream(new FileOutputStream(file));

                while (isRecording) {
                    audioRecorder.read(data, 0, bufferSize);
                    dos.write(data);
                }

                dos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void audioDataToFile2Wav(String fileName) {
        File externalStorageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);

        if (externalStorageDir != null) {
            String filePathPcm = externalStorageDir.getAbsolutePath() + "/" + fileName + "_pcm" + AUDIO_RECORDER_FILE_EXT_WAV;
            String filePathWav = externalStorageDir.getAbsolutePath() + "/" + fileName + AUDIO_RECORDER_FILE_EXT_WAV;
            ptwUtil.pcmToWav(filePathPcm,filePathWav,true);
        }
    }

    public void startPlaying(String fileName) {
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
            try {
                File externalStorageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
                if (externalStorageDir != null) {
                    String filePath = externalStorageDir.getAbsolutePath() + "/" + fileName + AUDIO_RECORDER_FILE_EXT_WAV;
                    mediaPlayer.setDataSource(filePath);
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            mediaPlayer.seekTo(0);
            mediaPlayer.start();
        }
    }

    public void stopPlaying() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}

