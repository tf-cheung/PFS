
import index.BTreeIndex;
import io.BlockWriter;
import io.CSVReader;
import io.FileCreator;
import manager.BlockManager;
import manager.FCBManager;
import manager.IndexManager;
import metadata.MetadataHandler;
import model.FileControlBlock;
import utils.ApplicationContext;
import utils.Tools;


import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;


public class Main {
    public static void main(String[] args) throws IOException {
        String csvFileName = "";
        FileCreator fileCreator;
        RandomAccessFile file = null;
        String dbFile = "";
        boolean exit = false;
        Scanner scanner = new Scanner(System.in);

//
//        while (!exit) {
//            System.out.print("Enter command: ");
//            String command = scanner.nextLine();
//            String[] parts = command.split(" ");
//
//            if (parts.length == 2 && parts[0].equals("open")) {
//                dbFile = parts[1];
//                fileCreator = new FileCreator(dbFile);
//                file = fileCreator.createFile();
//            }
//
//            if (parts.length == 2 && parts[0].equals("put")) {
//                csvFile = "src/" + parts[1];
//                BlockWriter blockWriter = new BlockWriter(file, blockManager);
//                CSVReader reader = new CSVReader(csvFile, blockWriter);
////                blockWriter.writeHeadBlock(parts[1], "data file", 1, "index.txt");
//                reader.readAndWriteCSV();
////                blockWriter.printBlockUsage();
//                blockWriter.writeBitmapToHeader();
//                System.out.println(blockWriter.readMovieData(2,6));
//                reader.close();
//
//            }
//
//            if (parts[0].equalsIgnoreCase("exit")) {
//                if (file != null) {
//                    file.close();
//                }
//                exit = true;
//            }
//
////            System.out.println(blockWriter.readMovieData(0,1).getTitle());
//
//
//            System.out.println("Data written successfully.");
//        }


        dbFile = "test.db0";
        IndexManager indexManager = new IndexManager();
        fileCreator = new FileCreator();
        file = fileCreator.openFile(dbFile);
        System.out.println("************************File 1*********************************");
        csvFileName = "movies.csv";
        FCBManager fcbManager = new FCBManager();
        ApplicationContext.setCsvFileName(csvFileName);

        ApplicationContext.setDbFileName(dbFile);
        BlockManager blockManager = new BlockManager(file);
        BlockWriter blockWriter = new BlockWriter(file, blockManager);
        CSVReader reader = new CSVReader(blockWriter);

        MetadataHandler metadataHandler = new MetadataHandler(file);
        metadataHandler.readBitmapFromMetadata();
        System.out.println(("Total blocks: "+metadataHandler.readTotalBlock()));
//                blockWriter.writeHeadBlock(parts[1], "data file", 1, "index.txt");
        reader.readAndWriteCSV(file,csvFileName);
        System.out.println("fcblist size: " + fcbManager.readFCBListFromMetadata(file).size());
        System.out.println("fcbname: "+ fcbManager.readFCBListFromMetadata(file).get(0).getFileName());

        blockWriter.writeBitmapToHeader();
        indexManager.writeIndexToFile(file,blockManager, blockWriter.getIndexTree());


        List<FileControlBlock> blocklist = fcbManager.readFCBListFromMetadata(file);

        FileControlBlock tempFCB=null;

        for (FileControlBlock block : blocklist) {
            System.out.println("block1: " + block.getFileName());
        }

        // print blocklist
        for (FileControlBlock block : blocklist) {
            if(block.getFileName().equals(ApplicationContext.getCsvFileName())){
                tempFCB = block;
                System.out.println("tempFCB: " + tempFCB.getFileName() );
                break;
            }
        }
////
        System.out.println("========================FCB info===============================");
        System.out.println("File name: "+tempFCB.getFileName());
        System.out.println("Index start: "+tempFCB.getIndexStartPosition());
        System.out.println("Index end: "+tempFCB.getIndexEndPosition());
        System.out.println("File size: "+tempFCB.getFileSize());
        System.out.println("Block number: "+tempFCB.getUsedBlocks());
        System.out.println("Start block: "+tempFCB.getStartBlock());
//        System.out.println("========================Search data============================");

//            System.out.println(result.toString());



        System.out.println("=======================Search result===========================");
        BTreeIndex result = indexManager.readIndexFromFile(file, ApplicationContext.getCsvFileName());
        System.out.println("Block #" + result.get(22));
        System.out.println("found movie data: " + blockWriter.readData(result.get(22),22));

        metadataHandler = new MetadataHandler(file);
//        Tools.printBitmap(metadataHandler.readBitmapFromMetadata());
//        System.out.println(metadataHandler.readTotalBlock());

//        blockWriter.printIndexTree();
//        blockWriter.readIndexFromFile();
//                blockWriter.printBlockUsage();
//        String string = "Fairfax Harrison (March 13, 1869 – February 2, 1938) was an American lawyer and businessman. He became a lawyer for the Southern Railway Company in 1896, and by 1906 he was the company's vice-president of finance. In 1913 he was elected president of Southern; under his leadership, the company expanded to an 8,000-mile (13,000 km) network across 13 states. Following the United States's entry into World War I, the federal government took control of the railroads, running them through the United States Railroad Administration, on which Harrison served. After the war, Harrison worked to improve the railroad's public relations, upgrade the locomotive stock by introducing more powerful engines, increase the company's amount of railroad track and extend the area serviced by the railway. Harrison struggled to keep the railroad afloat during the Great Depression, but by 1936 Southern was once again profitable. Harrison retired in 1937 and died three months later. (Full article...)";
//        byte [] data = string.getBytes();
//        blockWriter.printBlockUsage();

//        System.out.println(blockWriter.readMovieData(6,29));
//        BlockManager blockManager1 = new BlockManager();
//        Vector<Boolean> vector = blockManager1.getBitmap(file);
//
//        for (int i = 0; i < vector.size(); i++) {
//            boolean value = vector.get(i);
//            System.out.println("Block " + (i) + ": " + value);
//        }
//


//        System.out.println("Bitmap offset: " + Constants.BITMAP_OFFSET);
        System.out.println("");
        System.out.println("");
        System.out.println("");

        System.out.println("************************File 2*********************************");
//
        csvFileName = "ratings.csv"  ;
        ApplicationContext.setCsvFileName(csvFileName);
        ApplicationContext.setDbFileName(dbFile);
        fcbManager = new FCBManager();
        blockManager = new BlockManager(file);
        blockWriter = new BlockWriter(file, blockManager);
        reader = new CSVReader(blockWriter);
        reader.readAndWriteCSV(file,csvFileName);
        System.out.println("fcblist size: " + fcbManager.readFCBListFromMetadata(file).size());
        System.out.println("fcb Name: "+ fcbManager.readFCBListFromMetadata(file).get(0).getFileName());

        blockWriter.writeBitmapToHeader();
        indexManager = new IndexManager();
        indexManager.writeIndexToFile(file,blockManager, blockWriter.getIndexTree());

        blocklist = fcbManager.readFCBListFromMetadata(file);

        tempFCB=null;

        for (FileControlBlock block : blocklist) {
            System.out.println("block1: " + block.getFileName());
        }

        // print blocklist
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

        System.out.println("=======================Search result： rating ===========================");
        result = indexManager.readIndexFromFile(file, ApplicationContext.getCsvFileName());
        System.out.println("Block #" + result.get(11));
        System.out.println("found movie data: " + blockWriter.readData(result.get(11),11));



        System.out.println("=======================Search result: movie===========================");

        ApplicationContext.setCsvFileName("movies.csv");
        result = indexManager.readIndexFromFile(file, ApplicationContext.getCsvFileName());
        System.out.println("Block #" + result.get(22));
        System.out.println("found movie data: " + blockWriter.readData(result.get(22),22));



//
//
//
//
//        indexManager1.writeIndexToFile(file,blockManager, blockWriter.getIndexTree());
//
//
//
//
//        blocklist = fcbManager.readFCBListFromMetadata(file);
//
//         tempFCB=null;
//
//        for (FileControlBlock block : blocklist) {
//            System.out.println("block1: " + block.getFileName());
//        }
//
//        // print blocklist
//        for (FileControlBlock block : blocklist) {
//            if(block.getFileName().equals(ApplicationContext.getCsvFileName())){
//                tempFCB = block;
//                System.out.println("tempFCB: " + tempFCB.getFileName() );
//                break;
//            }
//        }
//
//
//        System.out.println("========================FCB info===============================");
//        System.out.println("File name: "+tempFCB.getFileName());
//        System.out.println("Index start: "+tempFCB.getIndexStartPosition());
//        System.out.println("Index end: "+tempFCB.getIndexEndPosition());
//        System.out.println("File size: "+tempFCB.getFileSize());
//        System.out.println("Block number: "+tempFCB.getUsedBlocks());
//        System.out.println("Start block: "+tempFCB.getStartBlock());
//        System.out.println("========================Search data============================");
//        result = indexManager1.readIndexFromFile(file, ApplicationContext.getCsvFileName());
//
//        System.out.println("found movie data: " + blockWriter.readData(result.get(1),1));
//
//        csvFileName = "movie.csv"  ;
//        ApplicationContext.setCsvFileName(csvFileName);
//
//        blocklist = fcbManager.readFCBListFromMetadata(file);
//        tempFCB=null;
//
//
//        for (FileControlBlock block : blocklist) {
//            if(block.getFileName().equals(ApplicationContext.getCsvFileName())){
//                tempFCB = block;
//                System.out.println("tempFCB: " + tempFCB.getFileName() );
//                break;
//            }
//        }
//
//        indexManager = new IndexManager();
//            result = indexManager.readIndexFromFile(file, ApplicationContext.getCsvFileName());
//            System.out.println("Block #" + result.get(22));
//
//            System.out.println("found movie data: " + blockWriter.readData(result.get(22),22));
        //--------

//
//        tempFCB=null;
//        // print blocklist
//
//        blocklist = blockWriter.readFCBListFromMetadata();
//
//        for (FileControlBlock block : blocklist) {
//            System.out.println("block: " + block.getFileName());
//        }
//
//        for (FileControlBlock block : blocklist) {
//            if(block.getFileName().equals("ratings.csv")){
//                tempFCB = block;
//                break;
//            }
//        }
//        System.out.println("========================FCB info===============================");
//        System.out.println("File name: "+tempFCB.getFileName());
//        System.out.println("Index start: "+tempFCB.getIndexStartPosition());
//        System.out.println("Index end: "+tempFCB.getIndexEndPosition());
//        System.out.println("File size: "+tempFCB.getFileSize());
//        System.out.println("Block number: "+tempFCB.getUsedBlocks());
//        System.out.println("Start block: "+tempFCB.getStartBlock());
//        System.out.println("========================Search data============================");
//        indexManager = new IndexManager();
//        BTreeIndex result2 = indexManager.readIndexFromFile(file,tempFCB.getIndexStartPosition(), tempFCB.getIndexEndPosition());
//        System.out.println("Block #" + result2.get(21));
//        System.out.println("found movie rating: " + blockWriter.readData(result2.get(21),21));
//
//        System.out.println("=============Metadata info=============");
//        file.seek(Constants.CREATE_DATE_OFFSET);
//        long createDateMillis = file.readLong();
//        System.out.println("Create Date: " + new Date(createDateMillis));
//
//
//        file.seek(Constants.DATABASE_NAME_OFFSET);
//
//        byte[] databaseNameBytes = new byte[Constants.DATABASE_NAME_SIZE];
//        int bytesRead = file.read(databaseNameBytes);
//        String databaseName = new String(databaseNameBytes).trim();
//        System.out.println("Database name: " + databaseName);



        reader.close();
    }


}
