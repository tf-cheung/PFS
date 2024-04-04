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

        // Main loop
        while (!exit) {
            System.out.print("Enter command: ");
            String command = scanner.nextLine();
            String[] parts = command.split(" ");
            try {
                if (parts.length == 2 && parts[0].equals("open")) { // Process the open command
                    dbFile = parts[1] + ".db0"; // Append the file extension
                    ApplicationContext.setDbFileName(dbFile); // Set the database file name in the application context
                    fileCreator = new FileCreator(); // Create a new file creator
                    file = fileCreator.openFile(dbFile); // Open the file
                    blockWriter = new BlockWriter(file, blockManager); // Create a new block writer

                } else if (parts.length == 2 && parts[0].equals("put")) { // Process the put command
                    csvFileName = parts[1];
                    FCBManager fcbManager = new FCBManager();
                    FileControlBlock fcb = fcbManager.findFCBByFileName(file,csvFileName); // Find the FCB by file name

                    if(fcb == null){ // If the FCB does not exist
                        ApplicationContext.setCsvFileName(csvFileName);
                        blockManager = new BlockManager(file);
                        blockWriter = new BlockWriter(file, blockManager);
                        CSVReader reader = new CSVReader(blockWriter);

                        File csvFile = new File(csvFileName);
                        if (csvFile.exists()) { // If the CSV file exists

                            MetadataHandler metadataHandler = new MetadataHandler(file);
                            metadataHandler.readBitmapFromMetadata(); // Read the bitmap from the metadata

                            reader.readAndWriteCSV(file, csvFileName); // Read and write the CSV file

                            blockWriter.writeBitmapToHeader(); // Write the bitmap to the header
                            indexManager.writeIndexToFile(file, blockManager, blockWriter.getIndexTree()); // Write the index to the file
                            List<FileControlBlock> blocklist = fcbManager.readFCBListFromMetadata(file); // Read the FCB list from the metadata
                            FileControlBlock tempFCB = null;

                            for (FileControlBlock block : blocklist) {
                                if (block.getFileName().equals(ApplicationContext.getCsvFileName())) {
                                    tempFCB = block;
                                    break;
                                }
                            }

                            if (tempFCB != null) {
                                System.out.println("Import successfully: " + csvFileName);
                                System.out.println("File size: " + tempFCB.getFileSize()+ " bytes");
//                                System.out.println("Block number: " + tempFCB.getUsedBlocks());
//                                System.out.println("Start block: " + tempFCB.getStartBlock());
                            }
                        }else {
                            System.out.println("The specified file does not exist: " + csvFileName);
                        }
                    } else {
                        System.out.println("File already exists: " + csvFileName);
                    }
                } else if (parts.length == 2 && parts[0].equals("find")) {  // Process the find command

                    String[] findParts = parts[1].split("\\.");
                    if (findParts.length == 2) {
                        String fileName = findParts[0] + ".csv";
                        int id = Integer.parseInt(findParts[1]);
                        ApplicationContext.setCsvFileName(fileName);

                        FCBManager fcbManager = new FCBManager();
                        FileControlBlock fcb = fcbManager.findFCBByFileName(file,fileName); // Find the FCB by file name

                        if(fcb == null){
                            System.out.println("File not found: " + fileName);
                        }else {
                            BTreeIndex result = indexManager.readIndexFromFile(file, ApplicationContext.getCsvFileName()); // Read the index from the file
                            System.out.println("found data: " + blockWriter.readData(result.get(id), id)); // Read the data from the block
                            System.out.println("Block #" + result.get(id)); // Print the block number
                        }
                    }else {
                        System.out.println("Invalid format. Please use the format: find filename.id");
                    }
                }else if (parts.length == 2 && parts[0].equals("get")) { // Process the get command
                    String fileName = parts[1];
                    ApplicationContext.setCsvFileName(fileName);

                    FCBManager fcbManager = new FCBManager();
                    FileControlBlock fcb = fcbManager.findFCBByFileName(file, fileName); // Find the FCB by file name

                    if (fcb == null) {
                        System.out.println("File not found: " + fileName);
                    } else {
                        String outputFileName = fileName.replace(".csv", "_output.csv"); // Generate the output file name

                        try (PrintWriter writer = new PrintWriter(new FileWriter(outputFileName))) { // Create a new print writer
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

                }else if (parts.length == 2 && parts[0].equals("delete")) { // Process the delete command
                    String fileName = parts[1];
                    ApplicationContext.setCsvFileName(fileName);

                    FCBManager fcbManager = new FCBManager();
                    FileControlBlock fcb = fcbManager.findFCBByFileName(file, fileName);

                    if (fcb == null) {
                        System.out.println("File not found: " + fileName);
                    } else {
                        int startBlock = fcb.getStartBlock();
                        int numBlocks = fcb.getUsedBlocks();
                        indexManager.removeIndexForFile(file, blockManager, fileName); // Remove the index for the file
                        blockManager.releaseContiguousBlocks(startBlock, numBlocks); // Release the contiguous blocks
                        blockWriter.clearBlocks(startBlock, numBlocks); // Clear the blocks
                        fcbManager.removeFCBFromMetadata(file, fcb); // Remove the FCB from the metadata


                        System.out.println("File deleted successfully: " + fileName);
                    }
                }
                else if (parts[0].equalsIgnoreCase("dir")) { // Process the dir command
                    FCBManager fcbManager = new FCBManager();
                    List<FileControlBlock> blocklist = fcbManager.readFCBListFromMetadata(file);
                    System.out.println("Total number of files: " + blocklist.size());
                    for (FileControlBlock block : blocklist) {
                        System.out.println("========================FCB info===============================");
                        System.out.println("File name: " + block.getFileName());
                        System.out.println("File size: " + block.getFileSize());
                        System.out.println("Created date: " + block.getDate());
                        System.out.println("Start block ID: " + block.getStartBlock());
                        System.out.println("End block ID: " + (block.getStartBlock()+block.getUsedBlocks()));
                    }
                }else if (parts[0].equalsIgnoreCase("kill")) { // Process the kill command
                    if (parts.length == 2) {
                        String fileName = parts[1];
                        String filePath = fileName + ".db0";
                        File databaseFile = new File(filePath);

                        if (databaseFile.exists()) {
                            if (databaseFile.delete()) {
                                System.out.println("PFS file deleted successfully: " + filePath);
                            } else {
                                System.out.println("Failed to delete PFS file: " + filePath);
                            }
                        } else {
                            System.out.println("PFS file does not exist: " + filePath);
                        }
                    } else {
                        System.out.println("Invalid command. Usage: kill <file_name>");
                    }
                }
                else if (parts[0].equalsIgnoreCase("quit")) {
                    if (file != null) {
                        file.close();
                    }
                    exit = true;
                }else{
                    System.out.println("Invalid command. Supported commands: open, put, find, dir, kill, get, quit");

                }
            } catch (FileNotFoundException e) {
                System.err.println("The specified file does not exist: " + csvFileName);
            }
        }
    }
}
