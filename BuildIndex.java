import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class BuildIndex {
    public static void main(String[] args) {
        String filePath = args[0];
        String content;
        String[] fileName;
        Indexer idx = new Indexer();
        
    try {
        content = new String(Files.readAllBytes(Paths.get(filePath)));
        File file = new File(filePath);

        idx.setContent(content);
        System.out.println("Start------------");
        //System.out.println(idx.getContent());
        System.out.println("End------------");
        fileName = file.getName().split("\\.");
        FileOutputStream fos = new FileOutputStream(fileName[0]);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        //System.out.println(idx.getSize());
        oos.writeObject(idx);
        
        oos.close();
        fos.close();
    } catch (IOException e) {
        e.printStackTrace();	
    }
    }
}