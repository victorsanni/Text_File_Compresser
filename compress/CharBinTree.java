import java.util.*;
import java.io.*;

/**
 * Generic binary tree, storing data of a parametric data in each node
 * @author Victor Sanni, Fall 2021
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Fall 2012
 * @author CBK, Spring 2016, minor updates to testing
 * @author Tim Pierson, Winter 2018, added code to manually build tree in main
 */

public class CharBinTree<E> {
	private CharBinTree<E> left, right;	// children; can be null
	String charData; Integer frequency;

	/**
	 * Constructs leaf node -- left and right are null
	 */
	public CharBinTree(String charData, Integer frequency) {
		this.charData = charData; this.left = null; this.right = null; this.frequency = frequency;
	}
	public CharBinTree(String charData) {
		this.charData = charData; this.left = null; this.right = null; this.frequency = null;
	}
	public CharBinTree(Integer frequency) {
		this.charData = null; this.left = null; this.right = null; this.frequency = frequency;
	}

	/**
	 * Constructs inner node
	 */
	public CharBinTree(String charData, Integer frequency, CharBinTree<E> left, CharBinTree<E> right) {
		this.charData = charData; this.frequency = frequency; this.left = left; this.right = right;
	}

	public CharBinTree(String charData, CharBinTree<E> left, CharBinTree<E> right) {
		this.charData = charData; this.frequency = null; this.left = left; this.right = right;
	}
	public CharBinTree(Integer frequency, CharBinTree<E> left, CharBinTree<E> right) {
		this.charData = null; this.frequency = frequency; this.left = left; this.right = right;
	}

	public void traverse() {
		System.out.println(charData + ":" + frequency);
		if (hasLeft()) left.traverse();
		if (hasRight()) right.traverse();
	}

	/**
	 * Is it an inner node?
	 */
	public boolean isInner() {
		return left != null || right != null;
	}

	/**
	 * Is it a leaf node?
	 */
	public boolean isLeaf() {
		return left == null && right == null;
	}

	/**
	 * Does it have a left child?
	 */
	public boolean hasLeft() {
		return left != null;
	}

	/**
	 * Does it have a right child?
	 */
	public boolean hasRight() {
		return right != null;
	}

	public CharBinTree<E> getLeft() {
		return left;
	}

	public CharBinTree<E> getRight() {
		return right;
	}

	public String getCharData() {
		return charData;
	}

	public Integer getFrequency() { return frequency;	}

	/**
	 * Number of nodes (inner and leaf) in tree
	 */
	public int size() {
		int num = 1;
		if (hasLeft()) num += left.size();
		if (hasRight()) num += right.size();
		return num;
	}

	/**
	 * Longest length to a leaf node from here
	 */
	public int height() {
		if (isLeaf()) return 0;
		int h = 0;
		if (hasLeft()) h = Math.max(h, left.height());
		if (hasRight()) h = Math.max(h, right.height());
		return h+1;						// inner: one higher than highest child
	}	

	/**
	 * Same structure and data?
	 */
	public boolean equalsTree(CharBinTree<E> t2) {
		if (hasLeft() != t2.hasLeft() || hasRight() != t2.hasRight()) return false;
		if (!charData.equals(t2.charData)) return false;
		if (!frequency.equals(t2.frequency)) return false;
		if (hasLeft() && !left.equalsTree(t2.left)) return false;
		if (hasRight() && !right.equalsTree(t2.right)) return false;
		return true;
	}

	/**
	 * Leaves, in order from left to right
	 */
	public ArrayList<String> fringe() {
		ArrayList<String> f = new ArrayList<String>();
		addToFringe(f);
		return f;
	}

	/**
	 * Helper for fringe, adding fringe data to the list
	 */
	private void addToFringe(ArrayList<String> fringe) {
		if (isLeaf()) {
			fringe.add(charData);
		}
		else {
			if (hasLeft()) left.addToFringe(fringe);
			if (hasRight()) right.addToFringe(fringe);
		}
	}

	/**
	 * Returns a string representation of the tree
	 */
	public String toString() {
		return toStringHelper("");
	}

