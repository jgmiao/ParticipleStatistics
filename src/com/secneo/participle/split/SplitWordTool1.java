package com.secneo.participle.split;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import com.secneo.participle.domain.Cell;
import com.secneo.participle.util.MyDict;
import com.secneo.participle.util.MyUtils;
import com.secneo.participle.util.StringUtils;

public class SplitWordTool1{
	static final int DEFINITE_STATE = 1;
	static final int MODIFY_STATE = 2;
	static final int NOISE_STATE = 3;
	static HashMap<Integer, Cell> STATE_MAX_WEIGHT;// 状态的最大权值
	
	private static List<String> DATA_LIST = new ArrayList<String>();

	/**
	 *	允许接收List的构造函数 
	 */
	public SplitWordTool1(List<String> list){
		DATA_LIST = list;
	}
	
	/**
	 * 允许接受String的构造函数
	 */
	public SplitWordTool1(String str){
		DATA_LIST.add(str);
	}
	
	/**
	 * 分词工具唯一对外的接口
	 */
	public HashMap<String, HashSet<String>> start(){
		HashMap<String, HashSet<String>> resulteMap = new HashMap<String, HashSet<String>>();
		for (String s : DATA_LIST) {
			System.out.println("对 " + s + " 分词");
			HashSet<String> set = splitWordPort(s);
			resulteMap.put(s, set);
			for (String sub : set) {
				System.out.print(sub + " ");
			}
			System.out.println("\n");
		}
		return resulteMap;
	}
	
	private static String check(String str, int start, int end) {
		String sub = str.substring(start, end);
		if(sub.length() < 2)
			return null;
		/**
		 * Case: modify_game
		 */
		else if (MyUtils.isNotEmpty(MyDict.MODIFY_GAME, sub)) {
			return sub;
		}
		/**
		 * Case: modify
		 */
		else if (MyUtils.isNotEmpty(MyDict.MODIFY, sub)) {
			return sub;
		}
		return null;
	}//check

	/**
	 * 核心算法 结合definit_all 以及各个修饰库来递归切割词串
	 */
	private static HashSet<String> checkByModify(String str) {
		HashSet<String> subSet = new HashSet<String>();
		int strLen = str.length();
		String maxWeightR = "";
		String maxWeightL = "";
		for (int i = 0; i < strLen - 1; i++) {
			/**
			 * TODO check的返回需要改成一个对象 或者利用好STATE_MAX_WEIGHT
			 */
			String subBuffR = check(str, i, strLen); // 后缀扫描
			if (StringUtils.isNotEmpty(subBuffR)) {
				if (maxWeightR.equals("")
						|| MyUtils.getCellAll(subBuffR).getCountSub() > MyUtils.getCellAll(
								maxWeightR).getCountSub()) {
					maxWeightR = subBuffR;
				}
				String surpluStr = str.replace(subBuffR, "");
				if (checkByDefiniteAll(surpluStr)) {
					subSet.add(subBuffR);
					if(StringUtils.isNotEmpty(surpluStr))
						subSet.add(surpluStr);
					return subSet;
				}
			}//后缀结束
			String subBuffL = check(str, 0, strLen - i - 1); // 前缀扫描
			if (StringUtils.isNotEmpty(subBuffL)) {
				if (maxWeightL.equals("")
						|| MyUtils.getCellAll(subBuffL).getCountSub() > MyUtils.getCellAll(
								maxWeightL).getCountSub()) {
					maxWeightL = subBuffL;
				}
				String surpluStr = str.replace(subBuffL, "");
				if (checkByDefiniteAll(surpluStr)) {
					subSet.add(subBuffL);
					if(StringUtils.isNotEmpty(surpluStr))
						subSet.add(surpluStr);
					return subSet;
				}
			}//前缀结束
		}// for
		String maxSurpluR = str.replace(maxWeightR, "");
		String maxSurpluL = str.replace(maxWeightL, "");
		if (!maxWeightR.equals("") && !maxWeightL.equals("")) {	//1、左右均有匹配
			if(MyUtils.getCellAll(maxWeightR).getCountSub() > MyUtils.getCellAll(maxWeightL).getCountSub()){
				subSet.add(maxWeightR);
				if(StringUtils.isNotEmpty(maxSurpluR))
					subSet.add(maxSurpluR);
			} else {
				subSet.add(maxWeightL);
				if(StringUtils.isNotEmpty(maxSurpluL))
					subSet.add(maxSurpluL);
			}
		} else if(!maxWeightR.equals("") && maxWeightL.equals("")){	//2、只有右缀有匹配
			subSet.add(maxWeightR);
			if(StringUtils.isNotEmpty(maxSurpluR))
				subSet.add(maxSurpluR);
		} else if(!maxWeightL.equals("") && maxWeightR.equals("")){	//3、只有左缀有匹配
			subSet.add(maxWeightL);
			if(StringUtils.isNotEmpty(maxSurpluL))
				subSet.add(maxSurpluL);
		} else { //4、词串在modify中未命中   则在整库中查询
//			if(checkByDefiniteAll(str)){
//				subSet.add(str);
//			}
			subSet.add(str);
		}
		return subSet;
	}//checkByModify

