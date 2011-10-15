package jay.moviefetch.core;


import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jay.moviefetch.core.beans.Movie;
import jay.moviefetch.core.beans.MovieGerm;

import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.AndFilter;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.filters.HasParentFilter;
import org.htmlparser.filters.NotFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.util.NodeIterator;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;


public class ImdbMovieFetcher implements MovieFetcher {

	private final static String searchUrl = "http://www.opensubtitles.org/pl/search/sublanguageid-all";


	public Movie fetchMovie(MovieGerm germ) throws MovieFetchException {

		List<String> movieUrls;
		try {
			movieUrls = searchForMovie(germ);
		}catch(ParserException ex) {
			ex.printStackTrace();
			throw new MovieFetchException("Parsing failed: " + ex.getMessage(), ex);
		}
		
		Movie movie = null;
		for(String movieUrl: movieUrls) {
			boolean flag = true;
			try{
				movie = extractMovie(movieUrl);
			}catch(ParserException ex) {
				flag = false;
			}
			
			if(flag) break;
			
		}
		
		if(movie == null)
			throw new MovieFetchException("Movie not found");
		
		return movie;
	}

	public List<Movie> fetchMovies(List<MovieGerm> germs) throws MovieFetchException{
		List<Movie> movies = new ArrayList<Movie>();
		for(MovieGerm germ: germs) 
			movies.add(fetchMovie(germ));

		return movies;
	}

	private List<String> searchForMovie(MovieGerm germ) throws ParserException {

		String url = createSearchUrl(germ);

		Parser.getConnectionManager().setRedirectionProcessingEnabled(true);
		Parser parser = new Parser(url);
		
		NodeList nodes = parser.parse(null);
		NodeList results = nodes.extractAllNodesThatMatch(new AndFilter(new TagNameFilter("table"), new HasAttributeFilter("id", "search_results")), true);
		
		if(results.size() != 0) {
			return searchResults(results);
		} else {
			results = nodes.extractAllNodesThatMatch(new AndFilter(new TagNameFilter("a"), new AndFilter(new HasAttributeFilter("href"), 
					new HasAttributeFilter("title"))), true);
			return searchRedirected(results);
		} 

	}

	private String createSearchUrl(MovieGerm germ) {
		String baseSearch = "/moviebytesize-" + germ.getFileSize() + "/moviefilename-" + germ.getFileName();
		String extendedSearch = baseSearch + "/moviehash-" + germ.getFileHash();

		if(germ.getFileHash() == null) {
			try {
				return  new URI(
					    "http", 
					    searchUrl, 
					    baseSearch,
					    null).toString();
			} catch (URISyntaxException e) {
				return (searchUrl + baseSearch).replace(' ', '+');
			}
		}else{
			try {
				return new URI(
					    "http", 
					    searchUrl, 
					    extendedSearch,
					    null).toString();
			} catch (URISyntaxException e) {
				return (searchUrl + extendedSearch).replace(' ', '+');
			}
		}
	}

	private List<String> searchResults(NodeList nodes) {
		String regex = "reLink\\('/redirect/(http://www\\.imdb\\.com/title/tt\\d+/)'\\)";
		Pattern pattern = Pattern.compile(regex);
		List<String> urls = new ArrayList<String>();

		String content = nodes.toHtml();
		Matcher matcher = pattern.matcher(content);

		while(matcher.find()) 
			urls.add(matcher.group(1));
		
		return urls;
	}

