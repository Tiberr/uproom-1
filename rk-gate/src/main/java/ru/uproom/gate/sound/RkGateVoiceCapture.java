package ru.uproom.gate.sound;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

/**
 * Created by osipenko on 01.07.15.
 */
public class RkGateVoiceCapture {


    //##############################################################################################################
    //######    fields

    private static final Logger LOG = LoggerFactory.getLogger(RkGateVoiceCapture.class);
    private final GetVoiceFromMic getVoiceFromMic = new GetVoiceFromMic();
    private AudioFileFormat.Type audioFileFormat = AudioFileFormat.Type.WAVE;
    private AudioFormat.Encoding encoding = AudioFormat.Encoding.PCM_SIGNED;
    private String outputFileName = "/home/osipenko/documents/voice-from-mic.wav";
    private TargetDataLine targetDataLine;
    private AudioInputStream audioInputStream;
    private File outputFile;


    //##############################################################################################################
    //######    constructor / destructor


    public void create() {

        AudioFormat audioFormat = new AudioFormat(
                encoding, 44100.0F, 16, 2, 4, 44100.0F, false);

        DataLine.Info info = new DataLine.Info(TargetDataLine.class, audioFormat);
        try {
            targetDataLine = (TargetDataLine) AudioSystem.getLine(info);
            targetDataLine.open(audioFormat);
        } catch (LineUnavailableException e) {
            LOG.error("RECORDING LINE : unable to get, reason ({})", e.getMessage());
        }

        if (targetDataLine != null)
            audioInputStream = new AudioInputStream(targetDataLine);

        outputFile = new File(outputFileName);

    }


    //##############################################################################################################
    //######    methods


    public void startRecording() {

        targetDataLine.start();
        new Thread(getVoiceFromMic).start();
    }


    //##############################################################################################################
    //######    inner classes


    private class GetVoiceFromMic implements Runnable {

        @Override
        public void run() {

            try {
                AudioSystem.write(audioInputStream, audioFileFormat, outputFile);
            } catch (IOException e) {
                LOG.info("GET VOICE FROM MIC : ");
                e.printStackTrace();
            }
        }
    }


}
