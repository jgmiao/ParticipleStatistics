package com.secneo.participle.optimize;

import java.util.HashMap;
import java.util.Iterator;

import com.secneo.participle.domain.Cell;
import com.secneo.participle.util.MyDict;
import com.secneo.participle.util.MyUtils;

/**
 * 用噪声词来对全库进行扫描， 去除全库中含有噪声词的整词   优化Definite_All
 */
public class OptimizeDefiniteByNoise{
	
	private HashMap<Integer, HashMap<String, Cell>> ALL_WORD = new HashMap<Integer, HashMap<String, Cell>>();
	private HashMap<Integer, HashMap<String, Cell>> DEFINITE_DELL_NOISE = new HashMap<Integer, HashMap<String, Cell>>();
	private HashMap<Integer, HashMap<String, Cell>> NOISE = new HashMap<Integer, HashMap<String, Cell>>();
	
	private final static String ROUTE = "F:\\participle\\Result\\400W\\";
	private final static String PATH_DEFINITE = ROUTE + "definite_all.txt";
	private final static String PATH_NOISE = ROUTE + "noise.txt";
	private final static String PATH_DELL_NOISE = ROUTE + "definite_dell_noise.txt";
	private final static String PATH_NEW_DEFINITE = ROUTE + "new_definite.txt";
	
	private final static int MAX_WORD_LEN = 40; // 最大支持的子串长度
	
	private boolean checkInNoise(String str) {
		int strLen = str.length();
//		for (int i = 2; i < strLen; i++) {// 前缀
//			String sub = str.substring(0, i);
//			if(isNotEmpty(NOISE, sub))
//				return true;
//		}
		for (int i = strLen - 2; i > 0; i--) {// 后缀
			String sub = str.substring(i, strLen);
			if(MyUtils.isNotEmpty(NOISE, sub))
				return true;
		}
		return false;
	}
	
	private void optimize(){
		MyUtils.log.info("初始化内存开始。。。\n");
		ALL_WORD = MyDict.file2Memory(PATH_DEFINITE);
		NOISE = MyDict.file2Memory(PATH_NOISE);
		MyUtils.log.info("初始化内存成功！\n");
		for (int keyNum = MAX_WORD_LEN; keyNum > 0; keyNum--) {
			HashMap<String, Cell> itMap = ALL_WORD.get(keyNum);
			if (null == itMap)
				continue;
			Iterator<String> iterator = itMap.keySet().iterator();
			while (iterator.hasNext()) {
				String keyStr = iterator.next();
				if(checkInNoise(keyStr)) {
					Cell cell = MyUtils.getCell(ALL_WORD, keyStr);
					MyUtils.getHash(DEFINITE_DELL_NOISE, keyStr).put(keyStr, cell);
					iterator.remove();
					MyUtils.getHash(DEFINITE_DELL_NOISE, keyStr).remove(keyStr);
				}
			}
		}//for
		MyUtils.log.info("噪声词检测全库结束！\n");
		MyUtils.persistent(ALL_WORD, PATH_NEW_DEFINITE);
		MyUtils.persistent(DEFINITE_DELL_NOISE, PATH_DELL_NOISE);
		MyUtils.log.info("DEFINITE_ALL、DEFINITE_DELL_NOISE数据持久化完成。\n");
	}//optimize
	
	public static void main(String[] args) {
		OptimizeDefiniteByNoise odbn = new OptimizeDefiniteByNoise();
		final long s = System.nanoTime();
		odbn.optimize();
		final long e = System.nanoTime();
		MyUtils.log.info("Definitey优化结束。 用时：" + (e - s) / 1.0e9 + "s\n");
	}
}