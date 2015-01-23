package com.secneo.participle;

import com.secneo.participle.extract.ExtractWordTool;
import com.secneo.participle.split.SplitWordTool2;
import com.secneo.participle.weight.WeightCountPort;

/**
 *	分词各个功能接口的管理器
 *	可以测试、发布用 
 */
public class ParticipleHandler {

	@SuppressWarnings("unused")
	public static void main(String[] args) {
		String storageRoute = null;
		String soucePath = null;
		String systemName = System.getProperties().getProperty("os.name");
		System.out.println("System Name: " + systemName);
		if (systemName.startsWith("Windows") || systemName.startsWith("windows")) {
			storageRoute = "F:\\participle\\Thesaurus\\";
			soucePath = "F:\\participle\\Souce\\appName(5w).txt";
		} else {
			soucePath = args[0];
			storageRoute = args[1];
		}
//		extractWordSupport(soucePath, storageRoute); //抽词算法调试
		//MyUtils.initMemory();	//初始化内存
		splitWordSupport();	//分词算法调试
//		getWeightSupport();	//获取权值
	}
	
	/**
	 * 抽词算法接口的支持
	 * @param soucePath	apk_name的存储路径
	 * @param storageRoute	得到的词库最终要保存到的路径
	 */
	public static void extractWordSupport(String soucePath, String storageRoute) {
		ExtractWordTool trie = new ExtractWordTool(soucePath, storageRoute);
		trie.start();
	}
	
	/**
	 * 分词算法接口的支持
	 */
	public static void splitWordSupport() {
//		SplitWordTool2.splitWordPort("Word Hunter - 单词猎人 - iPlayEnglish");
		SplitWordTool2.splitWordPort("单词-猎人《完结版》");
	}
	
	/**
	 * 获取字串全值接口的支持
	 */
	public static void getWeightSupport() {
		System.out.println(WeightCountPort.getWeight("愤怒的小鸟壁纸"));
	}
}