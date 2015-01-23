package com.secneo.participle.optimize;

import java.util.HashMap;

import com.secneo.participle.domain.Cell;

/**
 * 将原有的文件格式 转换成Gson
 */
public class FileTransformGson {

	private HashMap<Integer, HashMap<String, Cell>> ALL_WORD;
	private HashMap<Integer, HashMap<String, Cell>> DEFINITE;
	private HashMap<Integer, HashMap<String, Cell>> MODIFY;
	private HashMap<Integer, HashMap<String, Cell>> MODIFY_GAME;
	private HashMap<Integer, HashMap<String, Cell>> MODIFY_HIGH_MATCH_L;
	private HashMap<Integer, HashMap<String, Cell>> MODIFY_HIGH_MATCH_R;
	private HashMap<Integer, HashMap<String, Cell>> NOISE;
	
	private static void transform() {
		
	}
	
	public static void main(String[] args) {
		transform();
	}
}
