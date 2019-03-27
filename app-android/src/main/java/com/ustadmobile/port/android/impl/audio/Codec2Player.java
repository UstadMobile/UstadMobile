package com.ustadmobile.port.android.impl.audio;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import com.ustadmobile.codec2.Codec2;
import com.ustadmobile.codec2.Codec2Decoder;
import com.ustadmobile.core.util.UMIOUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

public class Codec2Player implements Runnable {

    private final String filePath;
    private final long pos;
    private AtomicBoolean playing;

    public Codec2Player(String filePath, long pos) {
        this.filePath = filePath;
        this.pos = pos;
        playing = new AtomicBoolean();

    }

    public void play() {
        playing.set(true);
        new Thread(this).start();
    }

    public void stop() {
        playing.set(false);
    }


    @Override
    public void run() {
        AudioTrack track = null;
        Codec2Decoder codec2 = null;
        InputStream is = null;
        try {
            int bufSize = AudioTrack.getMinBufferSize(8000,
                    AudioFormat.CHANNEL_CONFIGURATION_MONO,
                    AudioFormat.ENCODING_PCM_16BIT);

            track = new AudioTrack(
                    AudioManager.STREAM_VOICE_CALL,
                    8000,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufSize,
                    AudioTrack.MODE_STREAM);
            track.play();

            is = new FileInputStream(filePath);

            codec2 = new Codec2Decoder(is, Codec2.CODEC2_MODE_3200);
            int headerSize = 7;
            float frameDurationMs = (float) codec2.getSamplesPerFrame() / 8f;
            int framesToSkip = Math.round(pos / frameDurationMs);
            is.skip(headerSize + framesToSkip * codec2.getInputBufferSize());
            ByteBuffer buffer;
            while (playing.get() && (buffer = codec2.readFrame()) != null) {
                track.write(buffer.array(), 0, buffer.capacity());
                System.out.println("Wrote track data");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (track != null) {
                track.release();
            }
            if (codec2 != null) {
                codec2.destroy();
            }
            UMIOUtils.closeInputStream(is);
        }

    }
}
