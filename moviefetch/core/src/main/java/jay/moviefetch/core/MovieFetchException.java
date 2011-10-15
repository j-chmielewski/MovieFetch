package jay.moviefetch.core;

public class MovieFetchException extends Exception {

	public MovieFetchException() {
		super();
	}
	
	public MovieFetchException(String string) {
		super(string);
	}
	
	public MovieFetchException(String string, Exception cause) {
		super(string, cause);
	}
	
}