	/**
	 * Recursively constructs a String representation of the tree from this node, 
	 * starting with the given indentation and indenting further going down the tree
	 */
	public String toStringHelper(String indent) {
		String res = indent + charData + ":" + frequency + "\n";
		if (hasLeft()) res += left.toStringHelper(indent+"  ");
		if (hasRight()) res += right.toStringHelper(indent+"  ");
		return res;
	}

	/**
	 * Very simplistic binary tree parser based on Newick representation
	 * Assumes that each node is given a label; that becomes the data
	 * Any distance information (following the colon) is stripped
	 * <tree> = "(" <tree> "," <tree> ")" <label> [":"<dist>]
	 *        | <label> [":"<dist>]
	 * No effort at all to handle malformed trees or those not following these strict requirements
	 */
	public static CharBinTree<String> parseNewick(String s) {
		CharBinTree<String> t = parseNewick(new StringTokenizer(s, "(,)", true));
		// Get rid of the semicolon
		t.charData = t.charData.substring(0,t.charData.length()-1);
		return t;
	}

	/**
	 * Does the real work of parsing, now given a tokenizer for the string
	 */
	public static CharBinTree<String> parseNewick(StringTokenizer st) {
		String token = st.nextToken();
		if (token.equals("(")) {
			// Inner node
			CharBinTree<String> left = parseNewick(st);
			String comma = st.nextToken();
			CharBinTree<String> right = parseNewick(st);
			String close = st.nextToken();
			String label = st.nextToken();
			String[] pieces = label.split(":");
			return new CharBinTree<String>(pieces[0], left, right);
		}
		else {
			// Leaf
			String[] pieces = token.split(":");
			return new CharBinTree<String>(pieces[0]);
		}
	}

	/**
	 * Slurps the entire file into a single String, and returns it
	 */
	public static String readIntoString(String filename) throws IOException {
		StringBuffer buff = new StringBuffer();
		BufferedReader in = new BufferedReader(new FileReader(filename));
		String line;
		while ((line = in.readLine()) != null) buff.append(line);
		in.close();
		return buff.toString();
	}

	public CharBinTree<E> copyToDepth(int d){
		CharBinTree<E> shallow = new CharBinTree<>(charData, left, right);
		copyHelper(shallow, d);
		return shallow;
	}
	public void copyHelper(CharBinTree<E> copy, int d){

		if (copy.height() == d && !isLeaf()){
			this.left = null; this.right = null;
		}
		else{
			if (hasLeft()) left.copyHelper(copy.left, d);
			if (hasRight()) right.copyHelper(copy.right, d);
		}
	}

	/**
	 * Some tree testing
	 */
	public static void main(String[] args) throws IOException {
		//manually build a tree
		CharBinTree<String> root = new CharBinTree<String>("G");
		root.left = new CharBinTree<String>("B");
		root.right = new CharBinTree<String>("F");
		CharBinTree<String>temp = root.left;
		temp.left = new CharBinTree<String>("A");
		temp.right = new CharBinTree<String>("C");
		temp = root.right;
		temp.left = new CharBinTree<String>("D");
		temp.right = new CharBinTree<String>("E");
		System.out.println(root);
		
		System.out.println("Fringe");
		System.out.println(root.fringe());
		System.out.println(root.copyToDepth(1));



		CharBinTree<String> t = new CharBinTree<String>("A");
		System.out.println(t.height());
		System.out.println(t.size());
		
		// Smaller trees
		CharBinTree<String> t1 = parseNewick("((a,b)c,(d,f)e)g;");
		System.out.println(t1);
		System.out.println("height:" + t1.height());
		System.out.println("size:" + t1.size());
		System.out.println("fringe:" + t1.fringe());
		t1.traverse();
		

		CharBinTree<String> t2 = parseNewick("((a,b)c,(d,e)f)g;");
		CharBinTree<String> t3 = parseNewick("((a,b)z,(d,e)f)g;");
		System.out.println("== " + t1.equalsTree(t2) + " " + t1.equalsTree(t3));

		// Tournament
		CharBinTree<String> tournament = parseNewick("(((b,c1)b,(c2,d)d)d,((h,p1)h,(p2,y)y)h)d;");
		System.out.println(tournament);
		
		// Tree of life
		String s = readIntoString("inputs/itol.txt");
		CharBinTree<String> itol = parseNewick(s);
		System.out.println(itol);
		System.out.println("height:" + itol.height());
		System.out.println("size:" + itol.size());
	}
}
