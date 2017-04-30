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
				// �����½��ļ�ռλ
				File localFile = new File(path.substring(path.lastIndexOf("/")));
				RandomAccessFile randomAccessFile = new RandomAccessFile(localFile, "rw");
				long fileSize = connection.getContentLengthLong();
				randomAccessFile.setLength(fileSize); // ����ռλ�ļ���С
				randomAccessFile.close();	//�ر��ļ���
				blockSize = fileSize / (long) threadCount; // ����ÿ���߳�Ҫ���ص��ļ����С
				for (int i = 0; i < threadCount; i++) { // ÿѭ��һ�ο���һ���߳�
					startIndex = i * blockSize; // ��ʼ���ص���λ��
					endIndex = (i + 1) * blockSize - 1; // �������ص���λ��
					// �ж��Ƿ������һ��������
					if (i == threadCount - 1) {
						endIndex = fileSize - 1;
					}
					// ÿ��ѭ�� ����һ���߳�
					Runnable runnable = new Runnable() {
						public long start; // �����ⲿ��startIndex
						public long end; // �����ⲿ��endIndex
						{
							//�����̵߳��첽�Ե��� �߳��ڷ���startIndex��endIndex��ֵ��forѭ��֮��һ�θ���ֵ
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

								// ���ؿ�ʼ��
								int code = connection.getResponseCode();
								if (code == 200) { // 206��ʾ������Դ����ɹ���???
									InputStream inputStream = connection.getInputStream(); // ��ȡ��������
									RandomAccessFile randomAccessFile = new RandomAccessFile(
											new File(path.substring(path.lastIndexOf("/") + 1)), "rw");
									randomAccessFile.seek(start);	//Ѱ�Ҷ�Ӧλ��д��
									// ��д���ļ�
									byte[] buffer = new byte[1024];
									int length = -1;
									while ((length = inputStream.read(buffer)) != -1) {
										randomAccessFile.write(buffer, 0, length);
									}
									inputStream.close();
									randomAccessFile.close();
									System.out.println(
											"�߳�:" + Thread.currentThread() + "������ϣ�������λ�ã�" + start + "---" + end);
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					};

					// �����߳�
					new Thread(runnable).start();
				}
				// �����߳�ѭ������
				System.out.println("�������");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
