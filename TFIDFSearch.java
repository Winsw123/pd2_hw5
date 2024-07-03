import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class TrieNode implements Serializable {
    TrieNode[] children = new TrieNode[26];
    boolean isEndOfWord = false;
    int count;
    int docsCount;

    TrieNode() {
        isEndOfWord = false;
        count = 0;
        docsCount = 0;
    }
}

class TestTrie implements Serializable {
    TrieNode root = new TrieNode();
    int size;

    TestTrie() {
        size = 0;
    }

    public int size() {
        return size;
    }

    public void insert(String word) {
        TrieNode node = root;
        for (char c : word.toCharArray()) {
            if (node.children[c - 'a'] == null) {
                node.children[c - 'a'] = new TrieNode();
            }
            node = node.children[c - 'a'];
        }
        size++; 
        node.count++;
        node.isEndOfWord = true;
    }

    public boolean search(String word) {
        TrieNode node = root;
        for (char c : word.toCharArray()) {
            node = node.children[c - 'a'];
            if (node == null) {
                return false;
            }
        }
        return node.isEndOfWord;
    }

    public boolean updateDocsCount(String word, int count) {
        TrieNode node = root;
        for (char c : word.toCharArray()) {
            node = node.children[c - 'a'];
            if (node == null) {
                return false;
            }
        }
        node.docsCount = count;
        return node.isEndOfWord;
    }

    public int countOccurrences(String word) {
        TrieNode node = root;
        for (char ch : word.toCharArray()) {
            int index = ch - 'a';
            if (node.children[index] == null) {
                return 0;
            }
            node = node.children[index];
        }
        return node.isEndOfWord ? node.count : 0;
    }

    public int docsCountOccurrences(String word) {
        TrieNode node = root;
        for (char ch : word.toCharArray()) {
            int index = ch - 'a';
            if (node.children[index] == null) {
                return 0;
            }
            node = node.children[index];
        }
        return node.isEndOfWord ? node.docsCount : 0;
    }
}
    
public class TFIDFSearch {
    public static double tf(TestTrie doc, String term) {
        double number_term_in_doc = doc.countOccurrences(term);
        return number_term_in_doc / doc.size();
    }
    public static double idf(ArrayList<TestTrie> docList, TestTrie docs, String term) {
        double number_doc_contain_term = docs.docsCountOccurrences(term); 
        if(number_doc_contain_term == 0) {
            return 0;
        } else {
            return Math.log(docList.size() / number_doc_contain_term);
        }
    }
    
    public static double tfIdfCalculate(ArrayList<TestTrie> docList, TestTrie doc, TestTrie docs, String term) {
          return tf(doc, term) * idf(docList, docs, term);
    }

    public static ArrayList<TestTrie> SplitDocs(String content) {
        ArrayList<String> segments = new ArrayList<>();
        ArrayList<TestTrie> docs = new ArrayList<>();
        StringBuilder segment = new StringBuilder();
        String[] splitContent;
        String[] tempSegment;
        int targetLine = 0;
        
        splitContent = content.split("\n");
        //String[] subArray = Arrays.copyOfRange(splitContent, (Integer.parseInt(lineNum) * 5), (Integer.parseInt(lineNum) * 5) + 5);
        for(String line : splitContent) {     
            //System.out.println(line);  
            line = line.toLowerCase();
            line = line.replaceAll("[^a-zA-Z]"," ");
            line = line.replaceAll("\\s+"," ").trim();
            segment.append(line).append(" ");
            targetLine ++;

            if(targetLine == 5) {
                //segments.add(segment.toString());
                tempSegment = segment.toString().split("\\s+");
                TestTrie segmentTrie = new TestTrie();
                for(String iSegment : tempSegment) {
                    //System.out.print("Segment " + lineNum + ": " + "-------");
                    //System.out.println(iSegment);
                    segmentTrie.insert(iSegment);
                }
                docs.add(segmentTrie);
                segment.setLength(0);
                targetLine = 0;
                //System.out.println(segmentTrie.countOccurrences("t"));
            }
        }
        //System.out.println(segments.get(32));
        return docs;
    }

