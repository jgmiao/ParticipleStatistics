package com.secneo.participle.extract;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import com.secneo.participle.domain.Cell;
import com.secneo.participle.util.StringUtils;
import com.secneo.participle.util.WriteFile;

/**
 * 打算将字典存为3份 1、存放所有单个字 --> single_word_dictionary.txt 2、存放所有可以确定App的词 -->
 * definite_app_dictionary.txt 类似于（愤怒的小鸟、360助手之类） 3、存放所有噪声词 -->
 * noise_word_dictionary.txt
 */
public class TrieTree4Chinese {
	static long COUNT_ALL = 0; // 统计所有划分词的总数
	static int MIN_WORD_NUM = 1; // 最小认同词的长度 [将长度为1的全部踢出]
	static int MIN_SUPPORT_FREQUEN = 1; // 最小支持词出现的频率
	static int MIN_SUPPORT_WHOLE = 1; // 最小支持整串形式出现的频率
	static int MIN_LORR_MATCH = 5; // 噪声词切割出的剩余串匹配相列表最小的支持数
	static float MIN_AGGREGATE_NUM = 10;// 最小的聚集支持度
	static int MIN_WHOLE_SUPPORT = 80; // 最小整串保护度 如果大于则该串不可删
	static int MIN_NOISE_SUB_MATCH = 20;// 最小噪声子串匹配项支持度 大于 则认为该子串为噪声
	static int MAX_WHOLE_MULTIPLE = 10; // 最大子串形式出现相对整串形式倍数 如果大于则删除子串
	static int MAX_WORD_LEN = 40; // 最大支持的子串长度

	static String PATH;
	static String STORAGE_ROUTE;
//	static String STORAGE_ROUTE = "F:\\participle\\Thesaurus(5w)\\";
//	static String PATH = "F:\\participle\\Souce\\appName(5w).txt";
	static int TRIM_TIMES = 0;
	static int MEMORY_TRIM_NUM = 0;
	
	static HashSet<String> DEL_SET = null;
	@SuppressWarnings("rawtypes")
	static HashMap[] BUFFER = new HashMap[1000]; // 暂存FREQUEN的各层数据
	@SuppressWarnings("rawtypes")
	static HashMap[] DEL_FREQUEN_BUFFER = new HashMap[1000]; // 暂存由频率删除的各层数据
	@SuppressWarnings("rawtypes")
	static HashMap[] DEL_AGGREGATE_BUFFER = new HashMap[1000]; // 暂存由相似性删除的各层数据
	@SuppressWarnings("rawtypes")
	static HashMap[] DEL_NOTWHOLE_BUFFER = new HashMap[1000]; // 暂存不是整串的各层数据
	@SuppressWarnings("rawtypes")
	static HashMap[] DEL_CUTWHOLE_BUFFER = new HashMap[1000]; // 暂存被确定词字串切割的整串
	@SuppressWarnings("rawtypes")
	static HashMap[] HIGH_MATCH_SUB_BUFFER = new HashMap[1000]; // 暂存高频左右匹配项的子串
	static HashMap<Integer, HashMap<String, Cell>> FREQUEN = new HashMap<Integer, HashMap<String, Cell>>();
	static HashMap<Integer, HashMap<String, Cell>> DEL_FREQUEN = new HashMap<Integer, HashMap<String, Cell>>();
	static HashMap<Integer, HashMap<String, Cell>> DEL_AGGREGATE = new HashMap<Integer, HashMap<String, Cell>>();
	static HashMap<Integer, HashMap<String, Cell>> DEL_NOTWHOLE = new HashMap<Integer, HashMap<String, Cell>>();
	static HashMap<Integer, HashMap<String, Cell>> DEL_CUTWHOLE = new HashMap<Integer, HashMap<String, Cell>>();
	static HashMap<Integer, HashMap<String, Cell>> HIGH_MATCH_SUB = new HashMap<Integer, HashMap<String, Cell>>();

	/**
	 * [main]提取高频词 apk_name对所有拆分的情况进行统计 并记录到内存中
	 */
	public static void extractFrequencyWords() {
		String s = null;
		try {
			File f = new File(PATH);
            BufferedReader br;
            br = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
            System.out.println("切词开始。。。");
            final long start = System.nanoTime();
			while ((s = br.readLine()) != null) {
				if(s.length() > MAX_WORD_LEN)
					continue;
				splitStatistic(StringUtils.stringFilter(s));
			}
			final long end = System.nanoTime();
			System.out.println("切词完毕  HashMap size：" + COUNT_ALL + "  用时： " + (end - start)/1.0e9 + "s");
			br.close();
		} catch (IOException e) {
			System.out.println("文件读取出错。。。");
			e.printStackTrace();
		}
		System.out.println("开始内存整理。。。");
		memoryTrim(); // 内存整理
		System.out.println("内存整理结束！");
	}// extractFrequencyWords

