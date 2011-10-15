package jay.moviefetch.core;

import java.io.File;
import java.util.List;

public interface MovieFilesSeeker {
	
	public List<File> findMovieFiles(File dir);

}
