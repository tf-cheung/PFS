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
            try {
                if (parts.length == 2 && parts[0].equals("open")) {
                    dbFile = parts[1] + ".db0";
                    ApplicationContext.setDbFileName(dbFile);
                    fileCreator = new FileCreator();
                    file = fileCreator.openFile(dbFile);
                    blockWriter = new BlockWriter(file, blockManager);
                } else if (parts.length == 2 && parts[0].equals("put")) {
                    csvFileName = parts[1];

                    FCBManager fcbManager = new FCBManager();
                    FileControlBlock fcb = fcbManager.findFCBByFileName(file,csvFileName);

                    if(fcb == null){
                        ApplicationContext.setCsvFileName(csvFileName);
                        blockManager = new BlockManager(file);
                        blockWriter = new BlockWriter(file, blockManager);
                        CSVReader reader = new CSVReader(blockWriter);

                        File csvFile = new File(csvFileName);
                        if (csvFile.exists()) {


                            MetadataHandler metadataHandler = new MetadataHandler(file);
                            metadataHandler.readBitmapFromMetadata();

                            reader.readAndWriteCSV(file, csvFileName);

                            blockWriter.writeBitmapToHeader();
                            indexManager.writeIndexToFile(file, blockManager, blockWriter.getIndexTree());
                            List<FileControlBlock> blocklist = fcbManager.readFCBListFromMetadata(file);
                            FileControlBlock tempFCB = null;

                            for (FileControlBlock block : blocklist) {
                                if (block.getFileName().equals(ApplicationContext.getCsvFileName())) {
                                    tempFCB = block;
                                    System.out.println("tempFCB: " + tempFCB.getFileName());
                                    break;
                                }
                            }

                            if (tempFCB != null) {
                                System.out.println("========================FCB info===============================");
                                System.out.println("File name: " + tempFCB.getFileName());
                                System.out.println("Index start: " + tempFCB.getIndexStartPosition());
                                System.out.println("Index end: " + tempFCB.getIndexEndPosition());
                                System.out.println("File size: " + tempFCB.getFileSize());
                                System.out.println("Block number: " + tempFCB.getUsedBlocks());
                                System.out.println("Start block: " + tempFCB.getStartBlock());
                            }
                        }else {
                            System.out.println("The specified file does not exist: " + csvFileName);
                        }
                    } else {
                        System.out.println("File already exists: " + csvFileName);
                    }
                } else if (parts.length == 2 && parts[0].equals("find")) {
                    String[] findParts = parts[1].split("\\.");
                    if (findParts.length == 2) {
                        String fileName = findParts[0] + ".csv";
                        int id = Integer.parseInt(findParts[1]);
                        ApplicationContext.setCsvFileName(fileName);

                        FCBManager fcbManager = new FCBManager();
                        FileControlBlock fcb = fcbManager.findFCBByFileName(file,fileName);

                        if(fcb == null){
                            System.out.println("File not found: " + fileName);
                        }else {
                            BTreeIndex result = indexManager.readIndexFromFile(file, ApplicationContext.getCsvFileName());
                            System.out.println("found data: " + blockWriter.readData(result.get(id), id));
                        }
                    }else {
                        System.out.println("Invalid format. Please use the format: find filename.id");
                    }
                }else if (parts.length == 2 && parts[0].equals("download")) {
                    String fileName = parts[1];
                    ApplicationContext.setCsvFileName(fileName);

                    FCBManager fcbManager = new FCBManager();
                    FileControlBlock fcb = fcbManager.findFCBByFileName(file, fileName);

                    if (fcb == null) {
                        System.out.println("File not found: " + fileName);
                    } else {
                        String outputFileName = fileName.replace(".csv", "_output.csv");

                        try (PrintWriter writer = new PrintWriter(new FileWriter(outputFileName))) {
                            BTreeIndex index = indexManager.readIndexFromFile(file, fileName);
                            int id = 1;
                            String data;

                            while (true) {
                                int blockId = index.get(id);
                                if (blockId == -1) {
                                    // Node not found, break the loop
                                    break;
                                }

                                try {
                                    data = blockWriter.readData(blockId, id);
                                    if (data != null) {
                                        writer.println(data);
                                    }
                                } catch (IllegalArgumentException e) {
                                    // Invalid blockId, skip this block
                                    System.out.println("Skipping invalid block: " + e.getMessage());
                                }
                                id++;
                            }

                            System.out.println("Data downloaded successfully. Output file: " + outputFileName);
                        } catch (IOException e) {
                            System.out.println("An error occurred while writing the output file: " + e.getMessage());
                        }
                    }

                }else if (parts[0].equalsIgnoreCase("exit")) {
                    if (file != null) {
                        file.close();
                    }
                    exit = true;
                }else{
                    throw new InvalidCommandException("Invalid command. Supported commands: open, put, find, exit");

                }
            } catch (InvalidCommandException  e) {
                System.err.println(e.getMessage());
            } catch (FileNotFoundException e) {
                System.err.println("The specified file does not exist: " + csvFileName);
            }
        }
    }
    public static class InvalidCommandException extends Exception {
        public InvalidCommandException(String message) {
            super(message);
        }
    }
}
