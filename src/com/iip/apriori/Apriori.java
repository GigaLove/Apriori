package com.iip.apriori;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.iip.tool.ReadData;

public class Apriori {
	private static final int MIN_SUP_COUNT = 2;

	/**
	 * ͳ�Ƴ�ʼ���ݼ�����ȡL1Ƶ���
	 * 
	 * @param dataList
	 * @return
	 */
	private Map<String, Integer> getL1ItemSets(List<String[]> dataList) {
		// ��һ���ӵ�hashmap�У����г�ʼͳ��
		Map<String, Integer> initItemSets = new HashMap<String, Integer>();

		for (String[] dataItem : dataList) {
			if (dataItem.length > 1) {
				for (int i = 1; i < dataItem.length; i++) {
					if (initItemSets.containsKey(dataItem[i])) {
						initItemSets.put(dataItem[i],
								initItemSets.get(dataItem[i]) + 1);
					} else {
						initItemSets.put(dataItem[i], 1);
					}
				}
			}
		}
		// ����L1��ѡ����С����ֵ���
		filterCk2Lk(initItemSets);

		return initItemSets;
	}

	/**
	 * ���˺�ѡ��С����ֵ�������ȡLk
	 * 
	 * @param ck
	 */
	private void filterCk2Lk(Map<String, Integer> ck) {
		// ���˲�������С֧�ֶȵ�������
		Iterator<Entry<String, Integer>> iter = ck.entrySet().iterator();
		List<String> delList = new ArrayList<String>();

		while (iter.hasNext()) {
			Entry<String, Integer> entry = iter.next();
			String key = entry.getKey();
			Integer count = entry.getValue();

			if (count < MIN_SUP_COUNT) {
				// ��������֧�ֶ���ֵ������ӵ�delList��
				delList.add(key);
			}
		}

		// ����delList������ɾ��
		for (String str : delList) {
			ck.remove(str);
		}
	}

	/**
	 * Apriori�㷨���Ŀ��
	 * 
	 * @param fileName
	 */
	public void run(String fileName) {
		// ��ȡ��ʼ���ݼ�
		List<String[]> dataList = ReadData.readCommaFile(fileName);
		// ����Lk List
		List<Map<String, Integer>> lkList = new ArrayList<Map<String, Integer>>();
		// ��ȡƵ��һ�
		Map<String, Integer> l1 = getL1ItemSets(dataList);
		lkList.add(l1);

		int k = 1;
		while (lkList.get(k - 1).size() != 0) {
			Map<String, Integer> preLk = lkList.get(k - 1);
			Map<String, Integer> ck = aprioriGen(preLk); // ��Lk-1��ȡCk��ѡ��

			for (String[] tRecord : dataList) {
				Set<String> ct = subset(ck, tRecord); // ��ȡ��ǰ����������Ck�е��Ӽ�

				for (String str : ct) {
					ck.put(str, ck.get(str) + 1); // ͳ�Ƽ���
				}
			}

			// ���˺�ѡ�����������˺�Ľ����ӵ�Ƶ���list��
			filterCk2Lk(ck);
			lkList.add(ck);
			k++;
		}

		printFreItemSets(lkList);
	}

	/**
	 * ���Ƶ�����ͳ�ƽ��
	 * 
	 * @param lkList
	 *            Ƶ���list
	 */
	private void printFreItemSets(List<Map<String, Integer>> lkList) {
		for (Map<String, Integer> map : lkList) {
			Iterator<Entry<String, Integer>> iter = map.entrySet().iterator();

			while (iter.hasNext()) {
				Entry<String, Integer> entry = iter.next();
				String key = entry.getKey();
				int count = entry.getValue();
				System.out.println(key + " : " + count);
			}
		}
	}

	/**
	 * ��ȡ���������������Ck���Ӽ�
	 * 
	 * @param ck
	 *            ��ѡ��
	 * @param tRecord
	 *            ��������
	 * @return Ct
	 */
	private Set<String> subset(Map<String, Integer> ck, String[] tRecord) {
		Set<String> ct = new HashSet<String>();
		List<String> tList = Arrays.asList(tRecord);

		Iterator<Entry<String, Integer>> iter = ck.entrySet().iterator();

		while (iter.hasNext()) {
			Entry<String, Integer> entry = iter.next();
			String key = entry.getKey();

			// �ж��Ƿ�Ck�е����Ƿ���trecord���Ӽ���������ӵ�Ct��
			if (isSubset(key.split(","), tList)) {
				ct.add(key);
			}
		}

		return ct;
	}

