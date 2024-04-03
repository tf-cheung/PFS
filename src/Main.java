import index.BTreeIndex;
import io.*;
import manager.*;
import metadata.MetadataHandler;
import model.FileControlBlock;
import utils.*;
import java.io.*;
import java.util.List;
import java.util.Scanner;
public class Main {
    public static void main(String[] args) throws IOException {
        String csvFileName = "";
        FileCreator fileCreator;
        String dbFile = "";
        Tools tools = new Tools();
        boolean exit = false;
        RandomAccessFile file = null;
        Scanner scanner = new Scanner(System.in);
        IndexManager indexManager = new IndexManager();
        BlockManager blockManager = new BlockManager();
        BlockWriter blockWriter = null;
        while (!exit) {
            System.out.print("Enter command: ");
            String command = scanner.nextLine();
            String[] parts = command.split(" ");

            if (parts.length == 2 && parts[0].equals("open")) {
                dbFile = parts[1] + ".db0";
                ApplicationContext.setDbFileName(dbFile);
                fileCreator = new FileCreator();
                file = fileCreator.openFile(dbFile);
                blockWriter = new BlockWriter(file, blockManager);


            }

            if (parts.length == 2 && parts[0].equals("put")) {
                csvFileName = parts[1];
                ApplicationContext.setCsvFileName(csvFileName);
                FCBManager fcbManager = new FCBManager();
                blockManager = new BlockManager(file);
                blockWriter = new BlockWriter(file, blockManager);
                CSVReader reader = new CSVReader(blockWriter);

                MetadataHandler metadataHandler = new MetadataHandler(file);
                metadataHandler.readBitmapFromMetadata();

                reader.readAndWriteCSV(file,csvFileName);

                blockWriter.writeBitmapToHeader();
                indexManager.writeIndexToFile(file,blockManager, blockWriter.getIndexTree());
                List<FileControlBlock> blocklist = fcbManager.readFCBListFromMetadata(file);
                FileControlBlock tempFCB=null;

                for (FileControlBlock block : blocklist) {
                    if(block.getFileName().equals(ApplicationContext.getCsvFileName())){
                        tempFCB = block;
                        System.out.println("tempFCB: " + tempFCB.getFileName() );
                        break;
                    }
                }

                System.out.println("========================FCB info===============================");
                System.out.println("File name: "+tempFCB.getFileName());
                System.out.println("Index start: "+tempFCB.getIndexStartPosition());
                System.out.println("Index end: "+tempFCB.getIndexEndPosition());
                System.out.println("File size: "+tempFCB.getFileSize());
                System.out.println("Block number: "+tempFCB.getUsedBlocks());
                System.out.println("Start block: "+tempFCB.getStartBlock());

            }

            if (parts.length == 2 && parts[0].equals("find")) {
                String[] findParts = parts[1].split("\\.");



                if (findParts.length == 2) {
                    String fileName = findParts[0] + ".csv";
                    int id = Integer.parseInt(findParts[1]);
                    ApplicationContext.setCsvFileName(fileName);
                    BTreeIndex result = indexManager.readIndexFromFile(file, ApplicationContext.getCsvFileName());
                    System.out.println("found data: " + blockWriter.readData(result.get(id),id));

                }


            }

            if (parts[0].equalsIgnoreCase("exit")) {
                if (file != null) {
                    file.close();
                }
                exit = true;
            }
        }



    }


}