    public static Map.Entry<Integer, Double>[] Intersection(ArrayList<TestTrie> dataBase, String[] testcase, TestTrie cache, HashMap<Integer, Double> map) {
        int lineCount = 0;
        for(TestTrie line : dataBase) {
            double result = 0;
            boolean state = true;
            for(String j : testcase) {
                if(!line.search(j)) {
                    state = false;
                    break;
                } 
            }
            if(state) {
                for(String term : testcase) {
                    if(!cache.search(term)) {//check if there have cache record tf
                        cache.insert(term);//if no then create new record
                        int count = 0;
                        for(TestTrie k : dataBase) {
                            if(k.search(term)) {
                                count ++;
                            }
                        }
                        cache.updateDocsCount(term, count);//calculate the query word tf among the document
                        //System.out.println(indexTrie.docsCountOccurrences(i));
                    }
                    result = result + tfIdfCalculate(dataBase, line, cache, term);
                }
                //System.out.println(result);
                map.put(lineCount, result);
            }  
            lineCount++;
        }
        @SuppressWarnings("unchecked")
        Map.Entry<Integer, Double>[] entries = map.entrySet().toArray(new Map.Entry[0]);
        reverseQuickSort(entries, 0, entries.length - 1);
        return entries;
    }

    public static Map.Entry<Integer, Double>[] Union(ArrayList<TestTrie> dataBase, String[] testcase, TestTrie cache, HashMap<Integer, Double> map) {
        int lineCount = 0;
        for(TestTrie line : dataBase) {
            double result = 0;
            boolean state = false;
            for(String j : testcase) {
                if(line.search(j)) {
                    state = true;
                    break;
                } 
            }
            if(state) {
                for(String term : testcase) {
                    if(!cache.search(term)) {//check if there have cache record tf
                        cache.insert(term);//if no then create new record
                        int count = 0;
                        for(TestTrie k : dataBase) {
                            if(k.search(term)) {
                                count ++;
                            }
                        }
                        //System.out.println(term + " in all docs count : " + count);
                        cache.updateDocsCount(term, count);//calculate the query word tf among the document
                        //System.out.println(indexTrie.docsCountOccurrences(i));
                    }
                    result = result + tfIdfCalculate(dataBase, line, cache, term);
                }
                //System.out.println(result);
                map.put(lineCount, result);
            }  
            lineCount++;
        }
        @SuppressWarnings("unchecked")
        Map.Entry<Integer, Double>[] entries = map.entrySet().toArray(new Map.Entry[0]);
        reverseQuickSort(entries, 0, entries.length - 1);
        return entries;
    }

    public static  Map.Entry<Integer, Double>[] Exist(ArrayList<TestTrie> dataBase, String testcase, TestTrie cache, HashMap<Integer, Double> map) {
        int lineCount = 0;
        for(TestTrie line : dataBase) {
            double result = 0;
            boolean state = false;

            if(line.search(testcase)) {
                state = true;
            } 
            
            if(state) {
                    if(!cache.search(testcase)) {//check if there have cache record tf
                        cache.insert(testcase);//if no then create new record
                        int count = 0;
                        for(TestTrie k : dataBase) {
                            if(k.search(testcase)) {
                                count ++;
                            }
                        }
                        cache.updateDocsCount(testcase, count);//calculate the query word tf among the document
                        //System.out.println(indexTrie.docsCountOccurrences(i));
                    }
                    result = tfIdfCalculate(dataBase, line, cache, testcase);
                
                //System.out.println(result);
                map.put(lineCount, result);
            }

                lineCount++;
        }
        @SuppressWarnings("unchecked")
        Map.Entry<Integer, Double>[] entries = map.entrySet().toArray(new Map.Entry[0]);

        reverseQuickSort(entries, 0, entries.length - 1);
        return entries;
    }
    public static void reverseQuickSort(Map.Entry<Integer, Double>[] arr, int low, int high) {
        if (arr == null || arr.length == 0) {
            return;
        }
    
        if (low >= high) {
            return;
        }
    
        Map.Entry<Integer, Double> pivot = arr[low + (high - low) / 2];
    
        int i = low, j = high;
        while (i <= j) {
            while (compareEntries(arr[i], pivot) < 0) {
                i++;
            }
            while (compareEntries(arr[j], pivot) > 0) {
                j--;
            }
            if (i <= j) {
                Map.Entry<Integer, Double> temp = arr[i];
                arr[i] = arr[j];
                arr[j] = temp;
                i++;
                j--;
            }
        }
    
        if (low < j) {
            reverseQuickSort(arr, low, j);
        }

        if (high > i) {
            reverseQuickSort(arr, i, high);
        }
    }

