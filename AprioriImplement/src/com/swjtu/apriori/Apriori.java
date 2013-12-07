package com.swjtu.apriori;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import jxl.read.biff.BiffException;
public class Apriori {
	private int minSup;
	private static List<String> data;
	private static List<Set<String>> dataSet;
	/**
	 * @param args
	 * @throws IOException 
	 * @throws BiffException 
	 */
	public static void main(String[] args) throws BiffException, IOException {

		long startTime = System.currentTimeMillis();
		Apriori apriori = new Apriori();
		apriori.setMinSup(4);									//设置最小支持度
		data = apriori.buildData();								//构造数据集
		List<Set<String>> f1Set = apriori.findF1Items(data);	//构造频繁1项集
		apriori.printSet(f1Set, 1);
		List<Set<String>> result = f1Set;
		
		int i = 2;
		do{
			result = apriori.arioriGen(result);
			apriori.printSet(result, i);
			i++;
		}while(result.size()>0);

		long endTime = System.currentTimeMillis();
		System.out.println("共用时：" + (endTime - startTime) + "ms");
	}
	
	/**
	 * 构造原始数据集，可以为之提供参数，也可以不提供
	 * 如果不提供参数，将按程序默认构造的数据集；
	 * 如果提供参数为文件名，则使用文件中的数据集
	 * @return
	 * @throws IOException 
	 * @throws BiffException 
	 */
	List<String> buildData(String...fileName) throws BiffException, IOException {
		List<String> transactionData = new ArrayList<String>();	//交易事务数据
		if(fileName.length !=0){
			File file = new File(fileName[0]);
			try {
				BufferedReader reader = new BufferedReader(new FileReader(file));
				String line;
				while( (line = reader.readLine()) != null){
					transactionData.add(line);
				}
				
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				
				e.printStackTrace();
			}
		}else{
			transactionData = GetDataFromExcel.getData();
			/*
			transactionData.add("I1 I3 I4");
			transactionData.add("I2 I3 I5");
			transactionData.add("I1 I2 I3 I5");
			transactionData.add("I2 I5");
			*/
		}
		
		dataSet = new ArrayList<Set<String>>();
		Set<String> goodsSet;
		for (String d : transactionData) {				//遍历data中每个事务记录
			goodsSet = new TreeSet<String>();	//构造一个Set(Set不允许有重复值 )，用于存放每个事务中所购买的的商品
			String[] goodsArr = d.split(" ");	//将每条事务记录中涉及的商品分离出来
			for (String str : goodsArr) {
				goodsSet.add(str);
			}
			dataSet.add(goodsSet);
		}
		return transactionData;
	}

	/**
	 * 找出候选1项集
	 * @param data
	 * @return
	 */
	List<Set<String>> findF1Items(List<String> data) {
		List<Set<String>> result = new ArrayList<Set<String>>();
		Map<String, Integer> goodsMap = new HashMap<String, Integer>();
		for (String d : data) {				//遍历data中每条事务记录
			String[] items = d.split(" ");  //得到每条事务记录中商品
			for (String item : items) {
				if (goodsMap.containsKey(item)) {
					goodsMap.put(item, goodsMap.get(item) + 1);
				} else {
					goodsMap.put(item, 1);
				}
			}
		}
		for (String item : goodsMap.keySet()) {
			if (goodsMap.get(item) >= minSup) {
				Set<String> f1Set = new TreeSet<String>(); //
				f1Set.add(item);
				result.add(f1Set);
			}
		}

		return result;
	}

	
	/**
	 * 利用arioriGen方法由k-1项集生成k项集
	 * @param preSet
	 * @return
	 */
	List<Set<String>> arioriGen(List<Set<String>> preSet) {
		List<Set<String>> result = new ArrayList<Set<String>>();
		int preSetSize = preSet.size();
		for (int i = 0; i < preSetSize - 1; i++) {
			for (int j = i + 1; j < preSetSize; j++) {
				String[] strA1 = preSet.get(i).toArray(new String[0]);
				String[] strA2 = preSet.get(j).toArray(new String[0]);
				if (isCanLink(strA1, strA2)) { // 判断两个k-1项集是否符合连接成k项集的条件　
					Set<String> set = new TreeSet<String>();
					for (String str : strA1) {
						set.add(str);
					}
					set.add((String) strA2[strA2.length - 1]); // 连接成k项集
					
					if (!isNeedCut(preSet, set)) {// 判断k项集是否需要剪切掉，如果不需要被cut掉，则加入到k项集列表中
						result.add(set);
					}
				
				}

			}
		}
		return checkSupport(result);
	}

	
	
