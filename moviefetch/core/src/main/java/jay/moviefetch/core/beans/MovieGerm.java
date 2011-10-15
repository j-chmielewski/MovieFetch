package jay.moviefetch.core.beans;


public class MovieGerm implements Comparable<MovieGerm>{

	private String fileName;
	private long fileSize = 0;
	private String fileHash = null;
	
	public MovieGerm(String fileName, long fileSize) {
		this.fileName = fileName;
		this.fileSize = fileSize;
	}
	
	public MovieGerm(String fileName, long fileSize, String fileHash) {
		this.fileName = fileName;
		this.fileSize = fileSize;
		this.fileHash = fileHash;
	}
	
	@Override
	public boolean equals(Object o) {
		if(!(o instanceof MovieGerm))
			return false;
		
		MovieGerm germ = (MovieGerm)o;
		return germ.getFileName().equals(this.fileName) && germ.fileSize == this.fileSize; 
	}
	
	@Override
	public int compareTo(MovieGerm germ) {
		return fileName.compareTo(germ.getFileName());
	}

	public String getFileName() {
		return fileName;
	}

	public long getFileSize() {
		return fileSize;
	}

	public String getFileHash() {
		return fileHash;
	}


}
