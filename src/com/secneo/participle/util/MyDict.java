package com.secneo.participle.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.secneo.participle.domain.Cell;


public class MyDict {

    public static HashMap<Integer, HashMap<String, Cell>> DEFINITE;
    public static HashMap<Integer, HashMap<String, Cell>> DEFINITE_ALL;
    public static HashMap<Integer, HashMap<String, Cell>> MODIFY;
    public static HashMap<Integer, HashMap<String, Cell>> MODIFY_GAME;
    public static HashMap<Integer, HashMap<String, Cell>> MODIFY_HIGH_MATCH_LEFT;
    public static HashMap<Integer, HashMap<String, Cell>> MODIFY_HIGH_MATCH_RIGHT;
    public static HashMap<Integer, HashMap<String, Cell>> NOISE;

    public static final int DEFINITE_ALL_STATE = 0;
    public static final int DEFINITE_STATE = 1;
    public static final int MODIFY_STATE = 2;
    public static final int MODIFY_GAME_STATE = 3;
    public static final int MODIFY_HIGH_MATCH_STATE = 4;
    public static final int NOISE_STATE = 5;
    
    public static final String CUT_BY = "cutBy-->";
    public static final String L_MATCH = "lMatch-->";
    public static final String R_MATCH = "rMatch-->";
    private static final Pattern STR_PATTERN = Pattern.compile("(.+): *(-?[0-9]\\d*) *sub-->(-?[0-9]\\d*) *whole-->([0-9]\\d*) *aggregate-->(-?([1-9]\\d*\\.\\d*|0\\.\\d*[1-9]\\d*|0?\\.0+|0)) *");
    
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
        MyUtils.log.info("DEFINITE内存化成功\n");
        DEFINITE = file2Memory(pathDefinite);
        MyUtils.log.info("DEFINITE_ALL内存化成功\n");
        DEFINITE_ALL = file2Memory(pathDefiniteAll);
        MyUtils.log.info("MODIFY内存化成功\n");
        MODIFY = file2Memory(pathModify);
        MyUtils.log.info("MODIFY_GAME内存化成功\n");
        MODIFY_GAME = file2Memory(pathModifyGame);
        MyUtils.log.info("MODIFY_HIGH_MATCH_LEFT内存化成功\n");
        MODIFY_HIGH_MATCH_LEFT = file2Memory(pathModifyHighMatchL);
        MyUtils.log.info("MODIFY_HIGH_MATCH_RIGHT内存化成功\n");
        MODIFY_HIGH_MATCH_RIGHT = file2Memory(pathModifyHighMatchR);
        MyUtils.log.info("NOISE内存化成功\n");
        NOISE = file2Memory(pathNoise);
        MyUtils.log.info("初始化内存成功。\n");
    }
    
	/**
	 * 从硬盘将词库录入内存中
	 */
	public static HashMap<Integer, HashMap<String, Cell>> file2Memory(String path) {
		MyUtils.log.info(path);
		String s = null;
		File file = new File(path);
		HashMap<Integer, HashMap<String, Cell>> hash = new HashMap<Integer, HashMap<String, Cell>>();
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			while ((s = br.readLine()) != null) {
				String wholeStr = s.toString();
				Matcher matchr = STR_PATTERN.matcher(wholeStr);
				if (matchr.find()) {
					Cell cell = new Cell();
					String patternStr = matchr.group(0);
					String name = matchr.group(1).trim();
					int count = Integer.parseInt(matchr.group(2)); // 获取频率
					int countSub = Integer.parseInt(matchr.group(3));// 获取子串出现数
					int countWhole = Integer.parseInt(matchr.group(4));// 获取整串出现数
					String[] cutBy = null;
					String[] lMatch = null;
					String[] rMatch = null;
					String dataStr = wholeStr.replace(patternStr, "").trim();
					String[] dataStrs = dataStr.split("\\|\\|");
					if (dataStrs.length > 3) {
						MyUtils.log.error("过度分裂！！" + wholeStr + "\n");
					} else if (dataStrs.length < 3) {
						MyUtils.log.error("分裂过少！！" + wholeStr + "\n");
					}
					
					if (dataStrs[0].startsWith(CUT_BY)) {
						cutBy = dataStrs[0].replace(CUT_BY, "").split(" ");
					} else {
						MyUtils.log.error("Cut_by出错！！" + wholeStr + "\n");
					}
					
					if (dataStrs[1].startsWith(L_MATCH)) {
						lMatch = dataStrs[1].replace(L_MATCH, "").split(" ");
					} else {
						MyUtils.log.error("LMatch出错！！" + wholeStr + "\n");
					}
					
					if (dataStrs[2].startsWith(R_MATCH)) {
						rMatch = dataStrs[2].replace(R_MATCH, "").split(" ");
					} else {
						MyUtils.log.error("RMatch出错！！" + wholeStr + "\n");
					}
					
					cell.setCount(count);
					cell.setCountSub(countSub);
					cell.setCountWhole(countWhole);
					cell.setCutBy(new HashSet<String>(Arrays.asList(cutBy)));
					cell.setlMatch(new HashSet<String>(Arrays.asList(lMatch)));
					cell.setrMatch(new HashSet<String>(Arrays.asList(rMatch)));
					MyUtils.getHash(hash, name).put(name, cell);
				} else {
					MyUtils.log.error("正则未匹配到" + wholeStr);
				}
			}// while
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return hash;
	}//file2Memory
}