	/**
	 * [extractFrequencyWords]对App名进行拆分统计 
	 * 这里数据结构采用的是HashMap<Integer, HashMap<String, Cell>> 
	 * 最后在我们的HashMap中会存放两级数据，第一级是应用名长度，第二级为正儿八经的应用名统计量
	 * 在这里多加了一级 干了两件事
	 * 1、在后来为建立single_word_dictionary.txt大大的提供了方便
	 * 2、在进行子集驱逐时可以很快捷的进行由高位向地位的扫描
	 */
	@SuppressWarnings({ "unchecked" })
	public static void splitStatistic(String apkName) {
		int apkLen = apkName.length();
		for (int i = MIN_WORD_NUM + 1; i <= apkLen; i++) {// i为需要切分的长度
			for (int j = 0; j <= apkLen - i; j++) {
				COUNT_ALL++;
				String subName = apkName.substring(j, j + i); // 切割串
				int subLen = subName.length();
				Cell c = null;
				// 如果字串已经存在，则对原有统计进行增1操作
				if (isNotEmpty(FREQUEN, subName) && getCount(FREQUEN, subName) > 0) {
					c = getCell(FREQUEN, subName);
					int count = c.getCount();
					/**
					 * 对count操作
					 */
					c.setCount(++count);
					/**
					 * 对countWhole、countSub操作
					 */
					if (i == apkLen) {
						int countWhole = c.getCountWhole();
						c.setCountWhole(++countWhole);
					} else {
						int countSub = c.getCountSub();
						c.setCountSub(++countSub);
					}
				} else {// 初始化BUFFER
					BUFFER[subLen] = getHash(FREQUEN, subName);
					c = new Cell(1);
					if (i == apkLen) { // 对CountWhole、CountSub初始化
						c.setCountWhole(1);
					} else {
						c.setCountSub(1);
					}
				}
				/**
				 * 对前缀串&&后缀串的lMatch、rMatch操作
				 */
				String surplusName = apkName.replace(subName, "");
				if (i == 0 && i + j != apkLen
						&& !c.getrMatch().contains(surplusName)) {// 前缀
					c.setrMatch(surplusName);
				} else if (j != 0 && i + j == apkLen
						&& !c.getlMatch().contains(surplusName)) {// 后缀
					c.setlMatch(surplusName);
				}
				BUFFER[subLen].put(subName, c);
				FREQUEN.put(subLen, BUFFER[subLen]);
//				FREQUEN.get(subLen).put(subName, c);
			}// for
		}// for
	}// splitStatistic

	/**
	 * [filterChain]检查找到的高频匹配集的子串是否已经以更长的形式出现过 如“桌面主题壁纸”会是一个高频匹配集的子串 “面主题壁纸”同样也是
	 * 在filterChain中是从高位向低位扫描 这里“面主题壁纸”则不会被保存
	 * 
	 * @return true代表sub是个需要保存的噪声词
	 */
	public static boolean checkHighSubExist(String sub) {
		Cell subCell = getCell(FREQUEN, sub);
		HashSet<String> subLMatch = subCell.getlMatch();
		HashSet<String> subRMatch = subCell.getrMatch();
		// 先判断sub的左右匹配集的复杂度是否满足要求
		if (subLMatch.size() + subRMatch.size() < MIN_NOISE_SUB_MATCH)
			return false;
		for (int keyNum = MAX_WORD_LEN; keyNum > sub.length(); keyNum--) {
			HashMap<String, Cell> itMap = HIGH_MATCH_SUB.get(keyNum);
			if (null == itMap)
				continue;
			Iterator<String> iterator = itMap.keySet().iterator();
			while (iterator.hasNext()) {// 遍历所有母集
				String str = iterator.next();
				if (str.contains(sub)) {
					Cell strCell = getCell(HIGH_MATCH_SUB, str);
					HashSet<String> strLMatch = strCell.getlMatch();
					HashSet<String> strRMatch = strCell.getrMatch();
					// 如果sub跟已经存在HIGH_MATCH_SUB中的str的左右匹配集一样多 sub不算噪声词
					if (subLMatch.size() == strLMatch.size()
							&& subRMatch.size() == strRMatch.size()) {
						return false;
					} else {// 将sub左右匹配集中与str重合的去除
						for (String s : strLMatch) {// 将左匹配集整合
							String subMatchName = s + str.replace(sub, ""); //s+
							if (subLMatch.contains(subMatchName)){
								Cell subMatchCell = getCellByAll(subMatchName);
								if(null != subMatchCell)
									subCell.setCount(subCell.getCount() - subMatchCell.getCount());
								subLMatch.remove(subMatchName);
							}
						}
						for (String s : strRMatch) {// 将右匹配集整合
							String subMatchName = str.replace(sub, "") + s;	//+s
							if (subRMatch.contains(subMatchName)){
								Cell subMatchCell = getCellByAll(subMatchName);
								if(null != subMatchCell)
									subCell.setCount(subCell.getCount() - subMatchCell.getCount());
								subRMatch.remove(subMatchName);
							}
						}
					}// if
				}// if
			}// while
		}// for
		// 再判断经过处理后的sub左右串是否满足要求
		if (subLMatch.size() + subRMatch.size() < MIN_NOISE_SUB_MATCH)
			return false;
		return true;
	}// checkHighSubExist

