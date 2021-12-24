import java.io.*;
import java.util.*;
/**
 * Compresses files using created binary tree class "CharBinTree"
 *
 * @author Victor Sanni, Fall 2021
 *
 */
public class Compression {
    public static BufferedReader page;
    public String pageItems;
    PriorityQueue<CharBinTree> treeQueue = new PriorityQueue<CharBinTree>(Comparator.comparingInt((CharBinTree t) -> t.frequency));
    public CharBinTree codeTree = new CharBinTree(null, null);  // tree holding character encoding

    public Compression(BufferedReader page) throws IOException {
        Compression.page = page;  // file to read
        int value;
        pageItems = "";

        // reads to the end of the stream
        while((value = page.read()) != -1) {

            // converts int to character
            char c = (char) value;
            pageItems += c;
        }

    }

    public Map<String, Integer> freqTable(){  
        String[] allWords = pageItems.split("");


        // Declare new Map to hold count of each word
        Map<String,Integer> wordCounts = new TreeMap<String,Integer>(); // word -> count

        // Loop over all the words split out of the string, adding to map or incrementing count
        for (String s: allWords) {

            // Check to see if we have seen this word before, update wordCounts appropriately
            if (wordCounts.containsKey(s)) {
                // Have seen this word before, increment the count
                wordCounts.put(s, wordCounts.get(s)+1);
            }
            else {
                // Have not seen this word before, add the new word
                wordCounts.put(s, 1);
            }
        }
        return wordCounts;
    }

    
    public void charQueue(){ // add binary tree per character to priority queue

        //Initialize priority tree, set priority

        // Then create set of keys for iteration purposes
        Set<String> mapSet = freqTable().keySet();
        for (String key : mapSet){
            treeQueue.add(new CharBinTree(key, freqTable().get(key)));  // add to priority queue
        }

    }

    public void createTree(){ //Create desired tree for encoding
        charQueue();
        while (treeQueue.size() != 1){  //iterate until only one tree left in priority queue
            CharBinTree t1 = treeQueue.remove();

            CharBinTree t2 = treeQueue.remove();
            CharBinTree mainTree = new CharBinTree(t1.getFrequency() + t2.getFrequency(), t1, t2);
            treeQueue.add(mainTree);
        }

    }

    public Map<String, String> makePath(){
        Map<String, String> charPath = new TreeMap<>();
        String currPath = "";
        CharBinTree mainTree = treeQueue.remove();
        codeTree = mainTree;

        pathHelper(charPath, mainTree, currPath);
        return charPath;
    }

    public void pathHelper(Map<String, String> charPath, CharBinTree mainTree, String currPath){ //Post-order traversal
        if (mainTree.isLeaf()){
            charPath.put(mainTree.charData, currPath);
        }
        if (mainTree.hasLeft()){
            pathHelper(charPath, mainTree.getLeft(), currPath + '0');
        }
        if (mainTree.hasRight()){
            pathHelper(charPath, mainTree.getRight(), currPath + '1' );
        }
    }

    public void compressFile (BufferedBitWriter bitOut, Map<String, String> path) throws IOException {
        for (int i = 0; i < pageItems.length(); i++){
            char a = pageItems.charAt(i);
            String val = (path.get(Character.toString(a)));  //Get each character's encoding from the map
            for (int j = 0; j < val.length(); j++){
                char num = val.charAt(j);
                //Process char
                bitOut.writeBit(num == '1'); // true or false if 0 or 1
            }
        }
        bitOut.close();
    }

    public void decompress (BufferedBitReader bitIn, BufferedWriter finalText, CharBinTree mainTree) throws IOException {

        while (bitIn.hasNext()) {
            boolean bit = bitIn.readBit();
            // do something with bit
            if (mainTree.isLeaf()) {
                finalText.write(mainTree.charData);  //write character to file
                mainTree = codeTree; //return to root
            }

            if (!bit && !mainTree.isLeaf()) {
                mainTree = mainTree.getLeft();
            }

            if (bit && !mainTree.isLeaf()) {
                mainTree = mainTree.getRight();
            }
        }
        if (pageItems != "") finalText.write(pageItems.charAt(pageItems.length() - 1));  // Write the last character directly, as long as we do not have an empty file

       bitIn.close(); finalText.close();
    }

    public static void main(String[] args) throws Exception {

        // test files are in inputs/. To test with other text files, simply change the names of the files you want to use in the code
        // or better still, you could build a GUI/app where a user can directly plug in files and have them compressed
        BufferedReader input = new BufferedReader(new FileReader("inputs/test.txt"));
        BufferedBitWriter bitOutput = new BufferedBitWriter("inputs/test_compressed.txt");
        BufferedWriter output = new BufferedWriter(new FileWriter("inputs/test_decompressed.txt"));


        Compression compress = new Compression(input);

        compress.createTree();
        Map<String, String> path = compress.makePath(); // Code map of characters with their paths

        compress.compressFile(bitOutput, path);
        System.out.println(path);

        try{
            BufferedBitReader bitInput = new BufferedBitReader("inputs/test_compressed.txt");
            compress.decompress(bitInput, output, compress.codeTree);
            bitInput.close();
        }
        catch (EOFException e) {
            System.out.println("End of file");
        }
        finally{  //Close all files, even if exception occurs
            input.close();
            output.close();
            bitOutput.close();
        }
    }
}
