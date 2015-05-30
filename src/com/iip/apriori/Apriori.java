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
	 * 统计初始数据集，获取L1频繁项集
	 * 
	 * @param dataList
	 * @return
	 */
	private Map<String, Integer> getL1ItemSets(List<String[]> dataList) {
		// 将一项集添加到hashmap中，进行初始统计
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
		// 过滤L1候选集中小于阈值的项集
		filterCk2Lk(initItemSets);

		return initItemSets;
	}

	/**
	 * 过滤候选集小于阈值的项集，获取Lk
	 * 
	 * @param ck
	 */
	private void filterCk2Lk(Map<String, Integer> ck) {
		// 过滤不满足最小支持度的数据项
		Iterator<Entry<String, Integer>> iter = ck.entrySet().iterator();
		List<String> delList = new ArrayList<String>();

		while (iter.hasNext()) {
			Entry<String, Integer> entry = iter.next();
			String key = entry.getKey();
			Integer count = entry.getValue();

			if (count < MIN_SUP_COUNT) {
				// 将不满足支持度阈值的项添加到delList中
				delList.add(key);
			}
		}

		// 迭代delList，进行删除
		for (String str : delList) {
			ck.remove(str);
		}
	}

	/**
	 * Apriori算法核心框架
	 * 
	 * @param fileName
	 */
	public void run(String fileName) {
		// 获取初始数据集
		List<String[]> dataList = ReadData.readCommaFile(fileName);
		// 声明Lk List
		List<Map<String, Integer>> lkList = new ArrayList<Map<String, Integer>>();
		// 获取频繁一项集
		Map<String, Integer> l1 = getL1ItemSets(dataList);
		lkList.add(l1);

		int k = 1;
		while (lkList.get(k - 1).size() != 0) {
			Map<String, Integer> preLk = lkList.get(k - 1);
			Map<String, Integer> ck = aprioriGen(preLk); // 由Lk-1获取Ck候选集

			for (String[] tRecord : dataList) {
				Set<String> ct = subset(ck, tRecord); // 获取当前事务中满足Ck中的子集

				for (String str : ct) {
					ck.put(str, ck.get(str) + 1); // 统计计数
				}
			}

			// 过滤候选集，并将过滤后的结果添加到频繁项集list中
			filterCk2Lk(ck);
			lkList.add(ck);
			k++;
		}

		printFreItemSets(lkList);
	}

	/**
	 * 输出频繁项集的统计结果
	 * 
	 * @param lkList
	 *            频繁项集list
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
	 * 获取事务数据中满足的Ck的子集
	 * 
	 * @param ck
	 *            候选集
	 * @param tRecord
	 *            事务数据
	 * @return Ct
	 */
	private Set<String> subset(Map<String, Integer> ck, String[] tRecord) {
		Set<String> ct = new HashSet<String>();
		List<String> tList = Arrays.asList(tRecord);

		Iterator<Entry<String, Integer>> iter = ck.entrySet().iterator();

		while (iter.hasNext()) {
			Entry<String, Integer> entry = iter.next();
			String key = entry.getKey();

			// 判断是否Ck中的项是否是trecord的子集，是则添加到Ct中
			if (isSubset(key.split(","), tList)) {
				ct.add(key);
			}
		}

		return ct;
	}

	/**
	 * 判断是否Ck中的项是否是trecord的子集
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
	 * 基于Lk-1生成Ck
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
			List<String> l1List = sortStrArray(l1);	// 按照字典序进行排序
			for (int j = 0; j < keyList.size(); j++) {
				String l2 = keyList.get(j);
				List<String> l2List = sortStrArray(l2);	// 按照字典序进行排序

				List<String> c = null;
				
				// 判断是否可以连接
				if (isAttachable(l1List, l2List)) {
					c = new ArrayList<String>(l1List);
					c.add(l2List.get(l2List.size() - 1));
					
					// 判断生成的项集的所有子集是否都在Lk-1中，进行剪枝操作
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
	 * 将string list转换成","分割的string 进而充当CK的hashmap的key
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
	 * 剪枝操作，判断Ck候选集中的(K-1)子集是否都在Lk-1中
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

			// 复制c， 依次移除每一项，判断是否在Lk-1中
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
	 * 判断L1项集和L2项集是否可以连接
	 * 
	 * @param l1
	 *            L1项集
	 * @param l2
	 *            L2项集
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
	 * 按照字典序排序原始项集
	 * @param str	项集string
	 * @return	排序号的项集list
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
		System.out.println("频繁项集统计结果为：");
		new Apriori().run("data.txt");
	}

}