	/**
	 * [remove]子集频率的判断 当n_str/n_sub > 0.8时认为sub不是一个app名字
	 * 
	 * @param str
	 *            母串
	 * @param sub
	 *            子串
	 * @return sub需要删除返回true 否则返回false
	 */
	public static boolean checkSubsetNeedDel(String str, String sub) {
		int strFrequen = getCount(FREQUEN, str); // 母串频率
		int subFrequen = getCount(FREQUEN, sub); // 子串频率
		if (strFrequen >= 1000 && strFrequen / subFrequen > 0.8
				|| strFrequen < 1000 && strFrequen >= 100
				&& strFrequen / subFrequen > 0.7 || strFrequen < 100
				&& strFrequen >= 1 && strFrequen / subFrequen > 0.5) {
			return true;
		}
		return false;
	}// checkSubsetNeedDel

	/**
	 * [remove]聚集度的判断 当P(母串)/P(子串)*P(子串的互补串)越小 越能说明母串是随机拼接成的
	 * 会将最小值保存在母串的minAggregate中 待遍历完所有子串后判断母串是否删除 母串是被检查过存在的
	 * 子串若为null则到DEL_FREQUEN中找到进行校验
	 * 
	 * @param str
	 *            母串
	 * @param sub
	 *            子串
	 */
	public static void checkStrMinAggregate(String str, String sub) {
		int[] mutualFrequens = new int[str.length()];
		// 子串的互补串
		int index = str.indexOf(sub);
		String[] mutualNames = { str.substring(0, index),
				str.substring(index + sub.length(), str.length()) };
		Cell cellStr = getCell(FREQUEN, str);
		Cell cellSub = getCellByAll(sub);
		if (null == cellSub)
			return;

		float pStr = (float) cellStr.getCount() / COUNT_ALL; // 母串频率
		float pSub = (float) cellSub.getCount() / COUNT_ALL; // 子串频率
		for (int i = 0; null != mutualNames && i < mutualNames.length; i++) {
			if (mutualNames[i].equals(""))
				continue;
			String m = mutualNames[i];
			// 对互补串进行判空处理 如果空 则到DEL_FREQUEN检索
			if (isNotEmpty(FREQUEN, m)) {
				mutualFrequens[i] = getCount(FREQUEN, m);
			} else if (isNotEmpty(DEL_FREQUEN, m)) {
				mutualFrequens[i] = getCount(DEL_FREQUEN, m);
			} else if (isNotEmpty(DEL_AGGREGATE, m)) {
				mutualFrequens[i] = getCount(DEL_AGGREGATE, m);
			} else if (isNotEmpty(DEL_NOTWHOLE, m)) {
				mutualFrequens[i] = getCount(DEL_NOTWHOLE, m);
			} else if (isNotEmpty(HIGH_MATCH_SUB, m)) {
				mutualFrequens[i] = getCount(HIGH_MATCH_SUB, m);
			} else
				continue;

			float pMutual = (float) mutualFrequens[i] / COUNT_ALL; // 互补串频率
			float aggregate = pStr / (pSub * pMutual);
			// 如果聚集度小于母串原有的值 则替换
			if (cellStr.getMinAggregate() == 0
					|| cellStr.getMinAggregate() > aggregate) {
				cellStr.setMinAggregate(aggregate);
			}
		}
	}// checkStrMinAggregate

