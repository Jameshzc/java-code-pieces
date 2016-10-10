package multithreading;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

/**
 * 这个例子展示了Collable/Future/Runnable/FutureTask的使用
 * Collable其实和Runnable的区别其实就是Collable有返回值，Runnable没有，所以可以利用这一特性来使得线程返回一些结果，例如本例中的返回一个count
 * FutureTask包装器是一种非常便利的机制，可以将Callable转换成Future和Runnable
 * FutureTask实现了RunnableFuture接口，而RunnableFuture同时继承了Runnable和Future接口
 * @author hzc
 * @date 2016年10月10日下午7:40:28
 */
public class FutureTest {

	public static void main(String[] args) {
		Scanner in = new Scanner(System.in);
		System.out.println("Enter base directory:(e.g./usr/local/jdk1.6.0/src):");
		String directory = in.nextLine();
		System.out.println("Enter keyword(e.g. volatile)");
		String keyword = in.nextLine();
		
		MatchCounter counter = new MatchCounter(new File(directory), keyword);
		FutureTask<Integer> task = new FutureTask<>(counter);
		Thread t = new Thread(task);
		t.start();
		try {
			System.out.println(task.get() + " matching files.");
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
	}

}

/**
 * This task counts the file in a directory and its subdirectories that contain
 * a given keyword.
 */
class MatchCounter implements Callable<Integer> {
	private File directory;
	private String keyword;
	private int count;

	public MatchCounter(File directory, String keyword) {
		this.directory = directory;
		this.keyword = keyword;
	}

	public Integer call() throws Exception {
		count = 0;
		try {
			File[] files = directory.listFiles();
			List<Future<Integer>> results = new ArrayList<>();

			for (File file : files)
				if (file.isDirectory()) {
					MatchCounter counter = new MatchCounter(file, keyword);
					FutureTask<Integer> task = new FutureTask<>(counter);
					results.add(task);
					Thread t = new Thread(task);
					t.start();
				} else {
					if (search(file))
						count++;
				}
			for (Future<Integer> result : results) {
				try {
					count += result.get();
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
			}
		} catch (InterruptedException e) {

		}
		return count;
	}

	/**
	 * Search a file for a given keyword.
	 * 
	 * @param file
	 * @return
	 */
	public boolean search(File file) {
		try {
			try (Scanner in = new Scanner(file)) {
				boolean found = false;
				while (!found && in.hasNextLine()) {
					String line = in.nextLine();
					if (line.contains(keyword))
						found = true;
				}
				return found;
			}
		} catch (IOException e) {
			return false;
		}
	}

}