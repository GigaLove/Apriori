package com.iip.tool;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ReadData {

	public static List<String[]> readCommaFile(String fileName) {
		// 判断文件名是否为空
		List<String[]> dataList = null;
		
		if (fileName != null && !fileName.equals("")) {

			try {
				File csvFile = new File(fileName);
				BufferedReader br = new BufferedReader(new FileReader(csvFile));

				String line = "";
				dataList = new ArrayList<String[]>();

				while ((line = br.readLine()) != null) {
					String[] dataStr = line.split(",");
					dataList.add(dataStr);
				}

				br.close();

////				for (String[] strArray : dataList) {
////					System.out.println(Arrays.toString(strArray));
////				}
//
//				System.out.println("\n the size of the data set: "
//						+ dataList.size());
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return dataList;
	}

	public void readTxt(String txtFileName) {
		if (txtFileName != null && !txtFileName.equals("")) {
			try {
				File csvFile = new File(txtFileName);
				BufferedReader br = new BufferedReader(new FileReader(csvFile));

				String line = "";
				List<String> strList = new ArrayList<String>();

				while ((line = br.readLine()) != null) {
					strList.add(line);
				}

				br.close();

				for (String str : strList) {
					System.out.println(str);
				}

				System.out.println("\n the size of the data set: "
						+ strList.size());
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		Scanner in = new Scanner(System.in);

		System.out.println("Please input the file name:");
		String fileName = in.next();

		if (fileName != null && !fileName.equals("")) {
			String fileType = fileName.substring(fileName.indexOf(".") + 1,
					fileName.length());

			ReadData rd = new ReadData();

			if (fileType.equals("csv")) {
				ReadData.readCommaFile(fileName);

			} else if (fileType.equals("txt")) {
				rd.readTxt(fileName);
			}
		}
	}
}