	/**
	 * [remove]在做任何删除操作前 需要校验其以整串出现的概率
	 */
	public static boolean checkByWholeRate(String str) {
		int countSub = getCountSub(FREQUEN, str);
		int countWhole = getCountWhole(FREQUEN, str);
		if (countWhole > countSub || countWhole > MIN_WHOLE_SUPPORT)
			return false;
		return true;
	}// checkByWholeRate

	/**
	 * [removeSubset]递归扫描其子集 
	 * 从FREQUEN中删除子集中不是词的词 再此母串、子串进行双向校验 
	 * 由母串验证子串频率 判断是否需要删除 如果子串有独立成串出现过 母串不可进行删除 
	 * 由子串验证最小聚集度 判断母串的去留
	 */
	@SuppressWarnings("unchecked")
	public static void remove(String str) {//当前下 算法聚集度的校验对词库意义不大 为节省内存 不进行此环节 TODO
		// 当进行扫描的字串为null || 长度不大于2时 则认为其不可再分 返回
		if (null == str || str.length() <= MIN_WORD_NUM + 1)
			return;
		int strLen = str.length();
		// i为想要截取的长度 长度为MIN_WORD_NUM时不在考虑
		for (int i = strLen - 1; i > MIN_WORD_NUM; i--) {
			for (int j = 0; j <= strLen - i; j++) {// j为从str的第j位开始截取
				String sub = str.substring(j, j + i);// 截取长度为i的sub
				/**
				 * 由子串校验母串 当循环完会给母串找到一个最小聚集度
				 */
//				checkStrMinAggregate(str, sub);
				// 如果子串已经在之前被扫描删除 则跳过
				if (!isNotEmpty(FREQUEN, sub))
					continue;
				/**
				 * 由母串校验子串 判断子串是否需要删除 这里用DEL_LIST来暂存需要删除的sub\str
				 * 为了避免iterator底层的校验机制
				 */
				if (checkSubsetNeedDel(str, sub)) {
					int len = sub.length();
					if (null == DEL_FREQUEN_BUFFER[len])
						DEL_FREQUEN_BUFFER[len] = new HashMap<String, Cell>();
					DEL_FREQUEN_BUFFER[len].put(sub, FREQUEN.get(len).get(sub));
					DEL_FREQUEN.put(len, DEL_FREQUEN_BUFFER[len]);
					DEL_SET.add(sub);
				} else {
					remove(sub);
				}
			}// for
		}// for
		/**
		 * 在母串结束时，对其进行校验 聚集度判断暂时不用 因为聚集度是对子串的处理有一定好处 对母串处理效果不好
		 */
		// if (FREQUEN.get(strLen).get(str).getMinAggregate() <
		// MIN_AGGREGATE_NUM
		// && checkByWholeRate(str)) {
		// if (null == DEL_AGGREGATE_BUFFER[strLen])
		// DEL_AGGREGATE_BUFFER[strLen] = new HashMap<String, Cell>();
		// DEL_AGGREGATE_BUFFER[strLen].put(str, FREQUEN.get(strLen).get(str));
		// DEL_AGGREGATE.put(strLen, DEL_AGGREGATE_BUFFER[strLen]);
		// DEL_LIST.add(str);
		// System.out.println("str: " + str + "  aggregate: " +
		// FREQUEN.get(strLen).get(str).getMinAggregate());
		// }
	}// remove

	/**
	 * [memoryTrim]从内存中去除确定词 其的子集由高位向低位扫描、判断 在递归扫描判断子串的同时 子串也在校验母串
	 */
	public static void removeSubset() {
		for (int keyNum = MAX_WORD_LEN; keyNum > 0; keyNum--) {
			HashMap<String, Cell> itMap = FREQUEN.get(keyNum);
			if (null == itMap)
				continue;
			Iterator<String> iterator = itMap.keySet().iterator();
			DEL_SET = new HashSet<String>();
			while (iterator.hasNext()) {
				String keyStr = iterator.next();
				remove(keyStr);
			}
			for (String s : DEL_SET) {
				FREQUEN.get(s.length()).remove(s);
			}
		}
	}// removeSubsets

