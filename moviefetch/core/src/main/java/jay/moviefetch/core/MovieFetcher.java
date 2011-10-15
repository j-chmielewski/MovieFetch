package jay.moviefetch.core;

import java.util.List;

import jay.moviefetch.core.beans.Movie;
import jay.moviefetch.core.beans.MovieGerm;

public interface MovieFetcher {

	public Movie fetchMovie(MovieGerm germ) throws MovieFetchException;
	public List<Movie> fetchMovies(List<MovieGerm> germs) throws MovieFetchException;
	
}
