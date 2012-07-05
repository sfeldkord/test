package test;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;

public class LineCounter {
	private static final String EXTENSION = ".java";
	private static final String USAGE = "LineCounter <root directory>";
	private long totalLOC = 0L;
	private long effectiveLOC = 0L;
	private String currentLine;
	private boolean multiLineComment = false;

	private boolean isEffective() {
		filterComments();
		if (currentLine.trim().length() <= 1)
			return false;
		return true;
	}

	private void filterComments() {
		char[] array = currentLine.toCharArray();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < array.length - 1; i++) {
			char current = array[i];
			char next = array[i + 1];
			if (multiLineComment) {
				if (current == '*' && next == '/') {
					multiLineComment = false;
					i++;
				}
			} else {
				if (current == '/') {
					if (next == '*') {
						multiLineComment = true;
						i++;
					} else if (next == '/') {
						i = array.length;
					} else {
						sb.append(current);
						sb.append(next);
						i++;
					}
				} else {
					sb.append(current);
				}
			}
		}
		currentLine = sb.toString();
	}

	private void count(File file) {
		LineNumberReader reader = null;
		try {
			reader = new LineNumberReader(new FileReader(file));
			currentLine = reader.readLine();
			while (currentLine != null) {
				totalLOC++;
				if (isEffective())
					effectiveLOC++;
				currentLine = reader.readLine();
			}
		} catch (IOException e) {
			System.out.println(e.getMessage());
			System.out.println(USAGE);
		} finally {
			if (reader != null)
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}

	private void iterate(File root) {
		for (File file : root.listFiles()) {
			if (file.isDirectory())
				iterate(file);
			else if (file.getName().endsWith(EXTENSION))
				count(file);
		}
	}

	private void printStatistics() {
		System.out.printf("Total Lines of Code: %,d\n", totalLOC);
		System.out.printf("Effective Lines of Code: %,d\n", effectiveLOC);
		System.out.printf("Ratio: %3.2f%%\n", 100.0 * effectiveLOC / totalLOC);
	}

	public static void main(String[] args) {
		if (args.length != 1) {
			System.out.println(USAGE);
			System.exit(-1);
		}
		LineCounter counter = new LineCounter();
		counter.iterate(new File(args[0]));
		counter.printStatistics();
	}
}