	/**
	 * 获取满足最小支持度的频繁集
	 * @param set
	 * @return
	 */
	List<Set<String>> checkSupport(List<Set<String>> setList) {
		List<Set<String>> result = new ArrayList<Set<String>>();
		boolean flag = true;
		int[] counter = new int[setList.size()];	//构造一个数组用于记录,候选集的出现次数
		for (int i = 0; i < setList.size(); i++) {  //遍历候选集
			for (Set<String> dSets : dataSet) {		//遍历原始事务数据
					for (String str : setList.get(i)) {
						if (!dSets.contains(str)) {		//该原始事务数据中没有这个组合的数据
							flag = false;
							break;
						}
					}
					if (flag) {				//在原始事务数据中出现，则计数加1
						counter[i] += 1;
					} else {
						flag = true;
					}
			}
		}

		for (int i = 0; i < setList.size(); i++) {
			if (counter[i] >= minSup) {		//符合最小支持度，则返回 
				result.add(setList.get(i));
			}
		}
		return result;
	}
	
	
	
	

	/**
	 * 判断两个项集合能否执行连接操作
	 * 
	 * @param s1
	 * @param s2
	 * @return
	 */
	boolean isCanLink(String[] s1, String[] s2) {
		boolean flag = true;
		if (s1.length == s2.length) {
			for (int i = 0; i < s1.length - 1; i++) {
				if (!s1[i].equals(s2[i])) {
					flag = false;
					break;
				}
			}
			if (s1[s1.length - 1].equals(s2[s2.length - 1])) {
				flag = false;
			}
		} else {
			flag = false;
		}

		return flag;
	}

	/**
	 * 判断set是否需要被cut
	 * @param setList
	 * @param set
	 * @return
	 */
	boolean isNeedCut(List<Set<String>> setList, Set<String> set) {
		boolean flag = false;
		List<Set<String>> subSets = getSubset(set); // 获得k项集的所有k-1项集
		for (Set<String> subSet : subSets) {
			// 判断当前的k-1项集set是否在频繁k-1项集中出现，如出现，则不需要cut
			// 若没有出现，则需要被cut
			if (!isContained(setList, subSet)) {
				flag = true;
				break;
			}
		}
		return flag;
	}

	
	/**
	 * 判断k项集的某k-1项集是否包含在频繁k-1项集列表中
	 * @param setList
	 * @param set
	 * @return
	 */
	boolean isContained(List<Set<String>> setList, Set<String> set) {
		boolean flag = false;
		int position = 0;
		for (Set<String> s : setList) {
			String[] sArr = s.toArray(new String[0]);
			String[] setArr = set.toArray(new String[0]);
			for (int i = 0; i < sArr.length; i++) {
				if (sArr[i].equals(setArr[i])) { // 如果对应位置的元素相同，则position为当前位置的值
					position = i;
				} else {
					break;
				}
			}
			// 如果position等于了数组的长度，说明已找到某个setList中的集合与
			// set集合相同了，退出循环，返回包含
			// 否则，把position置为0进入下一个比较
			if (position == sArr.length - 1) {
				flag = true;
				break;
			} else {
				flag = false;
				position = 0;
			}

		}
		return flag;
	}

	/**
	 * 获得k项集的所有k-1项集
	 * @param set
	 * @return
	 */
	List<Set<String>> getSubset(Set<String> set) {
		List<Set<String>> result = new ArrayList<Set<String>>();
		String[] setArr = set.toArray(new String[0]);
		for (int i = 0; i < setArr.length; i++) {
			Set<String> subSet = new TreeSet<String>();
			for (int j = 0; j < setArr.length; j++) {
				if (i != j) {
					subSet.add((String) setArr[j]);
				}
			}
			result.add(subSet);
		}
		return result;
	}

	
	/**
	 * 打印频繁集
	 * @param setList
	 * @param i
	 */
	private void printSet(List<Set<String>> setList, int i){
		String outString = new String();
		outString="频繁"+i+"项集： 共" +  setList.size()+"项：{";
		for (Set<String> set : setList) {
			outString = outString+"[";
			for (String str : set) {
				outString=outString+str+" ";
			}
			outString = outString.substring(0,outString.lastIndexOf(" "));
			outString = outString+"]"+",";
		}
		if(outString.lastIndexOf(",")!=-1){
			outString = outString.substring(0,outString.lastIndexOf(","));
		}
		outString = outString+"}"+"\n";
		System.out.println(outString);
	}

	
	public void setMinSup(int minSup) {
		this.minSup = minSup;
	}

}
