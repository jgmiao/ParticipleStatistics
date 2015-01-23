package com.secneo.participle.util;

import java.util.HashMap;

import org.apache.log4j.Logger;

import com.secneo.participle.domain.Cell;

public class MyUtils {

    public static Logger log = Logger.getLogger(MyUtils.class);

    /**
     * 首先初始化字典信息，单例
     */
    static {
        String pathDefinite = Config.i.dict_file_dir + "definite.txt";
        String pathDefiniteAll = Config.i.dict_file_dir + "definite_all.txt";
        String pathModify = Config.i.dict_file_dir + "modify.txt";
        String pathModifyGame = Config.i.dict_file_dir + "modify_game.txt";
        String pathModifyHighMatchL = Config.i.dict_file_dir + "modify_high_match_l.txt";
        String pathModifyHighMatchR = Config.i.dict_file_dir + "modify_high_match_r.txt";
        String pathNoise = Config.i.dict_file_dir + "noise.txt";
        log.info("DEFINITE\n");
        MyDict.DEFINITE = MyDict.file2Memory(pathDefinite);
        log.info("DEFINITE_ALL\n");
        MyDict.DEFINITE_ALL = MyDict.file2Memory(pathDefiniteAll);
        log.info("MODIFY\n");
        MyDict.MODIFY = MyDict.file2Memory(pathModify);
        log.info("MODIFY_GAME\n");
        MyDict.MODIFY_GAME = MyDict.file2Memory(pathModifyGame);
        log.info("MODIFY_HIGH_MATCH_LEFT\n");
        MyDict.MODIFY_HIGH_MATCH_LEFT = MyDict.file2Memory(pathModifyHighMatchL);
        log.info("MODIFY_HIGH_MATCH_RIGHT\n");
        MyDict.MODIFY_HIGH_MATCH_RIGHT = MyDict.file2Memory(pathModifyHighMatchR);
        log.info("NOISE\n");
        MyDict.NOISE = MyDict.file2Memory(pathNoise);
        log.info("初始化内存成功。\n");
    }
	
	/**
	 * 持久化内存
	 * @param hash
	 * @param path
	 */
	public static void persistent(HashMap<Integer, HashMap<String, Cell>> hash,
			String path) {
		int count = 0;
		StringBuffer sbf = new StringBuffer();
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
						.append(lMatch).append(rMatch + "\r\n");
				if (++count % 10000 == 0)
					WriteFile.write(sbf.toString(), path);
			}
			if (count % 10000 != 0)
				WriteFile.write(sbf.toString(), path);
		}
	}// persistent
	
	/**
	 * 得到对应的HashMap
	 */
	public static HashMap<String, Cell> getHash(
			final HashMap<Integer, HashMap<String, Cell>> hash, final String str) {
		if (!hash.containsKey(str.length())) {
			hash.put(str.length(), new HashMap<String, Cell>());
		}
		return hash.get(str.length());
	}
	
	/**
	 * 获取对应的Cell
	 */
	public static Cell getCell(HashMap<Integer, HashMap<String, Cell>> hash,
			String str) {
		Cell cell = getHash(hash, str).get(str);
		return null != cell ? cell : null;
	}

	/**
	 * 从所有的库中获取对应的Cell
	 */
	public static Cell getCellAll(String str) {
		if (isNotEmpty(MyDict.DEFINITE, str)) {
			return getCell(MyDict.DEFINITE, str);
		} else if (isNotEmpty(MyDict.DEFINITE_ALL, str)) {
			return getCell(MyDict.DEFINITE_ALL, str);
		} else if (isNotEmpty(MyDict.MODIFY, str)) {
			return getCell(MyDict.MODIFY, str);
		} else if (isNotEmpty(MyDict.MODIFY_GAME, str)) {
			return getCell(MyDict.MODIFY_GAME, str);
		} else if (isNotEmpty(MyDict.MODIFY_HIGH_MATCH_LEFT, str)) {
			return getCell(MyDict.MODIFY_HIGH_MATCH_LEFT, str);
		} else if (isNotEmpty(MyDict.MODIFY_HIGH_MATCH_RIGHT, str)) {
			return getCell(MyDict.MODIFY_HIGH_MATCH_RIGHT, str);
		} else if (isNotEmpty(MyDict.NOISE, str)) {
			return getCell(MyDict.NOISE, str);
		} else {
			return null;
		}
	}

	/**
	 * 判断HashMap内是否有元素
	 */
	public static boolean isNotEmpty(
			HashMap<Integer, HashMap<String, Cell>> hash, String s) {
		return hash.containsKey(s.length())
				&& hash.get(s.length()).containsKey(s) ? true : false;
	}
}