package multithreading;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 这个例子展示了线程池的用法，将创建的线程通过submit方法提交到线程池中统一管理
 * @author hzc
 * @date 2016年10月10日下午8:11:00
 */
public class ThreadPoolTest {

	public static void main(String[] args) {
		Scanner in = new Scanner(System.in);
		System.out.println("Enter base directory:(e.g./usr/local/jdk1.6.0/src):");
		String directory = in.nextLine();
		System.out.println("Enter keyword(e.g. volatile)");
		String keyword = in.nextLine();
		
		ExecutorService threadPool = Executors.newCachedThreadPool();
		
		MatchCounterTwo counter = new MatchCounterTwo(new File(directory), keyword, threadPool);
		Future<Integer> result = threadPool.submit(counter);
		try {
			System.out.println(result.get() + " matching files.");
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int largestPoolSize = ((ThreadPoolExecutor)threadPool).getLargestPoolSize();
		System.out.println("largest pool size=" + largestPoolSize);
	}

}

class MatchCounterTwo implements Callable<Integer> {
	private File directory;
	private String keyword;
	private ExecutorService threadPool;
	private int count;

	public Integer call() throws Exception {
		count = 0;
		File[] files = directory.listFiles();
		List<Future<Integer>> results = new ArrayList<>();

		for (File file : files)
			if (file.isDirectory()) {
				MatchCounterTwo counter = new MatchCounterTwo(file, keyword, threadPool);
				Future<Integer> result = threadPool.submit(counter);
				results.add(result);
			} else {
				if (search(file))
					count++;
			}
		for(Future<Integer> result : results){
			try {
				count += result.get();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
			
		}
		return count;
	}

	public MatchCounterTwo(File directory, String keyword, ExecutorService threadPool) {
		this.directory = directory;
		this.keyword = keyword;
		this.threadPool = threadPool;
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
