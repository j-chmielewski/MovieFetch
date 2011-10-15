package jay.moviefetch.core.beans;

import java.util.List;


public class Movie implements Comparable<Movie>{

	private String title;
	private String plot;
	private List<String> cast;
	private List<String> tags;
	private int year = 0;
	private int length = 0;
	private float rating = 0;
	private String posterUrl;
	
	public Movie(String title, String plot, List<String> cast,
			List<String> tags, int year, int length, float rating) {
		super();
		this.title = title;
		this.plot = plot;
		this.cast = cast;
		this.tags = tags;
		this.year = year;
		this.length = length;
		this.rating = rating;
	}
	
	public Movie(String title, String plot, List<String> cast,
			List<String> tags, int year, int length, float rating,
			String posterUrl) {
		super();
		this.title = title;
		this.plot = plot;
		this.cast = cast;
		this.tags = tags;
		this.year = year;
		this.length = length;
		this.rating = rating;
		this.posterUrl = posterUrl;
	}
	
	@Override
	public int compareTo(Movie movie) {
		return this.title.compareTo(movie.getTitle());
	}
	
	@Override
	public boolean equals(Object o) {
		if(!(o instanceof Movie))
			return false;
		
		Movie m = (Movie)o;
		return title.equals(m.getTitle()) && plot.equals(m.getPlot()) && year == m.getYear() 
			&& length == m.getLength() && rating == m.getRating() && posterUrl.equals(m.getPosterUrl())
			&& tags.equals(m.getTags()) && cast.equals(m.getCast());
	}
	
	// Accessors

	public String getTitle() {
		return title;
	}

	public String getPlot() {
		return plot;
	}

	public List<String> getCast() {
		return cast;
	}

	public List<String> getTags() {
		return tags;
	}

	public int getYear() {
		return year;
	}

	public int getLength() {
		return length;
	}

	public float getRating() {
		return rating;
	}

	public String getPosterUrl() {
		return posterUrl;
	}
	
}
