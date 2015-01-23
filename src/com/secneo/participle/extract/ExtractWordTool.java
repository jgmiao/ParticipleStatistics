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
import com.secneo.participle.util.MyUtils;
import com.secneo.participle.util.StringUtils;

public class ExtractWordTool {
	private static final int MIN_WORD_LEN = 1; // 最小支持的字串长度
	private static final int MAX_WORD_LEN = 40; // 最大支持的字串长度
	private static final int MIN_SUPPORT_ALL_WORD = 1; // 最小支持词出现的频率
	private static final int MIN_LORR_MATCH = 5; // 噪声词切割出的剩余串匹配相列表最小的支持数
	private static final int MIN_MODIFY_SUB_MATCH = 15;// 最小修饰词匹配项支持度 大于 则认为该子串为修饰词
	private static final int MIN_HIGH_MATCH_SUB_LEN = 5;
	// 存储ALL_WORD的各层数据（进过一系列的处理后，最后成为我们需要的全词库）
	private static HashMap<Integer, HashMap<String, Cell>> ALL_WORD = new HashMap<Integer, HashMap<String, Cell>>();
	// 存储不是整串的词
	private static HashMap<Integer, HashMap<String, Cell>> DEL_NOTWHOLE = new HashMap<Integer, HashMap<String, Cell>>();
	// 存储被确定词字串切割剩余串
	private static HashMap<Integer, HashMap<String, Cell>> MODIFY_CUTWHOLE = new HashMap<Integer, HashMap<String, Cell>>();
	// 存储由切割串判定从全库中删除的词
	private static HashMap<Integer, HashMap<String, Cell>> DEL_CUTWHOLE = new HashMap<Integer, HashMap<String, Cell>>();
	// 存储具有高频左右匹配项的词
	private static HashMap<Integer, HashMap<String, Cell>> HIGH_MATCH_SUB = new HashMap<Integer, HashMap<String, Cell>>();
	
	
	private HashSet<String> delSubCutSet = null; //暂存待删除母串的集合
	private long countAll = 0; // 统计所有划分词的总数
	private String soucePath;	
	private String storageRoute;
	
	public ExtractWordTool() {}
	
	/**
	 * @param soucePath 所有apk_name存放路径
	 * @param storageRoute	处理得到的各个字典的存放路径
	 */
	public ExtractWordTool(final String soucePath, final String storageRoute) {
		this.soucePath = soucePath;
		this.storageRoute = storageRoute;
	}
	
	/**
	 * 抽词工具唯一对外接口
	 */
	public void start() {
		MyUtils.log.info("ParticipleStatistics Begin。。。\n");
		final long start = System.nanoTime();
		extractWordPort();
		final long end = System.nanoTime();
		MyUtils.log.info("ParticipleStatistics End！\n");
		MyUtils.log.info("总用时： " + (end - start) / 1.0e9 + "s\n");
	}
	
	/**
	 * 抽词算法接口
	 */
	private void extractWordPort() {
		String s = null;
		try {
			File f = new File(soucePath);
			BufferedReader br;
			br = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
			MyUtils.log.info("切词开始。。。\n");
			final long s0 = System.nanoTime();
			while ((s = br.readLine()) != null) {//可以并发执行 需要修改ALL_WORD的安全性
				if (s.length() > MAX_WORD_LEN || s.length() <= MIN_WORD_LEN)
					continue;
				splitStatistic(StringUtils.stringFilter(s));
			}
			final long e0 = System.nanoTime();
			MyUtils.log.info("切词完毕  HashMap size：" + countAll + "  用时： "
					+ (e0 - s0) / 1.0e9 + "s\n");
			br.close();
		} catch (IOException e) {
			MyUtils.log.error("文件读取出错。。。\n");
			e.printStackTrace();
		}
		MyUtils.log.info("开始内存整理。。。\n");
		memoryTrim(); // 内存整理
		MyUtils.log.info("内存整理结束！\n内存持久化开始。。。\n");
		final long s4 = System.nanoTime();
		persistentMemory(storageRoute);
		final long e4 = System.nanoTime();
		MyUtils.log.info("内存持久化结束！用时： " + (e4 - s4) / 1.0e9 + "s\n");
	}// extractWordPort

