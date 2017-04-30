package moreThreadDownload;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

public class Demo {
	private static int threadCount = 3;
	private static long blockSize = 0;
	private static String path = "http://120.25.3.174/putty.exe";
	// private static String path = "http://120.25.3.174/morethreaddown.txt";

	private static long startIndex;
	private static long endIndex;

	public static void main(String[] args) {
		try {
			URL url = new URL(path);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setReadTimeout(1000 * 10);
			connection.setRequestMethod("GET");

			if (connection.getResponseCode() == 200) {
				// 本地新建文件占位
				File localFile = new File(path.substring(path.lastIndexOf("/")));
				RandomAccessFile randomAccessFile = new RandomAccessFile(localFile, "rw");
				long fileSize = connection.getContentLengthLong();
				randomAccessFile.setLength(fileSize); // 设置占位文件大小
				randomAccessFile.close();	//关闭文件流
				blockSize = fileSize / (long) threadCount; // 计算每个线程要下载的文件块大小
				for (int i = 0; i < threadCount; i++) { // 每循环一次开启一个线程
					startIndex = i * blockSize; // 开始下载的流位置
					endIndex = (i + 1) * blockSize - 1; // 结束下载的流位置
					// 判断是否是最后一块数据流
					if (i == threadCount - 1) {
						endIndex = fileSize - 1;
					}
					// 每次循环 开启一个线程
					Runnable runnable = new Runnable() {
						public long start; // 保存外部的startIndex
						public long end; // 保存外部的endIndex
						{
							//避免线程的异步性导致 线程内访问startIndex和endIndex的值是for循环之后一次赋的值
							start = startIndex;	
							end = endIndex;
						}

						@Override
						public void run() {
							try {
								URL url = new URL(path);
								HttpURLConnection connection = (HttpURLConnection) url.openConnection();
								connection.setReadTimeout(1000 * 10);
								connection.setRequestMethod("GET");
								connection.setRequestProperty("Range", "bytes:" + start + "-" + end);

								// 下载开始咯
								int code = connection.getResponseCode();
								if (code == 200) { // 206表示部分资源请求成功，???
									InputStream inputStream = connection.getInputStream(); // 获取请求数据
									RandomAccessFile randomAccessFile = new RandomAccessFile(
											new File(path.substring(path.lastIndexOf("/") + 1)), "rw");
									randomAccessFile.seek(start);	//寻找对应位置写入
									// 流写入文件
									byte[] buffer = new byte[1024];
									int length = -1;
									while ((length = inputStream.read(buffer)) != -1) {
										randomAccessFile.write(buffer, 0, length);
									}
									inputStream.close();
									randomAccessFile.close();
									System.out.println(
											"线程:" + Thread.currentThread() + "下载完毕，数据流位置：" + start + "---" + end);
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					};

					// 开启线程
					new Thread(runnable).start();
				}
				// 结束线程循环创建
				System.out.println("运行完毕");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