	/**
	 * [memoryTrim]过滤链 可提取三个子函数 1、单个字节 将其存储到single_word_dictionary.txt
	 * 2、限定长度内未出现中文词 3、小于最小限定频率
	 */
	@SuppressWarnings("unchecked")
	public static void filterChain() {
		/**
		 * 去除最小支持数的词 一般是单个字节
		 */
		FREQUEN.remove(MIN_WORD_NUM);
		for (int keyNum = MAX_WORD_LEN; keyNum > 0; keyNum--) {
			HashMap<String, Cell> itMap = FREQUEN.get(keyNum);
			if (null == itMap)
				continue;
			Iterator<String> iterator = itMap.keySet().iterator();
			while (iterator.hasNext()) {
				boolean flag = false; // 默认认为子串不带有中文的
				String apkName = iterator.next();
				int apkLen = apkName.length();
				/**
				 * 中文字判断
				 */
				char[] apkChars = apkName.toCharArray();
				for (int iName = 0; iName < apkLen; iName++) {
					char apkChar = apkChars[iName];
					if (StringUtils.isChinese(apkChar)) {
						flag = true;
						break;
					}
				}
				/**
				 * 1、小于最小限定频率 删除 
				 * 2、如果子串在限定长度内没有中文 则删除 由于Iterator底层验证机制的问题
				 * 这里需要先对iterator进行删除 Iterator在遍历是会生成一个索引表 
				 * C\D的操作会改变物理信息 但索引表不变 即会抛错
				 */
				if ((!flag || itMap.get(apkName).getCount() <= MIN_SUPPORT_FREQUEN)) {
					if (null == DEL_FREQUEN_BUFFER[apkLen])
						DEL_FREQUEN_BUFFER[apkLen] = new HashMap<String, Cell>();
					DEL_FREQUEN_BUFFER[apkLen].put(apkName, FREQUEN.get(apkLen)
							.get(apkName));
					DEL_FREQUEN.put(apkLen, DEL_FREQUEN_BUFFER[apkLen]);
					iterator.remove();
					itMap.remove(apkName);
					continue;
				}// if
				/**
				 * 对串出现的形式进行判断  如果apkName没有以整串出现过 则将其删除 
				 * 如果出现形式（子串 > 整串 * MAX_WHOLE_MULTIPLE）
				 * 则认为apkName子串形式过于频繁 删除 另 需要在对删除子串中查找噪声词
				 */
				int countSub = itMap.get(apkName).getCountSub();
				int countWhole = itMap.get(apkName).getCountWhole();
				/**
				 * 当待删词其左右匹配项丰富 则将词转存到HIGH_MATCH_SUB 否则存入DEL_NOTWHOLE
				 */
				if (countWhole == 0 
						|| ((MAX_WHOLE_MULTIPLE * countWhole) < countSub) 
						&& countWhole < MIN_WHOLE_SUPPORT) {
					if (checkHighSubExist(apkName)) {
						HIGH_MATCH_SUB_BUFFER[apkLen] = getHash(HIGH_MATCH_SUB,
								apkName);
						HIGH_MATCH_SUB_BUFFER[apkLen].put(apkName,
								getCell(FREQUEN, apkName));
						HIGH_MATCH_SUB.put(apkLen,
								HIGH_MATCH_SUB_BUFFER[apkLen]);
					}
//					else {
//						DEL_NOTWHOLE_BUFFER[apkLen] = getHash(DEL_NOTWHOLE,
//								apkName);
//						DEL_NOTWHOLE_BUFFER[apkLen].put(apkName,
//								getCell(FREQUEN, apkName));
//						DEL_NOTWHOLE.put(apkLen, DEL_NOTWHOLE_BUFFER[apkLen]);
//					}
					iterator.remove();
					itMap.remove(apkName);
					continue;
				}// if
			}// while
			FREQUEN.put(keyNum, itMap);
		}// for
	}// filterChain

	/**
	 * 检测给定区位的子串是否可以切母串 由start切到end 且只对前、后缀考虑 1、记录str是由谁切割的 2、Sub切去的匹配集
	 * 3、Surplus被切去的匹配集
	 */
	public static void checkSubCutWhole(String str, int start, int end) {
		String subName = str.substring(start, end);
		if (isNotEmpty(FREQUEN, subName)) {
			String surplusName = str.replace(subName, "");
			DEL_SET.add(str);// 将母串标记为待删除
			Cell strCell = getCell(FREQUEN, str);
			Cell subCell = getCell(FREQUEN, subName);
			/**
			 * 记录str是由谁切割的
			 */
			strCell.setCutBy(subName);
			/**
			 * 分别记录subCut、surplusCut左右匹配集
			 */
			if (start == 0) {
				subCell.setrMatch(surplusName);
			} else {
				subCell.setlMatch(surplusName);
			}
			getHash(FREQUEN, str).put(str, strCell);
			getHash(FREQUEN, subName).put(subName, subCell);
		}
	}// checkSubCutWhole

