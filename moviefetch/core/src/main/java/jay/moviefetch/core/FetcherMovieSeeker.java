package jay.moviefetch.core;

// TODO: rewrite to return list of files

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FetcherMovieSeeker implements MovieFilesSeeker {

	private final String[] movieExtensions = {"avi", "mpg", "mpeg", "mp4", "mkv", "rmvb"};
	
	@Override
	public List<File> findMovieFiles(File dir) {
		
		List<File> movieFiles = new ArrayList<File>();
		if (!dir.isDirectory())
			return movieFiles;
		
		String[] children = dir.list();
		for(String childString: children) {
			File child = new File(dir.getAbsolutePath() + "/" + childString);
			if(child.isDirectory()) {
				movieFiles.addAll(findMovieFiles(child));
			} else {
				if(isMovie(childString))
					movieFiles.add(child);
			}
		}
		
		return movieFiles;
	}
	
	private boolean isMovie(String string) {
		for(String extension: movieExtensions) 
			if(string.endsWith(extension)) 
				return true;
		
		return false;
			
		
	}

}
