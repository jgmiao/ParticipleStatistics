package com.secneo.participle.weight;

import java.util.HashSet;

import com.secneo.participle.domain.Cell;
import com.secneo.participle.split.SplitWordTool2;
import com.secneo.participle.util.MyDict;
import com.secneo.participle.util.MyUtils;
import com.secneo.participle.util.StringUtils;

public class WeightCountPort {
	private final static float WEIGHT_SUB = 0.2f;
	private final static float WEIGHT_WHOLE = 1 - WEIGHT_SUB;
	private final static float WEIGHT_L_MATCH = 1.3f;
	private final static float WEIGHT_R_MATCH = 1.4f;

	/**
	 * 计算字串的权值
	 * @param str 可以进过分词后的 确定在库里有的字串 如：“壁纸”
	 */
	private static int weightCount(String str) {
		Cell wegCell = MyUtils.getCellAll(str);
		if (null == wegCell) {
			return 0;
		}
		float wegCountSub = wegCell.getCountSub() * WEIGHT_SUB;
		float wegCountWhole = wegCell.getCountWhole() * WEIGHT_WHOLE;
		float wegLMatch = wegCell.getlMatch().size() * WEIGHT_L_MATCH;
		float wegRMatch = wegCell.getrMatch().size() * WEIGHT_R_MATCH;

		// 返回(所有权值[4个]) * 状态权值
		return (int) (wegCountSub + wegCountWhole + wegLMatch + wegRMatch);
	}

	/**
	 * [分词工具对外接口] 获得字串的权值
	 */
	public static int getWeight(String str) {
		if (!StringUtils.isNotEmpty(str)) {
			return 0;
		}
		int maxWeightCount = 0;
		int state;
		float wegState = 0;
		boolean noiseFlag = false;
		HashSet<Cell> cellSet = SplitWordTool2.splitWordPort(str);
		for (Cell hCell : cellSet) {
			state = hCell.getState();
			//对词性的判断
			if (state == MyDict.NOISE_STATE) {
				wegState = 1f;
				noiseFlag = true;
			} else if (state == MyDict.DEFINITE_ALL_STATE) {
				wegState = 1f;
			} else if (state == MyDict.DEFINITE_STATE) {
				wegState = 0.8f;
			} else if (state == MyDict.MODIFY_STATE
					|| state == MyDict.MODIFY_GAME_STATE
					|| state == MyDict.MODIFY_HIGH_MATCH_STATE) {
				wegState = 0.4f;
			}
			//比较获取最大的权值[这里噪声词跟应用词只做标记未区分]
			int weightCount = (int) (weightCount(hCell.getName()) * wegState);
			if (maxWeightCount < weightCount) {
				maxWeightCount = weightCount;
			}
		}//for
		if (noiseFlag) { //如果存在噪声词  则由此标记将权值改为负数
			maxWeightCount = -maxWeightCount;
		}
		return maxWeightCount;
	}//getWeight
}