	/**
	 * [amendPrecision]删除所记录的待删除队列
	 * 此方法处理的事情比较多 
	 * 需要找到具有最大左右匹配相的切割串
	 * 需要恰到好处的对母串进行删除
	 * 需要判断剩余串其复杂程度 如果过低 则不再DEL_CUTWHOLE中保存 
	 * @param list 待删除队列
	 */
	@SuppressWarnings({ "unchecked" })
	public static void delSubCutSet(HashSet<String> set) {
		//记录str最后被切割成什么剩余的映射
		HashMap<String, String> surplus2StrMapping = new HashMap<String, String>();
		for (String str : set) {
			// 获取能切割串s的子串集合
			Cell strCell = getCell(FREQUEN, str);
			HashSet<String> cutStrs = strCell.getCutBy();
			/**
			 * 找到其中匹配项最多的切割串
			 */
			int maxMatchNum = 0;
			int leftOrRigth = 0; // 0代表左 1代表右
			String maxMatchCut = "";	//具有最大匹配集的子串
			for (String subCut : cutStrs) {
				Cell matchCell = getCell(FREQUEN, subCut);
				int lMatchNum = matchCell.getlMatch().size();
				int rMatchNum = matchCell.getrMatch().size();
				if (maxMatchNum < lMatchNum) {
					maxMatchNum = lMatchNum;
					maxMatchCut = subCut;
					leftOrRigth = 0;
				}
				if (maxMatchNum < rMatchNum) {
					maxMatchNum = rMatchNum;
					maxMatchCut = subCut;
					leftOrRigth = 1;
				}
			}
			//剪枝
			if(maxMatchNum < MIN_LORR_MATCH)
				continue;
			/**
			 * 将最高匹配集的子串切割出的噪声词保存到DEL_CUTWHOLE
			 */
			Cell surplusCell = null;
			String maxMatchSurplus = str.replace(maxMatchCut, "");	//具有最大匹配集的剩余串
			int maxSurplusLen = maxMatchSurplus.length();
			if (isNotEmpty(DEL_CUTWHOLE, maxMatchSurplus)) {
				surplusCell = getCell(DEL_CUTWHOLE, maxMatchSurplus);
				int count = surplusCell.getCount();
				// countSub记录在各个词串中切分剩余的出现总次数
				int countSub = surplusCell.getCountSub() + getCountWhole(FREQUEN, str);
				surplusCell.setCount(++count);
				surplusCell.setCountSub(countSub);
				if (leftOrRigth == 0) {
					surplusCell.setlMatch(maxMatchCut);
				} else {
					surplusCell.setrMatch(maxMatchCut);
				}
			} else {
				DEL_CUTWHOLE_BUFFER[maxSurplusLen] = getHash(DEL_CUTWHOLE, maxMatchSurplus);
				surplusCell = new Cell(1);
				surplusCell.setCountSub(1);
				if (leftOrRigth == 0) {
					surplusCell.setlMatch(maxMatchCut);
				} else {
					surplusCell.setrMatch(maxMatchCut);
				}
			}
			DEL_CUTWHOLE_BUFFER[maxSurplusLen].put(maxMatchSurplus, surplusCell);
			DEL_CUTWHOLE.put(maxSurplusLen, DEL_CUTWHOLE_BUFFER[maxSurplusLen]);
			/**
			 * 母串中切割的子串有很高的匹配集 这里暂时不对str进行删除
			 * 删除动作 先保存到surplus2StrMapping映射中 后在处理
			 */
			surplus2StrMapping.put(str, maxMatchSurplus);
		}//for
		/**
		 * 在确定要删除的str中 查看其剩余串的匹配复杂程度
		 * 过低  则从DEL_CUTWHOLE中去除
		 * 这里没有对从DEL_CUTWHOLE删除的集 建库保存 TODO
		 */
		Iterator<String> ito = surplus2StrMapping.keySet().iterator();
		while(ito.hasNext()){
			String kStr = ito.next();
			String vSurplus = surplus2StrMapping.get(kStr);
			Cell vSurplusCell = getCell(DEL_CUTWHOLE, vSurplus);
			int matchSize = vSurplusCell.getlMatch().size() + vSurplusCell.getrMatch().size();
			//可能会有一个vSurplus被多个str删  则第一次以后matchSize = 0
			if(matchSize != 0){
				if (matchSize < MIN_LORR_MATCH) {
					DEL_CUTWHOLE.get(vSurplus.length()).remove(vSurplus);
				} else {
					FREQUEN.get(kStr.length()).remove(kStr);
				}
			}
		}//if
	}// delSubCutList

