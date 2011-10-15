package jay.moviefetch.core;

import java.util.List;

import jay.moviefetch.core.beans.Movie;
import jay.moviefetch.core.beans.MovieGerm;

public interface CatalogueGenerator {

	public String generateCatalogue(List<Movie> movies, List<MovieGerm> notFound, String templatePath) throws CatalogueGeneratorException;
	
}
