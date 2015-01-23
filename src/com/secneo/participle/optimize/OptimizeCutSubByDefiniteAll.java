package com.secneo.participle.optimize;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import com.secneo.participle.domain.Cell;
import com.secneo.participle.util.MyDict;
import com.secneo.participle.util.MyUtils;

/**
 *	存在问题： modify_high_match.txt库中有大量的不该是修饰词的高频匹配项
 *	如：疯狂的小、方旗舰店、合金弹、人力资
 *	解决思路： 上述例子中 其母串都是在All库中确定的应用词  愤怒的小鸟、官方旗舰店、合金弹头、人力资源  等等。。。
 *	而抽词算法在计算其左右匹配集时  其频繁度完全依赖于母串，而自身并不频繁
 *	我们要做的  就是判断该词是否是All库中某个词的子串  如果是 则做相关处理
 */
public class OptimizeCutSubByDefiniteAll{
	
	private static final int MIN_MODIFY_SUB_MATCH = 5;// 最小修饰词匹配项支持度 大于 则认为该子串为修饰词
	
	private HashMap<Integer, HashMap<String, Cell>> ALL_WORD = new HashMap<Integer, HashMap<String, Cell>>();
	private HashMap<Integer, HashMap<String, Cell>> HIGH_MATCH_SUB_OPTIMIZE = new HashMap<Integer, HashMap<String, Cell>>();
	private HashMap<Integer, HashMap<String, Cell>> HIGH_MATCH_SUB_DEL = new HashMap<Integer, HashMap<String, Cell>>();
	
	private final static String ROUTE = "F:\\participle\\Result\\dictionary\\";
	private final static String PATH_ALL = ROUTE + "definite_all.txt";
	private final static String PATH_HIGH_MATCH = ROUTE + "modify_high_match.txt";
	private final static String PATH_HIGH_MATCH_OPTIMIZE = ROUTE + "modify_high_match_new.txt";
	private final static String PATH_HIGH_MATCH_DEL = ROUTE + "modify_high_match_del.txt";

	private void optimize() {
		HashSet<String> tmprMatch = null;
		HashSet<String> tmplMatch = null;
		HashSet<String> singlerMatch = null;
		HashSet<String> singlelMatch = null;
		HashMap<String, Cell> tmpMap = null;

		MyUtils.log.info("初始化内存开始。。。\n");
		ALL_WORD = MyDict.file2Memory(PATH_ALL);
		HIGH_MATCH_SUB_OPTIMIZE = MyDict.file2Memory(PATH_HIGH_MATCH);
		MyUtils.log.info("初始化内存成功！\n");
		
		for (int keyNum : HIGH_MATCH_SUB_OPTIMIZE.keySet()) {
			tmpMap = HIGH_MATCH_SUB_OPTIMIZE.get(keyNum);
			Iterator<String> tmpItor = tmpMap.keySet().iterator();
			while(tmpItor.hasNext()) {
				String tmpStr = tmpItor.next();
				if (MyUtils.isNotEmpty(ALL_WORD, tmpStr)) {	//如果高频词在全库中存在  则去除
					MyUtils.getHash(HIGH_MATCH_SUB_DEL, tmpStr).put(tmpStr, tmpMap.get(tmpStr));
					tmpItor.remove();
					MyUtils.getHash(HIGH_MATCH_SUB_OPTIMIZE, tmpStr).remove(tmpStr);
					continue;
				}
				tmprMatch = tmpMap.get(tmpStr).getrMatch();
				tmplMatch = tmpMap.get(tmpStr).getlMatch();
				singlerMatch = new HashSet<String>();
				singlelMatch = new HashSet<String>();
				for (String match : tmprMatch) {//match作为前缀
					if (match.length() > 0) {
						singlerMatch.add(match.substring(0, 1));
					}
				}
				for (String match : tmplMatch) {//match作为后缀
					if (match.length() > 0) {
						singlelMatch.add(match.substring(match.length() - 1, match.length()));
					}
				}
				if (singlelMatch.size() > singlerMatch.size()
						|| (singlerMatch.size() < MIN_MODIFY_SUB_MATCH && singlelMatch
								.size() < MIN_MODIFY_SUB_MATCH)) {
					System.out.println("deling..." + tmpStr);
					MyUtils.getHash(HIGH_MATCH_SUB_DEL, tmpStr).put(tmpStr, tmpMap.get(tmpStr));
					tmpItor.remove();
					MyUtils.getHash(HIGH_MATCH_SUB_OPTIMIZE, tmpStr).remove(tmpStr);
				}
			}//while
		}//for
		MyUtils.persistent(HIGH_MATCH_SUB_OPTIMIZE, PATH_HIGH_MATCH_OPTIMIZE);
		MyUtils.persistent(HIGH_MATCH_SUB_DEL, PATH_HIGH_MATCH_DEL);
	}//optimize
	
	public static void main(String[] args) {
		OptimizeCutSubByDefiniteAll ocsbda = new OptimizeCutSubByDefiniteAll();
		final long s = System.nanoTime();
		ocsbda.optimize();
		final long e = System.nanoTime();
		MyUtils.log.info("Definitey优化结束。 用时：" + (e - s) / 1.0e9 + "s\n");
	}
}