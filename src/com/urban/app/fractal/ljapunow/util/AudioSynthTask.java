package com.urban.app.fractal.ljapunow.util;

import java.util.Random;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.AsyncTask;

public class AudioSynthTask extends AsyncTask<Void, Void, Void>
{
	private static final int	SAMPLE_RATE	= 11025;

	private boolean				play;
	private float				A_frequency, B_frequency;
	private char[]				sequence;

	public AudioSynthTask(char[] sequence)
	{
		this.play = true;
		this.sequence = sequence;
		this.A_frequency = 50f + (250f * new Random(System.currentTimeMillis()).nextFloat());
		this.B_frequency = 300f + (300f * new Random((long) this.A_frequency + System.currentTimeMillis()).nextFloat());
	}

	public void stop()
	{
		play = false;
	}

	@Override
	protected Void doInBackground(Void... params)
	{
		// Start up an audio track
		final int minSize = AudioTrack.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_CONFIGURATION_STEREO, AudioFormat.ENCODING_PCM_16BIT);
		final AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, SAMPLE_RATE, AudioFormat.CHANNEL_CONFIGURATION_STEREO, AudioFormat.ENCODING_PCM_16BIT, minSize, AudioTrack.MODE_STREAM);
		audioTrack.play();
		audioTrack.setStereoVolume(0.25f, 0.25f);

		// Audio buffer to fill in each turn
		final short[] buffer = new short[minSize];

		// Play as long as not explicitly stopped
		int s = 0;
		long start = System.currentTimeMillis();
		float angle = 0, angular_frequency, frequency, period = 1;
		while (play)
		{
			frequency = (sequence[s] == 'A') ? A_frequency : B_frequency;
			if (((System.currentTimeMillis() - start) / 1000f) >= period)
			{
				s = (s + 1 == sequence.length) ? 0 : s + 1;
				start = System.currentTimeMillis();
				period = 0.5f + (2f * new Random(start).nextFloat());
			}

			angular_frequency = (float) (2 * Math.PI) * frequency / SAMPLE_RATE;
			for (int i = 0; i < buffer.length; i++)
			{
				buffer[i] = (short) (4000 + ((Short.MAX_VALUE - 8000) * ((float) Math.sin(angle))));
				angle += angular_frequency;
			}
			audioTrack.write(buffer, 0, buffer.length);
		}

		audioTrack.stop();
		return null;
	}
}
