package peer;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.*;

/**
 * Class that manages the storage and lookup of local files
 */
public class FileManager {

    /**
     * Stores the chunk numbers stored for each file
     */
    private ConcurrentHashMap<String, ArrayList<Integer>> fileToChunks;

    /**
     * Stores the storage space used so far
     */
    private int usedStorageSpace;

    /**
     * The ID of the peer of which files are being managed
     */
    private int peerId;

    /**
     * 
     * @param peerId The ID of the peer of which files are going to be managed
     */
    public FileManager(int peerId) {
        this.peerId = peerId;
        this.usedStorageSpace = 0;
        this.createDirectory("chunks");
        this.createDirectory("files");
        this.fileToChunks = new ConcurrentHashMap<>();
    }

    public int getUsedStorageSpace() {
		return this.usedStorageSpace;
	}

    /**
     * Creates a storage directory for the current peer
     * @return true if successful, false if otherwise
     */
    public boolean createDirectory(String dirName) {
        String path = this.getDirectoryPath(dirName);

        if(Files.exists(Paths.get(path)))
            return true;

        File file = new File(path);
        return file.mkdir();
    }

    /**
     * Return the path to a given chunk in the storage directory
     * @param fileId The ID of the file
     * @param chunkNo The number of the chunk
     * @param chunkContent The chunk's content
     * @return true if successful, false if otherwise
     */
    public boolean storeChunk(String fileId, int chunkNo, String chunkContent) throws IOException {
        if(this.isChunkStored(fileId, chunkNo)) {
            return true;
        }

        String chunkPath = getChunkPath(fileId, chunkNo);
        File chunk = new File(chunkPath);
        chunk.createNewFile();
        FileWriter chunkWriter = new FileWriter(chunk, true);
        chunkWriter.write(chunkContent);
        chunkWriter.close();

        // log storage
        this.usedStorageSpace += chunkContent.getBytes().length;

        ArrayList<Integer> chunksForFile = this.fileToChunks.get(fileId);
        if(chunksForFile == null) chunksForFile = new ArrayList<>();
        chunksForFile.add(chunkNo);
        this.fileToChunks.put(fileId, chunksForFile);

        return true;    //TODO: add error checking
    }

    /**
     * Return the content of a chunk
     * @param fileId The ID of the file
     * @param chunkNo The number of the chunk
     * @return A string with the chunk's content
     */
    public String retrieveChunk(String fileId, int chunkNo) throws IOException {
        String chunkPath = getChunkPath(fileId, chunkNo);
        String chunkContent = Files.readString(Paths.get(chunkPath), StandardCharsets.US_ASCII);
        return chunkContent;
    }

    /**
     * Returns the path to the peer's storage directory
     * @return A string containing the path
     */
    public String getDirectoryPath(String dirName) {
        return System.getProperty("user.dir") + "/peer/" + dirName + "/" + this.peerId;
    }

    /**
     * Return the path to a given chunk in the storage directory
     * @param fileId The ID of the file
     * @param chunkNo The number of the chunk
     * @return A string containing the path
     */
    public String getChunkPath(String fileId, int chunkNo) {
        String storageDirectoryPath = getDirectoryPath("chunks");
        String chunkPath = storageDirectoryPath + "/" + fileId + "_" + Integer.toString(chunkNo);
        return chunkPath;
    }

    private boolean isChunkStored(String fileId, int chunkNo) {
        ArrayList<Integer> chunksForFile = this.fileToChunks.get(fileId);
        return (chunksForFile == null) ? false : chunksForFile.contains(chunkNo);
    }
}
