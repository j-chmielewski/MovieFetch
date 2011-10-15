package jay.moviefetch.core;

import java.io.File;
import java.util.List;

import jay.moviefetch.core.beans.MovieGerm;

public interface MovieGermCreator {

	public MovieGerm createGerm(File file);
	public List<MovieGerm> createGerms(File[] files);
	
}