	/**
	 * [extractWordPort]对App名进行拆分统计 
	 * 这里数据结构采用的是HashMap<Integer, HashMap<String, Cell>> 
	 * 最后在我们的HashMap中会存放两级数据，第一级是应用名长度，第二级为描述的详细状况信息
	 * @return apkName被切割的次数 count
	 */
	private void splitStatistic(final String apkName) {
		int apkLen = apkName.length();
		int count;
		int countSub;
		int countWhole;
		Cell c = null;
		String subName = null;
		String surplusName = null;
		for (int i = MIN_WORD_LEN + 1; i <= apkLen; i++) {// i为需要切分的长度
			for (int j = 0; j <= apkLen - i; j++) {
				countAll++;
				subName = apkName.substring(j, j + i); // 切割串
				// 如果字串已经存在，则对原有统计进行增1操作
				if (MyUtils.isNotEmpty(ALL_WORD, subName) && getCell(ALL_WORD, subName).getCount() > 0) {
					c = getCell(ALL_WORD, subName);
					count = c.getCount();
					/**
					 * 对count操作
					 */
					c.setCount(++count);
					/**
					 * 对countWhole、countSub操作
					 */
					if (i == apkLen) {
						countWhole = c.getCountWhole();
						c.setCountWhole(++countWhole);
					} else {
						countSub = c.getCountSub();
						c.setCountSub(++countSub);
					}
				} else {// 初始化BUFFER
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
				surplusName = apkName.replace(subName, "");
				if (j == 0 && i + j != apkLen
						&& !c.getrMatch().contains(surplusName)) {// 前缀
					c.setrMatch(surplusName);
				} else if (j != 0 && i + j == apkLen
						&& !c.getlMatch().contains(surplusName)) {// 后缀
					c.setlMatch(surplusName);
				}
				MyUtils.getHash(ALL_WORD, subName).put(subName, c); // 在getHash进行了null的处理
			}// for
		}// for
	}// splitStatistic

	/**
	 * [filterChain]检查找到的高频匹配集的子串是否已经以更长的形式出现过 
	 * 如“桌面主题壁纸”会是一个高频匹配集的子串 “面主题壁纸”同样也是
	 * 在filterChain中是从高位向低位扫描 这里“面主题壁纸”则不会被保存
	 * @return true代表sub是个需要保存的噪声词
	 */
	private boolean checkHighSubExist(final String sub) {
		Cell subCell = getCell(ALL_WORD, sub);
		HashSet<String> subLMatch = subCell.getlMatch();
		HashSet<String> subRMatch = subCell.getrMatch();
		// 先判断sub的左右匹配集的复杂度是否满足要求
		if (subLMatch.size() + subRMatch.size() < MIN_MODIFY_SUB_MATCH) { 
			return false;
		}
		if (sub.length() > MIN_HIGH_MATCH_SUB_LEN) { //TODO 参数要调整  并只考虑前缀
			return false;
		}
		for (int keyNum = MAX_WORD_LEN; keyNum > sub.length(); keyNum--) {
			if (!HIGH_MATCH_SUB.containsKey(keyNum))
				continue;
			HashMap<String, Cell> itMap = HIGH_MATCH_SUB.get(keyNum);
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
						for (Object s : strLMatch) {// 将左匹配集整合
							String subMatchName = s + str.replace(sub, ""); // s+
							if (subLMatch.contains(subMatchName)) {
								Cell subMatchCell = getCellByAll(subMatchName);
								if (null != subMatchCell) {
									subCell.setCount(subCell.getCount() - subMatchCell.getCount());
								}
								subLMatch.remove(subMatchName);
							}
						}
						for (Object s : strRMatch) {// 将右匹配集整合
							String subMatchName = str.replace(sub, "") + s; // +s
							if (subRMatch.contains(subMatchName)) {
								Cell subMatchCell = getCellByAll(subMatchName);
								if (null != subMatchCell) {
									subCell.setCount(subCell.getCount() - subMatchCell.getCount());
								}
								subRMatch.remove(subMatchName);
							}
						}
					}// if
				}// if
			}// while
		}// for
			// 再判断经过处理后的sub左右串是否满足要求
		if (subLMatch.size() + subRMatch.size() < MIN_MODIFY_SUB_MATCH)
			return false;
		return true;
	}// checkHighSubExist

	/**
	 * [memoryTrim]过滤链 可提取三个子函数 
	 * 1、单个字节 将其存储到single_word_dictionary.txt
	 * 2、限定长度内未出现中文词 
	 * 3、小于最小限定频率
	 */
	private void filterChain() {
		for (int keyNum = MAX_WORD_LEN; keyNum > 0; keyNum--) {
			if (!ALL_WORD.containsKey(keyNum))
				continue;
			HashMap<String, Cell> itMap = ALL_WORD.get(keyNum);
			Iterator<String> iterator = itMap.keySet().iterator();
			while (iterator.hasNext()) {
				boolean flag = false; // 默认认为子串不带有中文的
				String apkName = iterator.next();
				/**
				 * 中文字判断
				 */
				char[] apkChars = apkName.toCharArray();
				for (int iName = 0; iName < apkName.length(); iName++) {
					char apkChar = apkChars[iName];
					if (StringUtils.isChinese(apkChar)) {
						flag = true;
						break;
					}
				}
				/**
				 * 1、小于最小限定频率 删除     2、如果子串在限定长度内没有中文 则删除 
				 * 由于Iterator底层验证机制的问题 这里需要先对iterator进行删除 
				 * Iterator在遍历是会生成一个索引表 C\D的操作会改变物理信息 但索引表不变 即会抛错
				 */
				if ((!flag || itMap.get(apkName).getCount() <= MIN_SUPPORT_ALL_WORD)) {
					iterator.remove();
					itMap.remove(apkName);
					continue;
				}// if
				/**
				 * 对串出现的形式进行判断 如果apkName没有以整串出现过 则将其删除 当待删词其左右匹配项丰富
				 * 则将词转存到HIGH_MATCH_SUB 否则存入DEL_NOTWHOLE
				 */
				if (itMap.get(apkName).getCountWhole() == 0) {
					if (checkHighSubExist(apkName)) {// TODO  需要做聚集度的判断  即可解决一系列此错的生成
						MyUtils.getHash(HIGH_MATCH_SUB, apkName).put(apkName, getCell(ALL_WORD, apkName));
					} else {
						MyUtils.getHash(DEL_NOTWHOLE, apkName).put(apkName, getCell(ALL_WORD, apkName));
					}
					iterator.remove();
					itMap.remove(apkName);
					continue;
				}// if
			}// while
			ALL_WORD.put(keyNum, itMap);
		}// for
	}// filterChain

	/**
	 * [amendPrecision]检测给定区位的子串是否可以切母串 由start切到end 且只对前、后缀考虑 
	 * 1、记录str是由谁切割的 
	 * 2、Sub切去的匹配集
	 * 3、Surplus被切去的匹配集
	 */
	private void checkSubCutWhole(final String str, final int start, final int end) {
		String sub = str.substring(start, end);
		if (MyUtils.isNotEmpty(ALL_WORD, sub)) {
			String surplusName = str.replace(sub, "");
			delSubCutSet.add(str);// 将母串标记为待删除
			Cell strCell = getCell(ALL_WORD, str);
			Cell subCell = getCell(ALL_WORD, sub);
			/**
			 * 记录str是由谁切割的
			 */
			strCell.setCutBy(sub);
			/**
			 * 分别记录subCut、surplusCut左右匹配集
			 */
			if (start == 0) {
				subCell.setrMatch(surplusName);
			} else {
				subCell.setlMatch(surplusName);
			}
			MyUtils.getHash(ALL_WORD, str).put(str, strCell);
			MyUtils.getHash(ALL_WORD, sub).put(sub, subCell);
		}
	}// checkSubCutWhole

	/**
	 * [amendPrecision]删除所记录的待删除队列 此方法处理的事情比较多 
	 * 需要找到具有最大左右匹配相的切割串 需要恰到好处的对母串进行删除
	 * 需要判断剩余串其复杂程度 如果过低 则不再DEL_CUTWHOLE中保存
	 * @param list 待删除队列
	 */
	private void delSubCutSet(final HashSet<String> set) {
		// 记录str最后被切割成什么剩余的映射
		HashMap<String, String> surplus2StrMapping = new HashMap<String, String>();
		for (String str : set) {
			// 获取能切割串s的子串集合
			Cell strCell = getCell(ALL_WORD, str);
			HashSet<String> cutStrs = strCell.getCutBy();
			/**
			 * 找到其中匹配项最多的切割串
			 */
			int maxMatchNum = 0;
			int leftOrRigth = 0; // 0代表左 1代表右
			String maxMatchCut = ""; // 具有最大匹配集的子串
			for (String subCut : cutStrs) {
				Cell matchCell = getCell(ALL_WORD, subCut);
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
			// 剪枝
			if (maxMatchNum < MIN_LORR_MATCH)
				continue;
			/**
			 * 将最高匹配集的子串切割出的噪声词保存到MODIFY_CUTWHOLE
			 */
			Cell surplusCell = null;
			String maxMatchSurplus = str.replace(maxMatchCut, ""); // 具有最大匹配集的剩余串
			if (MyUtils.isNotEmpty(MODIFY_CUTWHOLE, maxMatchSurplus)) {// 在MODIFY_CUTWHOLE中命中，则进行累加
				surplusCell = getCell(MODIFY_CUTWHOLE, maxMatchSurplus);
				int count = surplusCell.getCount();
				// countSub记录在各个词串中切分剩余的出现总次数
				int countSub = surplusCell.getCountSub()
						+ getCell(ALL_WORD, str).getCountWhole();
				surplusCell.setCount(++count);
				surplusCell.setCountSub(countSub);
				if (leftOrRigth == 0) {
					surplusCell.setlMatch(maxMatchCut);
				} else {
					surplusCell.setrMatch(maxMatchCut);
				}
			} else {
				surplusCell = new Cell(1);
				surplusCell.setCountSub(1);
				if (leftOrRigth == 0) {
					surplusCell.setlMatch(maxMatchCut);
				} else {
					surplusCell.setrMatch(maxMatchCut);
				}
			}// 得到处理过后的surplusCell
			MyUtils.getHash(MODIFY_CUTWHOLE, maxMatchSurplus).put(maxMatchSurplus, surplusCell);
			/**
			 * 母串中切割的子串有很高的匹配集 这里暂时不对str进行删除 删除动作 先保存到surplus2StrMapping映射中 后在处理
			 */
			surplus2StrMapping.put(str, maxMatchSurplus);
		}// for
		/**
		 * 在确定要删除的str中 查看其剩余串的匹配复杂程度 过低 则从MODIFY_CUTWHOLE中去除
		 */
		Iterator<String> ito = surplus2StrMapping.keySet().iterator();
		while (ito.hasNext()) {
			String kStr = ito.next();
			String vSurplus = surplus2StrMapping.get(kStr);
			Cell vSurplusCell = getCell(MODIFY_CUTWHOLE, vSurplus);
			int matchSize = vSurplusCell.getlMatch().size()
					+ vSurplusCell.getrMatch().size();
			// 可能会有一个vSurplus被多个str删 则第一次以后matchSize = 0
			if (matchSize != 0) {
				if (matchSize < MIN_LORR_MATCH) { // 如果匹配相频繁度过低 则从MODIFY_CUTWHOLE
					MyUtils.getHash(MODIFY_CUTWHOLE, vSurplus).remove(vSurplus);
				} else { // 否则保存到DEL_CUTWHOLE中
					MyUtils.getHash(DEL_CUTWHOLE, kStr).put(kStr, getCell(ALL_WORD, kStr));
//					getHash(ALL_WORD, kStr).remove(kStr);	//全词库中“愤怒的小鸟、捕鱼达人会被删除”
				}
			}
		}// if
	}// delSubCutList

	/**
	 * [memoryTrim]用确定是词的字串来前向、后向匹配扫描 且当遇到待切子串是一个词的时候 就不再往下面去进行 最外层由词长高向底扫描
	 * 此时的词在后面必定会再次被扫描
	 * @param flag为接收扫描的类型  0为前置 1为后置
	 */
	private void amendPrecision(final int flag) {
		for (int keyNum = MAX_WORD_LEN; keyNum > 0; keyNum--) {
			HashMap<String, Cell> itMap = ALL_WORD.get(keyNum);
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
	 * [extractWordPort]持久化内存 向指定路径写入对应hash内的值
	 */
	private void persistentMemory(final String storageRoute) {
		File file = new File(storageRoute);
		if (!file.exists() && !file.isDirectory()) {
			file.mkdirs();
		}
		String pathAllWord = storageRoute + "[1]all_definite.txt";
//		 String pathNotWholeDel = storageRoute + "[2]del_word_notwhole.txt";
		String pathCutWholeModify = storageRoute + "[3]modify_cut_whole.txt";
//		 String pathCutWholeDel = storageRoute + "[4]del_cut_whole.txt";
		String pathHighMatchSubModify = storageRoute + "[5]modify_highmatch_sub.txt";

		MyUtils.persistent(ALL_WORD, pathAllWord);
//		 persistent(DEL_NOTWHOLE, pathNotWholeDel);
		MyUtils.persistent(MODIFY_CUTWHOLE, pathCutWholeModify);
//		 persistent(DEL_CUTWHOLE, pathCutWholeDel);
		MyUtils.persistent(HIGH_MATCH_SUB, pathHighMatchSubModify);
	}// persistentMemory

	/**
	 * [extractWordPort]内存整理函数 
	 * 1、进行一系列过滤 
	 * 2、去除那些确定不是单词的子集
	 * 3、由子串切割母串 提高精确度
	 * (最后代码重构，速度提升，将下列步骤集并非处理)
	 */
	private void memoryTrim() { // 数据处理核心
		MyUtils.log.info("过滤链开始。。。\n");
		final long s0 = System.nanoTime();
		filterChain(); // 过滤链
		final long e0 = System.nanoTime();
		MyUtils.log.info("过滤链结束！用时： " + (e0 - s0) / 1.0e9 + "s\n");
		/**
		 * 由上可以得到初步的词典 下面需要对词典进行优化 提高精确度 0代表前缀扫描 1代表后缀扫描
		 */
		delSubCutSet = new HashSet<String>();
		MyUtils.log.info("前缀扫描开始。。。\n");
		final long s1 = System.nanoTime();
		amendPrecision(0);
		final long e1 = System.nanoTime();
		MyUtils.log.info("前缀扫描结束！用时： " + (e1 - s1) / 1.0e9 + "s\n后缀扫描开始。。。\n");
		final long s2 = System.nanoTime();
		amendPrecision(1);
		final long e2 = System.nanoTime();
		MyUtils.log.info("后缀扫描结束！用时： " + (e2 - s2) / 1.0e9 + "s\n处理待删除串开始。。。\n");
		final long s3 = System.nanoTime();
		delSubCutSet(delSubCutSet);
		final long e3 = System.nanoTime();
		MyUtils.log.info("处理待删除串结束！用时： " + (e3 - s3) / 1.0e9 + "s\n");
	}// memoryTrim

	/**
	 * 获取对应的Cell
	 */
	private static Cell getCell(
			final HashMap<Integer, HashMap<String, Cell>> hash, final String str) {
		Cell cell = MyUtils.getHash(hash, str).get(str);
		return null != cell ? cell : new Cell();
	}

	/**
	 * 从所有的库中获取对应的Cell
	 */
	private static Cell getCellByAll(final String str) {
		if (MyUtils.isNotEmpty(ALL_WORD, str)) {
			return getCell(ALL_WORD, str);
		} else if (MyUtils.isNotEmpty(DEL_CUTWHOLE, str)) {
			return getCell(DEL_CUTWHOLE, str);
		} else if (MyUtils.isNotEmpty(DEL_NOTWHOLE, str)) {
			return getCell(DEL_NOTWHOLE, str);
		} else if (MyUtils.isNotEmpty(MODIFY_CUTWHOLE, str)) {
			return getCell(MODIFY_CUTWHOLE, str);
		} else if (MyUtils.isNotEmpty(HIGH_MATCH_SUB, str)) {
			return getCell(HIGH_MATCH_SUB, str);
		} else
			return null;
	}
}