	private List<String> searchRedirected(NodeList nodes) {
		String regex = "(http://www\\.imdb\\.com/title/tt\\d+/)";
		Pattern pattern = Pattern.compile(regex);
		List<String> urls = new ArrayList<String>();

		String content = nodes.toHtml();
		Matcher matcher = pattern.matcher(content);

		while(matcher.find()) 
			urls.add(matcher.group(1));
		
		return urls;	
		
	}


private Movie extractMovie(String url) throws ParserException{
	Parser parser = new Parser(url);
	NodeList allNodes = parser.parse(null);
	NodeFilter titleFilter = new TagNameFilter("title");
	NodeFilter overviewFilter = new AndFilter(new TagNameFilter("div"), new HasAttributeFilter("class", "article title-overview"));
	NodeFilter storylineFilter = new AndFilter(new TagNameFilter("div"), new HasAttributeFilter("class", "article"));

	// Get all data from title
	String title;
	int year = 0;
	{
		NodeList titleNodes = allNodes.extractAllNodesThatMatch(titleFilter, true);
		if(titleNodes.size() == 0)
			throw new ParserException("Title tag not found");

		String titleString = titleNodes.elementAt(0).getChildren().elementAt(0).getText();
		Pattern pattern = Pattern.compile(".*\\(");
		Matcher matcher = pattern.matcher(titleString);

		if(matcher.find()) {
			title = matcher.group();
			title = title.substring(0, title.length()-1).trim();
		} else throw new ParserException("Movie title not found in page title");
		title = title.trim();

		pattern = Pattern.compile("\\(\\d\\d\\d\\d\\)");
		matcher = pattern.matcher(titleString);

		if(matcher.find()) {
			String yearStr = matcher.group();
			year = Integer.parseInt(yearStr.substring(1, 5));
		} else throw new ParserException("Movie date not found in page title");

	}

	// Get all data from overview section
	// Infobar:
	List<String> tags = new ArrayList<String>();
	int length = 0;
	{ 
		NodeList infobarNodes = allNodes.extractAllNodesThatMatch(new AndFilter(new HasParentFilter(overviewFilter, true), 
				new HasAttributeFilter("class", "infobar")), true);

		if(infobarNodes.size() != 0) {

			String infobarString = infobarNodes.toHtml();
			Pattern pattern = Pattern.compile("\\d{1,3} min");
			Matcher matcher = pattern.matcher(infobarString);

			if(matcher.find()) {
				String lengthString = matcher.group();
				length = Integer.parseInt(lengthString.substring(0, lengthString.indexOf("min")).trim());
			} 

			NodeList linkNodes = infobarNodes.extractAllNodesThatMatch(new AndFilter(new TagNameFilter("a"), 
					new AndFilter(new NotFilter(new HasAttributeFilter("title", "See all release dates")), new HasAttributeFilter("href"))), true);
			for(NodeIterator iterator = linkNodes.elements(); iterator.hasMoreNodes(); ) 
				tags.add(iterator.nextNode().getChildren().elementAt(0).getText());
		}

	}

	// Rating
	float rating = 0f;
	{
		NodeList ratingNodes = allNodes.extractAllNodesThatMatch(new AndFilter(new HasParentFilter(overviewFilter, true), 
				new AndFilter(new TagNameFilter("span"), new HasAttributeFilter("itemprop", "ratingValue"))), true);

		if(ratingNodes.size() != 0) 
			try{
				rating = Float.parseFloat(ratingNodes.elementAt(0).getChildren().elementAt(0).getText());
			}catch(NumberFormatException ex) {
				rating = 0f; // Not enough votes on IMDB
			}

	}

	// Cast
	List<String> cast = new ArrayList<String>();
	{
		NodeList txtblockNodes = allNodes.extractAllNodesThatMatch(new AndFilter(new HasParentFilter(overviewFilter, true), 
				new AndFilter(new TagNameFilter("div"), new HasAttributeFilter("class", "txt-block"))), true);
		for(NodeIterator iterator = txtblockNodes.elements(); iterator.hasMoreNodes(); ) {
			Node node = iterator.nextNode();
			NodeList sNames = node.getChildren().extractAllNodesThatMatch(
					new AndFilter(new TagNameFilter("h4"), new HasAttributeFilter("class", "inline")));

			if(sNames.size() != 0) {
				if(sNames.elementAt(0).getChildren().elementAt(0).getText().contains("Stars:")) {
					NodeList links = node.getChildren().extractAllNodesThatMatch(
							new AndFilter(new TagNameFilter("a"), new HasAttributeFilter("href")));
					for(NodeIterator linksIterator = links.elements(); linksIterator.hasMoreNodes(); )
						cast.add(linksIterator.nextNode().getChildren().elementAt(0).getText());
				}
			}
		}
	}

	// Storyline
	String plot = "";
	{
		NodeList storylineNodes = allNodes.extractAllNodesThatMatch(new AndFilter(new HasParentFilter(storylineFilter, true), 
				new TagNameFilter("p")), true);

		if(storylineNodes.size() != 0)
			plot = storylineNodes.elementAt(0).getChildren().elementAt(0).getText();
	}

	// Image
	String posterUrl = "";
	{
		NodeList imageSections = allNodes.extractAllNodesThatMatch(new AndFilter(new HasParentFilter(overviewFilter, true),
				new AndFilter(new TagNameFilter("td"), new HasAttributeFilter("id", "img_primary"))), true);
		NodeList images = imageSections.extractAllNodesThatMatch(new AndFilter(new TagNameFilter("img"),
				new HasAttributeFilter("src")), true);

		if(images.size() != 0) {
			Pattern pattern = Pattern.compile("http://.*\\.jpg");
			Matcher matcher = pattern.matcher(images.elementAt(0).getText());

			if(matcher.find()) 
				posterUrl = matcher.group();

		}
	}

	return new Movie(title, plot, cast, tags, year, length, rating, posterUrl);
}

}
