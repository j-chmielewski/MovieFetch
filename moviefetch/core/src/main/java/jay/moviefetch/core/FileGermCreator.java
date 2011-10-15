package jay.moviefetch.core;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jay.moviefetch.core.beans.MovieGerm;


public class FileGermCreator implements MovieGermCreator {
	
	@Override
	public MovieGerm createGerm(File file) {
		String hash;
		try{
			hash = OpenSubtitlesHasher.computeHash(file);
		}catch(IOException ex) {
			return new MovieGerm(file.getName(), getFileSize(file));
		}
		return new MovieGerm(file.getName(), getFileSize(file), hash);
	}
	
	@Override
	public List<MovieGerm> createGerms(File[] files) {
		List<MovieGerm> germList = new ArrayList<MovieGerm>();
		for(File string: files) {
			germList.add(createGerm(string));
		}
		
		return germList;
	}
	
	private long getFileSize(File file) {
		return file.length();

	}

}
