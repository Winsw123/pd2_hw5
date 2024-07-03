import java.io.Serializable;
import java.util.ArrayList;

public class Indexer implements Serializable {
	private static final long serialVersionUID = 1L;
	private String content;
    private ArrayList<TestTrie> trie;
    private transient int counter;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getCounter() {
        return counter;
    }

    public void setCounter(int counter) {
        this.counter = counter;
    }

    public ArrayList<TestTrie> getTrie() {
        return trie;
    }

    public void setTrie(ArrayList<TestTrie> trie) {
        this.trie = trie;
    }

}