
package blockingQueue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * @author hzc
 * @date 2016年10月10日下午3:21:53     这个例子采用阻塞队列来实现多线程同步，展示了如何利用阻塞队列来控制一组线程。
 *       程序功能是在一个目录以及它的所有子目录下搜索所有文件，打印出包含指定关键字的行。
 */
public class BlockingQueueTest {
	public static void main(String[] args) {
		Scanner in = new Scanner(System.in);
		System.out.println("Enter base directory:(e.g./usr/local/jdk1.6.0/src):");
		String directory = in.nextLine();
		System.out.println("Enter keyword(e.g. volatile)");
		String keyword = in.nextLine();
		final int FILE_QUEUE_SIZE = 10;
		final int SEARCH_THREADS = 100;
		BlockingQueue<File> queue = new ArrayBlockingQueue<>(FILE_QUEUE_SIZE);

		FileEnumerationTask enumerator = new FileEnumerationTask(queue, new File(directory));
		new Thread(enumerator).start();
		for (int i = 1; i <= SEARCH_THREADS; i++)
			new Thread(new SearchTask(queue, keyword)).start();
	}
}

// 文件枚举线程
class FileEnumerationTask implements Runnable {
	public static File DUMMY = new File("");
	private BlockingQueue<File> queue;
	private File startDirectory;

	public FileEnumerationTask(BlockingQueue<File> queue, File startDirectory) {
		this.queue = queue;
		this.startDirectory = startDirectory;
	}

	public void run() {
		try {
			enumerate(startDirectory);
			queue.put(DUMMY);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	public void enumerate(File directory) throws InterruptedException {
		File[] files = directory.listFiles();
		for (File file : files) {
			if (file.isDirectory())
				enumerate(file);
			else
				queue.put(file);
		}
	}
}

// 关键字搜索线程
class SearchTask implements Runnable {
	private BlockingQueue<File> queue;
	private String keyword;

	public SearchTask(BlockingQueue<File> queue, String keyword) {
		this.queue = queue;
		this.keyword = keyword;
	}

	public void run() {
		try {
			boolean done = false;
			while (!done) {
				File file = queue.take();
				if (file == FileEnumerationTask.DUMMY) {
					queue.put(file);
					done = true;
				} else
					search(file);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO: handle exception
		}
	}

	public void search(File file) throws FileNotFoundException {
		try (Scanner in = new Scanner(file)) {
			int lineNumber = 0;
			while (in.hasNextLine()) {
				lineNumber++;
				String line = in.nextLine();
				if (line.contains(keyword))
					System.out.printf("%s:%d:%s%n", file.getPath(), lineNumber, line);
			}
		}
	}
}