	/**
	 * [memoryTrim]用确定是词的字串来
	 *  前向、后向匹配扫描 且当遇到待切子串是一个词的时候 就不再往下面去进行 
	 *  最外层由词长高向底扫描 此时的词在后面必定会再次被扫描
	 * @param flag为接收扫描的类型 0为前置 1为后置
	 */
	public static void amendPrecision(int flag) {
//		for (int keyNum = FREQUEN.size() + MIN_WORD_NUM; keyNum > 0; keyNum--) {
		for (int keyNum = MAX_WORD_LEN; keyNum > 0; keyNum--) {
			HashMap<String, Cell> itMap = FREQUEN.get(keyNum);
			if (null == itMap)
				continue;
			Iterator<String> iterator = itMap.keySet().iterator();
			while (iterator.hasNext()) {
				String keyStr = iterator.next();
				int keyLen = keyStr.length();
				/**
				 * 先进行后缀扫描 如“愤怒的小鸟无限版” 这里会拿愤怒的小鸟去切割，成“愤怒的小鸟”+“无限版”
				 */
				if (flag == 0) {
					for (int i = keyLen - 2; i >= 2; i--) {
						checkSubCutWhole(keyStr, 0, i);
					}
				}
				/**
				 * 再进行前缀扫描 如“古墨主题壁纸” 这里会拿主题壁纸去切割，成“古墨”+“主题壁纸”
				 */
				else {
					for (int i = 2; i <= keyLen; i++) {
						checkSubCutWhole(keyStr, i, keyLen);
					}
				}
			}// while
		}// for
	}// amendPrecision
	
	/**
	 * [memoryTrim]持久化内存 向指定路径写入对应hash内的值
	 */
	public static void persistentMemory(){
		File file = new File(STORAGE_ROUTE);
		if(!file.exists() && !file.isDirectory()){
			file.mkdirs();
		}
		TRIM_TIMES++;
		String pathFre = STORAGE_ROUTE + "[1]definite_app_dictionary"
				+ TRIM_TIMES + ".txt";
//		String pathFrequenDel = STORAGE_ROUTE + "[2]noise_word_frequen.txt";
//		String pathNotWholeDel = STORAGE_ROUTE + "[3]noise_word_notwhole.txt";
		String pathCutWholeDel = STORAGE_ROUTE + "[4]noise_cut_whole"
				+ TRIM_TIMES + ".txt";
		String pathHighMatchSub = STORAGE_ROUTE + "[5]high_match_sub"
				+ TRIM_TIMES + ".txt";

		persistent(FREQUEN, pathFre);
//		persistent(DEL_FREQUEN, pathFrequenDel); //文件过大 300W数据10分钟->13G数据量 太占IO
//		persistent(DEL_NOTWHOLE, pathNotWholeDel);
		persistent(DEL_CUTWHOLE, pathCutWholeDel);
		persistent(HIGH_MATCH_SUB, pathHighMatchSub);
	}//persistentMemory

	/**
	 * [extractFrequencyWords]内存整理函数 
	 * 1、进行一系列过滤 
	 * 2、去除那些确定不是单词的子集 
	 * 3、由子串切割母串 提高精确度
	 * (最后代码重构，速度提升，将下列步骤集并非处理)
	 */
	public static void memoryTrim() { //数据处理核心  TODO
		System.out.println("过滤链开始。。。");
		final long s0 = System.nanoTime();
		filterChain(); // 过滤链
		final long e0 = System.nanoTime();
		System.out.println("过滤链结束！用时： " + (e0 - s0)/1.0e9 + "s");
//		removeSubset(); // 子集驱逐
		/**
		 * 由上两步可以得到初步的词典 下面需要对词典进行优化 提高精确度 0代表前缀扫描 1代表后缀扫描
		 */
		DEL_SET = new HashSet<String>();
		System.out.println("前缀扫描开始。。。");
		final long s1 = System.nanoTime();
		amendPrecision(0);
		final long e1 = System.nanoTime();
		System.out.println("前缀扫描结束！用时： " + (e1 - s1)/1.0e9 + "s\n后缀扫描开始。。。");
		final long s2 = System.nanoTime();	
		amendPrecision(1);
		final long e2 = System.nanoTime();
		System.out.println("后缀扫描结束！用时： " + (e2 - s2)/1.0e9 + "s\n处理待删除串开始。。。");
		final long s3 = System.nanoTime();
		delSubCutSet(DEL_SET);
		final long e3 = System.nanoTime();
		System.out.println("处理待删除串结束！用时： " + (e3 - s3)/1.0e9 + "s\n内存持久化开始。。。");
		final long s4 = System.nanoTime();
		persistentMemory();
		final long e4 = System.nanoTime();
		System.out.println("内存持久化结束！用时： " + (e4 - s4)/1.0e9 + "s");
	}// memoryTrim

