package com.secneo.participle.optimize;

import java.util.HashMap;
import java.util.Iterator;

import com.secneo.participle.domain.Cell;
import com.secneo.participle.util.MyDict;
import com.secneo.participle.util.MyUtils;

/**
 * 存在问题: 一些虽然其匹配集的种类很低 但是其频率很高 
 * 如: “圆桌” 其匹配复杂度只有“圆桌武士”，“圆桌骑士” 但其各自出现频率很高 
 * 解决思路: 从DEL_NOTWHOLE找到高频的词 
 * 因为 本身存放在DEL_NOTWHOLE中的词是由于过低的复杂度放入 现在重新有频度来找回
 */
public class OptimizeNotWholeByCount {

	private HashMap<Integer, HashMap<String, Cell>> DEL_NOTWHOLE;
	private HashMap<Integer, HashMap<String, Cell>> NOTWHOLE_HIGH_COUNT;
	
//	private final static String ROUTE = "F:\\participle\\Result\\400W（optimize）5-28\\";
	private final static String ROUTE = "/home/sec/MiaoJiaGuo/optimize/";
	private final static String PATH_DEL_NOTWHOLE = ROUTE + "[2]del_word_notwhole.txt";
	private final static String PATH_NOTWHOLE_HIGH_COUNT = ROUTE + "notwhole_high_count.txt";
	
	private final static int MAX_WORD_LEN = 40; // 最大支持的子串长度
	private final static int MIN_HIGH_COUNT_NOTWHOLE = 500; //最小认同需要回收的 不是整词的的高频子串
	
	private void optimize() {
		MyUtils.log.info("初始化内存开始。。。\n");
		DEL_NOTWHOLE = MyDict.file2Memory(PATH_DEL_NOTWHOLE);
		MyUtils.log.info("初始化内存成功！\n");
		Iterator<String> tmprMatch = null;
		Iterator<String> tmplMatch = null;
		for (int keyNum = MAX_WORD_LEN; keyNum > 0; keyNum--) {
			HashMap<String, Cell> itMap = DEL_NOTWHOLE.get(keyNum);
			if (null == itMap)
				continue;
			Iterator<String> iterator = itMap.keySet().iterator();
			while (iterator.hasNext()) {
				String keyStr = iterator.next();
				Cell cell = MyUtils.getCell(DEL_NOTWHOLE, keyStr);
				if(cell.getCount() > MIN_HIGH_COUNT_NOTWHOLE) {
					boolean flag = false;
					tmprMatch = cell.getrMatch().iterator();
					tmplMatch = cell.getlMatch().iterator();
					while (tmprMatch.hasNext()) {
						if (tmprMatch.next().length() > 0 ) {
							flag = true;
							break;
						}
					}
					while (tmplMatch.hasNext()) {
						if (tmplMatch.next().length() > 0 ) {
							flag = true;
							break;
						}
					}
					if (flag) {
						MyUtils.getHash(NOTWHOLE_HIGH_COUNT, keyStr).put(keyStr, cell);
					}
				}
			}
		}//for
		MyUtils.log.info("高频子串回收成功！\n");
		MyUtils.persistent(NOTWHOLE_HIGH_COUNT, PATH_NOTWHOLE_HIGH_COUNT);
		MyUtils.log.info("NOTWHOLE_HIGH_COUNT数据持久化完成。\n");
	}//optimize
	
	public static void main(String[] args) {
		OptimizeNotWholeByCount onwbc = new OptimizeNotWholeByCount();
		final long s = System.nanoTime();
		onwbc.optimize();
		final long e = System.nanoTime();
		MyUtils.log.info("Definitey优化结束。 用时：" + (e - s) / 1.0e9 + "s\n");
	}
}