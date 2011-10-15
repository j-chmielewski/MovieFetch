package jay.moviefetch;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.htmlparser.Parser;

import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

import org.junit.Ignore;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import jay.moviefetch.core.CatalogueGenerator;
import jay.moviefetch.core.CatalogueGeneratorException;
import jay.moviefetch.core.FetcherCatalogueGenerator;
import jay.moviefetch.core.FetcherMovieSeeker;
import jay.moviefetch.core.ImdbMovieFetcher;
import jay.moviefetch.core.MovieFetchException;
import jay.moviefetch.core.MovieFetcher;
import jay.moviefetch.core.MovieFilesSeeker;
import jay.moviefetch.core.MovieGermCreator;
import jay.moviefetch.core.FileGermCreator;
import jay.moviefetch.core.beans.Movie;
import jay.moviefetch.core.beans.MovieGerm;

public class AppTest extends TestCase {

	private final static String[] filenames = { "Alive.[1993].DVDRip.XviD",
			"Awakenings.1990.544x288.25fps.729kbs.V5mp3.MultiSub.WunSeeDee",
			"Berlin Calling-FF", "Braveheart (1995) [ENG] [DVDrip] CD1",
			"A Bronx Tale DVDRip Occor", "Casino[1995]DvDrip[Eng]-Zeus_Dias",
			"Raging Bull (1980) DVD-RIP DIVX.avi",
			"The.Mission[1986]DvDrip]-Zeus_Dias",
			"Flatliners.1990.DVDRip.XviD.[Movie-Torrentz]" };

	private final static String movieDir = "/home/jay/filmy/";
	private final static String templateLocation = "/home/jay/workspace/moviefetch/template/index.html";

	public AppTest(String testName) {
		super(testName);
	}

	public static Test suite() {
		return new TestSuite(AppTest.class);
	}

	
//	@Ignore @org.junit.Test 
//	public void testGermCreator() {
//
//		List<MovieGerm> germs = parseFileNames(filenames);
//		assertNotNull(germs);
//
//		System.out.println("---testTitleParser:");
//		for (MovieGerm germ : germs)
//			System.out.println("Title: " + germ.getTitle() + " Year: "
//					+ germ.getYear());
//	}
//
//	@Ignore @org.junit.Test
//	public void testFilesSeeker() {
//
//		List<File> movies = getFileNames();
//		assertNotNull(movies);
//
//		System.out.println("---testFilesSeeker");
//		for (File file : movies)
//			System.out.println(file.getPath());
//	}

	@Ignore @org.junit.Test
	public void testParserSeekerIntegration() {

		List<File> movies = getFileNames();
		assertNotNull(movies);

		List<MovieGerm> germs = parseFileNames(movies.toArray(new File[movies
				.size()]));
		assertNotNull(germs);

		System.out.println("---testParserSeekerIntegration");
		for (MovieGerm germ : germs)
			System.out.println("FileNeme: " + germ.getFileName() + " Size: "
					+ germ.getFileSize());
	}

//	@Ignore @org.junit.Test
//	public void fetcher() throws MovieFetchException {
//
//		Movie mov = fetchMovie(new MovieGerm("matrix", 2003));
//		assertNotNull(mov);
//
//		System.out.println("---testFetcher");
//		System.out.println("-Title: " + mov.getTitle() + " Year: "
//				+ mov.getYear() + " Rating " + mov.getRating() + " Length: "
//				+ mov.getLength());
//		System.out.println("-Tags:");
//		for (String tag : mov.getTags())
//			System.out.print(tag + " ");
//		System.out.println();
//		System.out.println("-Cast:");
//		for (String star : mov.getCast())
//			System.out.print(star + " ");
//		System.out.println();
//		System.out.println("-Plot: ");
//		System.out.println(mov.getPlot());
//
//	}

//	@Ignore @org.junit.Test
//	public void parserSeekerFetcherIntegration() throws MovieFetchException {
//
//		System.out.println("---testParserSeekerFetcherIntegration");
//
//		List<String> movies = getFileNames();
//		assertNotNull(movies);
//
//		List<MovieGerm> germs = parseFileNames(movies.toArray(new String[movies
//				.size()]));
//		assertNotNull(germs);
//
//		float counter = 0;
//		for (MovieGerm germ : germs) {
//			System.out.println("Fetching: " + germ.getTitle() + "("
//					+ germ.getYear() + ")");
//			Movie mov;
//			try {
//				mov = fetchMovie(germ);
//			} catch (MovieFetchException ex) {
//				System.out.println("Not found");
//				continue;
//			}
//			counter++;
//			System.out.println("-Title: " + mov.getTitle() + " Year: "
//					+ mov.getYear() + " Rating " + mov.getRating()
//					+ " Length: " + mov.getLength());
//			System.out.println("-Tags:");
//			for (String tag : mov.getTags())
//				System.out.print(tag + " ");
//			System.out.println();
//			System.out.println("-Cast:");
//			for (String star : mov.getCast())
//				System.out.print(star + " ");
//			System.out.println();
//			System.out.println("-Plot: ");
//			System.out.println(mov.getPlot());
//		}
//		System.out.println("Found " + counter / germs.size() * 100 + "%");
//	}

//	@Ignore @org.junit.Test
//	public void catalogueGenerator() throws CatalogueGeneratorException,
//			MovieFetchException {
//
//		System.out.println("---testCatalogueGenerator");
//
//		Movie movie = fetchMovie(new MovieGerm("Matrix", 1999));
//		assertNotNull(movie);
//		List<Movie> mList = new ArrayList<Movie>();
//		mList.add(movie);

//		NodeList template = getTemplate();
//		CatalogueGenerator generator = new FetcherCatalogueGenerator();
//		String test = generator.generateCatalogue(mList, templateLocation);
//		System.out.print(test);

//	}
	

	private List<File> getFileNames() {
		MovieFilesSeeker seeker = new FetcherMovieSeeker();
		assertNotNull(seeker);
		return seeker.findMovieFiles(new File(movieDir));

	}

	private List<MovieGerm> parseFileNames(File[] movies) {
		MovieGermCreator parser = new FileGermCreator();
		assertNotNull(parser);
		return parser.createGerms(movies);
	}

//	private Movie fetchMovie(MovieGerm germ) throws MovieFetchException {
//		MovieFetcher fetcher = new ImdbMovieFetcher();
//		assertNotNull(fetcher);
//
//		return fetcher.fetchMovie(germ);
//	}
//
//	private NodeList getTemplate() throws CatalogueGeneratorException {
//		Parser parser;
//		try {
//			parser = new Parser(templateLocation);
//			return parser.parse(null);
//		} catch (ParserException ex) {
//			throw new CatalogueGeneratorException("Parsing failed", ex);
//		}
//		
//	}
}
