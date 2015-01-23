package com.secneo.participle.domain;

import java.util.HashSet;

/**
 * 详细记录每个单元词的各种数据
 */
public class Cell {
	private String name;
	private int state; // 记录字串的词性
	private int count; // 词出现的总频率
	private int countSub = 0; // 词作为子串出现的频率
	private int countWhole = 0; // 词作为一个整体出现的频率
	private HashSet<String> lMatch = new HashSet<String>();; // 左匹配集
	private HashSet<String> rMatch = new HashSet<String>();; // 右匹配集
	private HashSet<String> cutBy = new HashSet<String>();; // 该词是由什么子串切割的
	private float minAggregate = 0;

	// //管理匹配集的映射(String --> Integer)
	// private static HashMap<String, Integer> lMatchMappingStrToInt = new
	// HashMap<String, Integer>();
	// private static HashMap<String, Integer> rMatchMappingStrToInt = new
	// HashMap<String, Integer>();
	// //管理匹配集的映射(Integer --> String)
	// private static HashMap<Integer, String> lMatchMappingIntToStr = new
	// HashMap<Integer, String>();
	// private static HashMap<Integer, String> rMatchMappingIntToStr = new
	// HashMap<Integer, String>();
	// // 维护映射的当前数量
	// private static int lMatchNowNum = 0;
	// private static int rMatchNowNum = 0;
	// private HashSet<String> rebackSet = new HashSet<String>(512);

	public Cell() {
		this.count = 0;
	}

	public Cell(int n) {
		this.count = n;
	}

	public Cell(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public int getCountSub() {
		return countSub;
	}

	public void setCountSub(int countSub) {
		this.countSub = countSub;
	}

	public int getCountWhole() {
		return countWhole;
	}

	public void setCountWhole(int countWhole) {
		this.countWhole = countWhole;
	}

	// public HashSet<String> getlMatch() {
	// rebackSet.clear();
	// for(int i : this.lMatch) {
	// rebackSet.add(lMatchMappingIntToStr.get(i));
	// }
	// return rebackSet;
	// }
	public HashSet<String> getlMatch() {
		return this.lMatch;
	}

	// public void setlMatch(HashSet<String> set) {
	// for(String s : set) {
	// if (lMatchMappingStrToInt.containsKey(s)) {
	// lMatchMappingStrToInt.put(s, ++lMatchNowNum);
	// lMatchMappingIntToStr.put(lMatchNowNum, s);
	// }
	// this.lMatch.add(lMatchMappingStrToInt.get(s));
	// }
	// }
	public void setlMatch(HashSet<String> set) {
		this.lMatch = set;
	}

	// public void setlMatch(String lMatch) {
	// if (!lMatchMappingStrToInt.containsKey(lMatch)) {
	// lMatchMappingStrToInt.put(lMatch, ++lMatchNowNum);
	// lMatchMappingIntToStr.put(lMatchNowNum, lMatch);
	// }
	// this.lMatch.add(lMatchMappingStrToInt.get(lMatch));
	// }
	public void setlMatch(String lMatch) {
		this.lMatch.add(lMatch);
	}

	// public HashSet<String> getrMatch() {
	// rebackSet.clear();
	// for(int i : this.rMatch) {
	// rebackSet.add(rMatchMappingIntToStr.get(i));
	// }
	// return rebackSet;
	// }
	public HashSet<String> getrMatch() {
		return this.rMatch;
	}

	// public void setrMatch(HashSet<String> set) {
	// for(String s : set) {
	// if (rMatchMappingStrToInt.containsKey(s)) {
	// rMatchMappingStrToInt.put(s, ++rMatchNowNum);
	// rMatchMappingIntToStr.put(rMatchNowNum, s);
	// }
	// this.rMatch.add(rMatchMappingStrToInt.get(s));
	// }
	// }
	public void setrMatch(HashSet<String> set) {
		this.rMatch = set;
	}

	// public void setrMatch(String rMatch) {
	// if (!rMatchMappingStrToInt.containsKey(rMatch)) {
	// rMatchMappingStrToInt.put(rMatch, ++rMatchNowNum);
	// rMatchMappingIntToStr.put(rMatchNowNum, rMatch);
	// }
	// this.rMatch.add(rMatchMappingStrToInt.get(rMatch));
	// }
	public void setrMatch(String rMatch) {
		this.rMatch.add(rMatch);
	}

	public HashSet<String> getCutBy() {
		return cutBy;
	}

	public void setCutBy(HashSet<String> set) {
		this.cutBy = set;
	}

	public void setCutBy(String cutBy) {
		HashSet<String> set = this.cutBy;
		set.add(cutBy);
		this.cutBy = set;
	}

	public float getMinAggregate() {
		return minAggregate;
	}

	public void setMinAggregate(float minAggregate) {
		this.minAggregate = minAggregate;
	}

	/**
	 * 注： 这里需要覆盖而不是重载 形参必须为Object
	 */
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Cell)) {
			return false;
		}
		if (this.hashCode() == obj.hashCode())
			return true;
		return false;
	}

	public int hashCode() {
		return name.hashCode();
	}
}