	/**
	 * �ж��Ƿ�Ck�е����Ƿ���trecord���Ӽ�
	 * 
	 * @param ckItem
	 * @param tRecord
	 * @return
	 */
	private boolean isSubset(String[] ckItem, List<String> tRecord) {
		boolean flag = true;

		for (int i = 0; i < ckItem.length; i++) {
			if (!tRecord.contains(ckItem[i])) {
				flag = false;
				break;
			}
		}

		return flag;
	}

	/**
	 * ����Lk-1����Ck
	 * 
	 * @param preLk
	 *            Lk-1
	 * @return Ck
	 */
	private Map<String, Integer> aprioriGen(Map<String, Integer> preLk) {
		List<String> keyList = new ArrayList<String>(preLk.keySet());

		Map<String, Integer> ck = new HashMap<String, Integer>();

		for (int i = 0; i < keyList.size(); i++) {
			String l1 = keyList.get(i);
			List<String> l1List = sortStrArray(l1);	// �����ֵ����������
			for (int j = 0; j < keyList.size(); j++) {
				String l2 = keyList.get(j);
				List<String> l2List = sortStrArray(l2);	// �����ֵ����������

				List<String> c = null;
				
				// �ж��Ƿ��������
				if (isAttachable(l1List, l2List)) {
					c = new ArrayList<String>(l1List);
					c.add(l2List.get(l2List.size() - 1));
					
					// �ж����ɵ���������Ӽ��Ƿ���Lk-1�У����м�֦����
					if (!isHasInfreQuentSubset(c, preLk)) {
						String strKey = strList2Str(c);
						ck.put(strKey, 0);
					}
				}

			}

		}

		return ck;
	}

	/**
	 * ��string listת����","�ָ��string �����䵱CK��hashmap��key
	 * 
	 * @param list
	 * @return str
	 */
	private String strList2Str(List<String> list) {
		String str = "";

		for (int i = 0; i < list.size() - 1; i++) {
			str += list.get(i) + ",";
		}

		str += list.get(list.size() - 1);
		return str;
	}

	/**
	 * ��֦�������ж�Ck��ѡ���е�(K-1)�Ӽ��Ƿ���Lk-1��
	 * @param c
	 *            Ck
	 * @param preLk
	 *            Lk-1
	 * @return
	 */
	private boolean isHasInfreQuentSubset(List<String> c,
			Map<String, Integer> preLk) {
		boolean flag = false;

		if (c != null && preLk != null) {
			List<String> subSet = null;

			// ����c�� �����Ƴ�ÿһ��ж��Ƿ���Lk-1��
			for (int i = 0; i < c.size(); i++) {
				subSet = new ArrayList<String>(c);
				subSet.remove(i);

				String subSetStr = strList2Str(subSet);
				if (!preLk.containsKey(subSetStr)) {
					flag = true;
					break;
				}
			}
		}

		return flag;
	}

	/**
	 * �ж�L1���L2��Ƿ��������
	 * 
	 * @param l1
	 *            L1�
	 * @param l2
	 *            L2�
	 * @return
	 */
	public boolean isAttachable(List<String> l1, List<String> l2) {
		boolean flag = true;

		if (l1 == null || l2 == null || l1.size() != l2.size()) {
			flag = false;
		} else if (l1.get(l1.size() - 1).compareTo(l2.get(l2.size() - 1)) >= 0) {
			flag = false;
		} else {
			for (int i = 0; i < l1.size() - 1; i++) {
				if (l1.get(i).equals(l2.get(i))) {
					flag = false;
					break;
				}
			}
		}

		return flag;
	}

	/**
	 * �����ֵ�������ԭʼ�
	 * @param str	�string
	 * @return	����ŵ��list
	 */
	private List<String> sortStrArray(String str) {
		String[] strArray = str.split(",");

		List<String> strList = new ArrayList<String>(Arrays.asList(strArray));
		Collections.sort(strList);

		return strList;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("Ƶ���ͳ�ƽ��Ϊ��");
		new Apriori().run("data.txt");
	}

}