	/**
	 * 由definite词库来检测
	 * @return 在definite库中是否找到
	 */
	private static boolean checkByDefinite(String str) {
		if (MyUtils.isNotEmpty(MyDict.DEFINITE, str)) { // 全词检查有无确定词的匹配
			if (null != STATE_MAX_WEIGHT.get(DEFINITE_STATE)) {
				if (STATE_MAX_WEIGHT.get(DEFINITE_STATE).getCountSub() < MyUtils.getCell(
				        MyDict.DEFINITE, str).getCountSub()) {
					//TODO 需要做判重  再去除 需引入起始位置、终止位置
					STATE_MAX_WEIGHT.remove(DEFINITE_STATE);
					STATE_MAX_WEIGHT.put(DEFINITE_STATE, MyUtils.getCell(MyDict.DEFINITE, str));
				}
			} else {// 初始化
				STATE_MAX_WEIGHT.put(DEFINITE_STATE, MyUtils.getCell(MyDict.DEFINITE, str));
			}
			return true;
		}
		return false;
	}//checkByDefinite

	/**
	 * 由全词库difinite_all来检测
	 * @return 在definite_all中是否找到
	 */
	private static boolean checkByDefiniteAll(String str) {
		if (MyUtils.isNotEmpty(MyDict.DEFINITE_ALL, str)) {
			System.out.println(str + "是一个整词的应用");
			return true;
		} else {
			System.out.println("被切割剩余串 " + str + "在全库中未命中");
			// TODO 下一步的未匹配词的回收算法
		}
		return false;
	}//checkByDefiniteAll

	/**
	 * 由noise词库来检测
	 * @return 在noise库中是否找到
	 */
	private static boolean checkByNoise(String str) {
		if (MyUtils.isNotEmpty(MyDict.NOISE, str)) {
			return true;
		}
		return false;
	}//checkByNoise

	/**
	 * 分词接口
	 * 
	 * @return 一个子集的集合
	 */
	private static HashSet<String> splitWordPort(String apkName) {
		HashSet<String> subSet = new HashSet<String>();
		if(!StringUtils.isContainChinese(apkName)){	//先做中文判断 如果未出现中文 直接返回
			subSet.add(apkName);
			return subSet;
		}
		boolean flag = true;
		STATE_MAX_WEIGHT = new HashMap<Integer, Cell>();
		final String str = StringUtils.stringFilter(apkName);
		final int strLen = str.length();
		/**
		 * Case1:精确确定词库(definite, noise) TODO 可能需要后缀匹配改成全词扫描
		 */
		for (int i = strLen; i >= 2; i--) { // i为需要切割的长度
			final String sub = str.substring(strLen - i, strLen);
			if (flag && checkByNoise(sub)) {// 噪声词 返回原串的列表形式
				System.out.println(str + "由 " + sub + " 判定为噪声词。");
				subSet.add(str);
				return subSet;
			}
			if (checkByDefinite(sub))
				flag = false;
		}// for 后缀扫描
		for (int i = strLen - 1; i >= 2; i--) {
			final String sub = str.substring(0, i);
			if (flag && checkByNoise(sub)) {
				System.out.println(str + "由" + sub + "判定为噪声词。");
				subSet.add(sub);
				return subSet;
			}
			if (checkByDefinite(sub))
				flag = false;
		}// for 前缀扫描
		/**
		 * Case2:修饰词库(modify, modify_game, modify_role) 当匹配到确定词时 对剩余串的检查
		 */
		if (STATE_MAX_WEIGHT.containsKey(DEFINITE_STATE)) {
			String maxWeightName = STATE_MAX_WEIGHT.get(DEFINITE_STATE).getName();
			subSet.add(maxWeightName);
			System.out.println(str + "由" + maxWeightName + "判定为应用词。");
			String surpluStr = str.replace(maxWeightName, "");
			if (StringUtils.isNotEmpty(surpluStr)) {
				subSet.addAll(checkByModify(surpluStr));
			}
		} else { // Case3: 当没有精确匹配的词的时候 到modify中进行递归分词
			subSet.addAll(checkByModify(str));
		}
		return subSet;
	}//splitWordPort

	public static void main(String[] args) {
//		String[] names = { "古道荒屋动态壁纸", "King of Fishing", "课表", "天天泡泡龙",
//				"许嵩粉丝论坛", "同居万岁", "The Moron Test", "弹弹岛战纪", "公路骑手3D",
//				"Quake Alarma", "草丛大作战", "美女公馆", "都市之古武风流", "超级泡泡龙",
//				"Happy Diwali SMS", "帝王.三国之傲视天下", "妈妈值得买", "疯狂的水果",
//				"后宫甄嬛传·贰（典藏版）", "指纹扫描", "Wall Car", "快打旋风", "GTalkSMS",
//				"Talk German (Free)", "清新风格", "Dark TD", "Tank Recon 3D",
//				"大头贴", "战国四大名将", "优酷", "重装机兵3图文攻略", "诛神OL",
//				"Clock Battery Widget Ferrari F12 Red Free", "毁灭妖塔", "恋游乐园",
//				"航空管制员", "全能钢琴", "搞怪碰碰球", "XmarkSync", "天朝小将", "Surfing",
//				"掌门眼配置工具", "清新陶瓷", "盗墓笔记全集", "Nana Wallpaper", "Vienna",
//				"优化大师万能卸载器", "Moto mApps Utah FREE", "ExionFly",
//				"ShareTextPicker", "World of Goo", "大稚慧早教通", "Reuters UK News",
//				"Link Battle", "重生之定三国", "超亮手电筒", "Weddings Japan", "同城物流网",
//				"惠林万家", "水果泡泡", "流量检测器", "驴友罗盘" };
		String[] names = { "QQ阅读" };
		for (String s : names) {
			System.out.println("对 " + s + " 分词");
			HashSet<String> set = splitWordPort(s);
			for (String sub : set) {
				System.out.print(sub + " ");
			}
			System.out.println("\n");
		}
	}
}