package com.secneo.test;


enum NodeKind {
	LN, BN
};

/**
 * Trie结点
 */
class TrieNode {
	char key;
	TrieNode[] points = null;
	NodeKind kind = null;
}

/**
 * Trie叶子结点
 */
class LeafNode extends TrieNode {
	LeafNode(char k) {
		super.key = k;
		super.kind = NodeKind.LN;
	}
}

/**
 * Trie内部结点
 */
class BranchNode extends TrieNode {
	BranchNode(char k) {
		super.key = k;
		super.kind = NodeKind.BN;
		super.points = new TrieNode[27];
	}
}

/**
 * 标准Trie树的实现
 */
public class ParticipleTest {
	private TrieNode root = new BranchNode(' ');

	/**
	 * 向Tire中插入字符串
	 */
	public void insert(String word) {
		// System.out.println("插入字符串："+word);
		// 从根结点出发
		TrieNode curNode = root;
		// 为了满足字符串集合X中不存在一个串是另外一个串的前缀
		word = word + "$";
		// 获取每个字符
		char[] chars = word.toCharArray();
		// 插入
		for (int i = 0; i < chars.length; i++) {
			 System.out.println("   插入"+chars[i]);
			if (chars[i] == '$') {
				curNode.points[26] = new LeafNode('$');
				 System.out.println("   插入完毕,使当前结点"+curNode.key+"的第26孩子指针指向字符：$");
			} else {
				int pSize = chars[i] - 'a';
				if (curNode.points[pSize] == null) {
					curNode.points[pSize] = new BranchNode(chars[i]);
					 System.out.println("   使当前结点"+curNode.key+"的第"+pSize+"孩子指针指向字符: "+chars[i]);
					curNode = curNode.points[pSize];
				} else {
					 System.out.println("   不插入，找到当前结点"+curNode.key+"的第"+pSize+"孩子指针已经指向字符: "+chars[i]);
					curNode = curNode.points[pSize];
				}
			}
		}
	}

	/**
	 * Trie的字符串全字匹配
	 */
	public boolean fullMatch(String word) {
		 System.out.print("查找字符串："+word+"/n查找路径：");
		// 从根结点出发
		TrieNode curNode = root;
		// 获取每个字符
		char[] chars = word.toCharArray();

		for (int i = 0; i < chars.length; i++) {
			if (curNode.key == '$') {
				System.out.println('&');
				 System.out.println("成功...");
				return true;
			} else {
				System.out.print(chars[i] + " -> ");
				int pSize = chars[i] - 'a';
				if (curNode.points[pSize] == null) {
					 System.out.println("失败!");
					return false;
				} else {
					curNode = curNode.points[pSize];
				}
			}
		}
		 System.out.println("失败!");
		return false;
	}

	/**
	 * 先根遍历Tire树
	 */
	private void preRootTraverse(TrieNode curNode) {
		if (curNode != null) {
			System.out.print(curNode.key + " ");
			if (curNode.kind == NodeKind.BN)
				for (TrieNode childNode : curNode.points)
					preRootTraverse(childNode);
		}

	}

	/**
	 * 得到Trie根结点
	 */
	public TrieNode getRoot() {
		return this.root;
	}

	/**
	 * 测试
	 */
	public static void main(String[] args) {
		ParticipleTest trie = new ParticipleTest();
		trie.insert("bear");
		trie.insert("bell");
		trie.insert("bid");
		trie.insert("bull");
		trie.insert("buy");
		trie.insert("sell");
		trie.insert("stock");
		trie.insert("stop");
		trie.preRootTraverse(trie.getRoot());
		trie.fullMatch("bear");
	}
}