	public static void main(String[] args) {
		System.out.println("ParticipleStatistics Begin！");
		final long start = System.nanoTime();
		PATH = args[0];
		STORAGE_ROUTE = args[1]; 
		extractFrequencyWords();
		final long end = System.nanoTime();
		System.out.println("总用时： " + (end - start)/1.0e9 + "s");
	}

	/**
	 * 判断HashMap内是否有元素
	 */
	protected static boolean isNotEmpty(
			HashMap<Integer, HashMap<String, Cell>> hash, String s) {
		return hash.containsKey(s.length())
				&& hash.get(s.length()).containsKey(s) ? true : false;
	}

	/**
	 * [persistentMemory]持久化到硬盘path下
	 */
	protected static void persistent(
			HashMap<Integer, HashMap<String, Cell>> hash, String path) {
		int count = 0;
		StringBuffer sbf = new StringBuffer("");
		for (Integer i : hash.keySet()) {
			HashMap<String, Cell> h = hash.get(i);
			for (String s : h.keySet()) {
				StringBuffer cutBy = new StringBuffer("  cutBy-->");
				StringBuffer lMatch = new StringBuffer("||lMatch-->");
				StringBuffer rMatch = new StringBuffer("||rMatch-->");
				for (String sCutBy : h.get(s).getCutBy()) {
					cutBy.append(sCutBy + " ");
				}
				for (String sLMatch : h.get(s).getlMatch()) {
					lMatch.append(sLMatch + " ");
				}
				for (String sRMatch : h.get(s).getrMatch()) {
					rMatch.append(sRMatch + " ");
				}
				sbf.append(
						s + ": " + h.get(s).getCount() + "  sub-->"
								+ h.get(s).getCountSub() + "  whole-->"
								+ h.get(s).getCountWhole() + "  aggregate-->"
								+ h.get(s).getMinAggregate()).append(cutBy)
						.append(lMatch).append(rMatch).append("\r\n");
				if (++count % 10000 == 0)
					WriteFile.write(sbf.toString(), path);
			}
			if (count % 10000 != 0)
				WriteFile.write(sbf.toString(), path);
		}
	}
	
	/**
	 * 得到对应的HashMap
	 */
	protected static HashMap<String, Cell> getHash(
			HashMap<Integer, HashMap<String, Cell>> hash, String str) {
		return hash.containsKey(str.length()) ? hash.get(str.length())
				: new HashMap<String, Cell>();
	}

	/**
	 * 获取对应的Cell
	 */
	protected static Cell getCell(HashMap<Integer, HashMap<String, Cell>> hash,
			String str) {
		Cell cell = getHash(hash, str).get(str);
		return null != cell ? cell : new Cell();
	}

	/**
	 * 从所有的库中获取对应的Cell
	 */
	protected static Cell getCellByAll(String str) {
		if (isNotEmpty(FREQUEN, str)) {
			return getCell(FREQUEN, str);
		} else if (isNotEmpty(DEL_FREQUEN, str)) {
			return getCell(DEL_FREQUEN, str);
		} else if (isNotEmpty(DEL_AGGREGATE, str)) {
			return getCell(DEL_AGGREGATE, str);
		} else if (isNotEmpty(DEL_NOTWHOLE, str)) {
			return getCell(DEL_NOTWHOLE, str);
		} else if (isNotEmpty(HIGH_MATCH_SUB, str)) {
			return getCell(HIGH_MATCH_SUB, str);
		} else
			return null;
	}

	/**
	 * 提取的获取hash Count函数
	 */
	public static int getCount(HashMap<Integer, HashMap<String, Cell>> hash,
			String str) {
		return getCell(hash, str).getCount();
	}

	/**
	 * 提取的获取hash CountSub函数
	 */
	public static int getCountSub(HashMap<Integer, HashMap<String, Cell>> hash,
			String str) {
		return getCell(hash, str).getCountSub();
	}

	/**
	 * 提取的获取hash CountWhole函数
	 */
	public static int getCountWhole(
			HashMap<Integer, HashMap<String, Cell>> hash, String str) {
		return getCell(hash, str).getCountWhole();
	}
}