    private static int compareEntries(Map.Entry<Integer, Double> e1, Map.Entry<Integer, Double> e2) {
        int cmp = e2.getValue().compareTo(e1.getValue()); 
        if (cmp != 0) {
            return cmp;
        }
        return e1.getKey().compareTo(e2.getKey()); 
    }

    public static void main(String[] args) {
        Map.Entry<Integer, Double>[] entries;
        ArrayList<TestTrie> dataBase = new ArrayList<>();
        ArrayList<String> output = new ArrayList<>();
        TestTrie cache = new TestTrie();
        String testFileName = args[0];
        String testcase = args[1];
        String[] temp;
        String tempOutput = "";
        int outputCount = 0;
        Pattern pattern = Pattern.compile("\\d");
        
        try {
            FileInputStream fis = new FileInputStream(testFileName);
            ObjectInputStream ois = new ObjectInputStream(fis);
            Indexer deserializedIdx = (Indexer) ois.readObject();
            ois.close();
            fis.close();
            //System.out.println(deserializedIdx.getContent());
            dataBase = SplitDocs(deserializedIdx.getContent());
            temp = Files.readString(Paths.get(testcase)).split("\n");
            for(String i : temp) {
                HashMap<Integer, Double> map = new HashMap<>();
                Matcher matcher = pattern.matcher(i);
                if(matcher.find()) {
                    i = i.trim();
                    outputCount = Integer.parseInt(i);
                } else if(i.contains("AND")) {
                    i = i.replaceAll("\\s+", "");
                    String[] searchAnd = i.split("AND");

                    entries = Intersection(dataBase, searchAnd, cache, map);

                    int count = 0;
                    for (Map.Entry<Integer, Double> entry : entries) {
                        if(count < outputCount) {
                        tempOutput = tempOutput + entry.getKey() + " ";
                        } else {
                            break;
                        }
                        count++;
                    }
                    while(count < outputCount) {
                        tempOutput = tempOutput + "-1" + " ";
                        count ++;
                    }
                    //System.out.println(tempOutput);
                    output.add(tempOutput);
                    tempOutput = "";
                
                } else if(i.contains("OR")) {

                    i = i.replaceAll("\\s+", "");
                    String[] searchAnd = i.split("OR");

                    entries = Union(dataBase, searchAnd, cache, map);

                    int count = 0;
                    for (Map.Entry<Integer, Double> entry : entries) {
                        if(count < outputCount) {
                        tempOutput = tempOutput + entry.getKey() + " ";
                        } else {
                            break;
                        }
                        count++;
                    }
                    while(count < outputCount) {
                        tempOutput = tempOutput + "-1" + " ";
                        count ++;
                    }
                    //System.out.println(tempOutput);
                    output.add(tempOutput);
                    tempOutput = "";
                    
                } else {
                    i = i.trim();

                    entries = Exist(dataBase, i, cache, map);
                    int count = 0;
                    for (Map.Entry<Integer, Double> entry : entries) {
                        if(count < outputCount) {
                        tempOutput = tempOutput + entry.getKey() + " ";
                        } else {
                            break;
                        }
                        count++;
                    }
                    while(count < outputCount) {
                        tempOutput = tempOutput + "-1" + " ";
                        count ++;
                    }
                    //System.out.println(tempOutput);
                    output.add(tempOutput);
                    tempOutput = "";
                    
                }
            }
            FileWriter writer = new FileWriter("output.txt");
            for(String i : output) {
                writer.write(i + "\n");
            }
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException c) {
            c.printStackTrace();
        }
        
    }
    
}