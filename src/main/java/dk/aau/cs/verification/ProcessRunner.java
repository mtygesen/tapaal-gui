package dk.aau.cs.verification;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.List;

import com.sun.jna.Platform;
import dk.aau.cs.debug.Logger;
import dk.aau.cs.util.MemoryMonitor;

public class ProcessRunner {
	private final String file;
	private final String[] options;

	private long runningTime = 0;
	private Process process;
	private BufferedReader bufferedReaderStdout;
	private BufferedReader bufferedReaderStderr;

	private boolean error = false;

	public ProcessRunner(String file, String[] options, ModelChecker modelChecker) {
		// this.setName("verification thread");

		if (file == null || file.isEmpty()) {
			if (modelChecker == null) {
				throw new IllegalArgumentException("file");
			}

			modelChecker.setup(); // Ask user to setup engine
		}

		this.file = file;
		this.options = options;
	}


	public ProcessRunner(String file, String[] options) {
		this(file, options, null);
	}

	public ProcessRunner(String file, String option, ModelChecker modelChecker) {
		this(file, new String[] { option }, modelChecker);
	}

	public ProcessRunner(String file, String option) {
		this(file, option, null);
	}

	public long getRunningTime() {
		return runningTime;
	}

	public BufferedReader standardOutput() {
		return bufferedReaderStdout;
	}

	public BufferedReader errorOutput() {
		return bufferedReaderStderr;
	}

	public boolean error() {
		return error;
	}

	public void kill() {
		if (process != null) {
			process.destroy();
		}
	}

	public void run() {
		long startTimeMs = 0, endTimeMs = 0;
		startTimeMs = System.currentTimeMillis();
		
		try {
			if (Platform.isWindows()) {
				Logger.log("Running: "+ "\"" + file + "\"" + " " + String.join(" ", options));
			} else {
				Logger.log("Running: "+ file + " " + String.join(" ", options));
			}

			String[] newOptions = new String[options.length + 1];
			newOptions[0] = file;
			for (int i = 0; i < options.length; i++) {
				newOptions[i + 1] = options[i];
			}

			System.out.println(String.join(" ", newOptions));
			process = Runtime.getRuntime().exec(newOptions);
			MemoryMonitor.attach(process);
		} catch (IOException e1) {
			error = true;
			return;
		}

		//Wrapping in BufferDrain as windows has really small buffers.
		BufferDrain stdout = new BufferDrain(new BufferedReader(new InputStreamReader(process.getInputStream())));
		BufferDrain stderr = new BufferDrain(new BufferedReader(new InputStreamReader(process.getErrorStream())));

        stdout.start();
        stderr.start();

		try {
			process.waitFor();
		} catch (InterruptedException e) {
			error = true;
			return;
		}
		endTimeMs = System.currentTimeMillis();

		try {
			stdout.join();
			stderr.join();
		} catch (InterruptedException e) {
			error = true;
			return;
		}

		bufferedReaderStdout = new BufferedReader(new StringReader(stdout.getString()));
		bufferedReaderStderr = new BufferedReader(new StringReader(stderr.getString()));

		runningTime = endTimeMs - startTimeMs;
	}
}
