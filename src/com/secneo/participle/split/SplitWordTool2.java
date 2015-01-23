package com.secneo.participle.split;

import java.util.HashSet;

import com.secneo.participle.domain.Cell;
import com.secneo.participle.util.MyDict;
import com.secneo.participle.util.MyUtils;
import com.secneo.participle.util.StringUtils;

public class SplitWordTool2 {
	
	/**
	 * [分词工具对外接口] 对apkName进行分词
	 * @return 一个子集的集合 HashSet<Cell>
	 */
	public static HashSet<Cell> splitWordPort(String apkName) {
		if (!StringUtils.isNotEmpty(apkName)) {
			return new HashSet<Cell>();
		}
		HashSet<Cell> cellSet = new HashSet<Cell>();
		// 预处理
		cellSet = pretreatment(apkName, cellSet);
		// 做中文判断 如果未出现中文 直接返回
		if(!StringUtils.isContainChinese(apkName)){
			Cell sCell = new Cell(apkName);
			sCell.setState(MyDict.DEFINITE_ALL_STATE);
			cellSet.add(sCell);
			return cellSet;
		}
		final String str = StringUtils.stringFilter(apkName);
		final int strLen = str.length();
		/**
		 * 前缀递归字串 在各个库中判断 并记录状态
		 * @算法说明
		 * splitWord()是遵循前缀的扫描的，每次从sub的第0位扫描至倒数第二位
		 * 而在外层的splitWordPort()是将基于后缀的词串取出 放入splitWord中的
		 * 这样  即可在保证递归的单一性（前缀）的情况下 遍历出所有的情况 
		 */
		for(int i = 0; i < strLen - 1; i++) {
			cellSet = splitWord(str, i , strLen, cellSet);
		}
		// 补漏  如果之前全部没有命中 则返回原始信息
		if(cellSet.size() <= 0) {
			Cell sCell = new Cell(apkName);
			sCell.setState(MyDict.DEFINITE_ALL_STATE);
			cellSet.add(sCell);
		}
		return cellSet;
	}//splitWordPort
	
	/**
	 * 对字串的预处理
	 * 将以‘-’，‘_’，‘ ’等做处理
	 */
	private static HashSet<Cell> pretreatment(String str, HashSet<Cell> cellSet) {
		char[] strs;
		final char[] signs = {'-', '_', ' ', '，', ',', '：', ':', '《', '》'};
		for (int iStr = 0; iStr < str.length(); iStr++) {
			strs = str.toCharArray();
			for (int jSign = 0; jSign < signs.length; jSign++) {
				if (strs[iStr] == signs[jSign]) {
					String sub = str.substring(0, iStr+1);
					Cell cell = new Cell(sub);
					cell.setState(MyDict.MODIFY_STATE);
					cellSet.add(cell);
					str = str.replace(sub, "");
					break;
				}
			}
		}
		return cellSet;
	}
	
	/**
	 * 【核心算法】  前缀递归检测词串归属于哪种状态
	 * 即 接收一个词  此算法可以给你一个集合  里面会有详细的被切割出来词的信息
	 * 信息包括  词名、起始、终点位置、状态
	 * 
	 * @param str  传进来的字串
	 * @param start  切割的起始位置
	 * @param end	切割的终止位置
	 * @param cellSet  返回的结果集
	 * @return	返回命中词的Cell set
	 */
	private static HashSet<Cell> splitWord(String str, int start, int end, HashSet<Cell> cellSet) {
		if (!StringUtils.isNotEmpty(str) || str.length() <= 1)	//结束条件
			return cellSet;
		if (start >= str.length() - 1 || end <= 1 || start >= end) //枝剪
			return cellSet;
		String sub = str.substring(start, end);	//获取切割串
		int subLen = sub.length();
		/**
		 * 在全库中先找 
		 * 命中：则将字串状态标为DEFINITE_ALL 且将串的子串继续向下层检测
		 * 未命中： 则将字串交给修饰库中去检测
		 */
		Cell definiteAllCell = checkInDefiniteAll(sub, start, end);
		if (null != definiteAllCell) {
			cellSet.add(definiteAllCell);
				cellSet.addAll(splitWord(sub, 0, subLen - 1, cellSet));
		} else {
			Cell otherCell = checkWithoutDefiniteAll(sub, start, end);
			if (null != otherCell) { //当在除全库外的库中命中时  
				cellSet.add(otherCell);
			} //结束后 对子集开始前缀递归检查
			cellSet.addAll(splitWord(sub, 0, subLen - 1, cellSet)); 
		}
		return cellSet;
	}//splitWord
	
	/**
	 * 由全词库difinite_all来检测
	 * @return Cell[name, start, end, state]
	 */
	private static Cell checkInDefiniteAll(String str, int start, int end) {
		Cell sCell = null;
		if (MyUtils.isNotEmpty(MyDict.DEFINITE_ALL, str)) {
			sCell = new Cell(str);
			if (MyUtils.isNotEmpty(MyDict.NOISE, str)) {
				sCell.setState(MyDict.NOISE_STATE);
				MyUtils.log.info(str + "是一个噪声词\n");
			} else {
				sCell.setState(MyDict.DEFINITE_ALL_STATE);
				MyUtils.log.info(str + "是一个整词的应用\n");
			}
		}
		return sCell;
	}//checkInDefiniteAll
	
	/**
	 * 由noise, definite, modify, modify_game, modify_role来检测
	 * @return Cell[name, start, end, state]
	 */
	private static Cell checkWithoutDefiniteAll(String str, int start, int end) {
		Cell sCell = new Cell(str);
		if (MyUtils.isNotEmpty(MyDict.NOISE, str)) { //noise库的判断
			sCell.setState(MyDict.NOISE_STATE);
			MyUtils.log.info(str + " 在noise中命中\n");
		} else if (MyUtils.isNotEmpty(MyDict.DEFINITE, str)) { //DEFINITE库的判断
			sCell.setState(MyDict.DEFINITE_STATE);
			MyUtils.log.info(str + " 在definite中命中\n");
		} else if (MyUtils.isNotEmpty(MyDict.MODIFY, str)) { //MODIFY库的判断
			sCell.setState(MyDict.MODIFY_STATE);
			MyUtils.log.info(str + " 在modify中命中\n");
		} else if (MyUtils.isNotEmpty(MyDict.MODIFY_GAME, str)) { //MODIFY_GAME库的判断
			sCell.setState(MyDict.MODIFY_GAME_STATE);
			MyUtils.log.info(str + " 在modify_game中命中\n");
		} else if (MyUtils.isNotEmpty(MyDict.MODIFY_HIGH_MATCH_LEFT, str) //MODIFY_HIGH_MATCH库的判断
				|| MyUtils.isNotEmpty(MyDict.MODIFY_HIGH_MATCH_RIGHT, str)) {
			sCell.setState(MyDict.MODIFY_HIGH_MATCH_STATE);
			MyUtils.log.info(str + " 在modify_high_match中命中\n");
		} else return null;
		return sCell;
	}//checkWithoutDefiniteAll
}