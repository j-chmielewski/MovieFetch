package jay.moviefetch.core;

import java.util.List;

import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.filters.AndFilter;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.nodes.TagNode;
import org.htmlparser.nodes.TextNode;
import org.htmlparser.tags.Div;
import org.htmlparser.tags.ImageTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

import jay.moviefetch.core.beans.Movie;
import jay.moviefetch.core.beans.MovieGerm;

public class FetcherCatalogueGenerator implements CatalogueGenerator {

	@Override
	public String generateCatalogue(List<Movie> movies, List<MovieGerm> notFound, String templatePath) throws CatalogueGeneratorException {
		
		NodeList template = null;
		Parser parser;
		try {
			parser = new Parser(templatePath);
			template = parser.parse(null);
		} catch (ParserException ex) {
			throw new CatalogueGeneratorException("Parsing template failed", ex);
		}
		
		NodeList contentList = template.extractAllNodesThatMatch(new AndFilter(
				new TagNameFilter("div"), new HasAttributeFilter("id", "content")), true);
		
		if(contentList.size() == 0)
			throw new CatalogueGeneratorException("No content section in template");
		
		Node contentNode = contentList.elementAt(0);
		NodeList movieNodes = new NodeList();
		for(Movie movie: movies) 
			movieNodes.add(generateMovieDiv(movie));
		for(MovieGerm germ: notFound)
			movieNodes.add(generateNotFoundMovieDiv(germ));
			
		contentNode.setChildren(movieNodes);
		return template.toHtml();
		
	}
	
	private Node generateMovieDiv(Movie movie) {
		Div movieNode = new Div();
		movieNode.setAttribute("class", "\"movie\"");
		NodeList movieInfo = new NodeList();

		TagNode divEnd = new TagNode();
		divEnd.setTagName("/div");
		
		// Poster
		ImageTag imageNode = new ImageTag();
		imageNode.setImageURL(movie.getPosterUrl());
		Div posterNode = new Div();
		posterNode.setAttribute("class", "\"poster\"");
		posterNode.setChildren(new NodeList(imageNode));
		movieInfo.add(posterNode);
		movieInfo.add(divEnd);

		// Title
		Div titleNode = new Div();
		titleNode.setAttribute("class", "\"title\"");
		titleNode.setChildren(new NodeList(new TextNode(movie.getTitle())));
		movieInfo.add(titleNode);
		movieInfo.add(divEnd);
		
		// Year
		Div yearNode = new Div();
		yearNode.setAttribute("class", "\"year\"");
		yearNode.setChildren(new NodeList(new TextNode("" + Integer.valueOf(movie.getYear()))));
		movieInfo.add(yearNode);
		movieInfo.add(divEnd);
		
		// Rating
		Div ratingNode = new Div();
		ratingNode.setAttribute("class", "\"rating\"");
		ratingNode.setChildren(new NodeList(new TextNode(new Float(movie.getRating()).toString() + "/10")));
		movieInfo.add(ratingNode);
		movieInfo.add(divEnd);
		
		// Cast
		Div castNode = new Div();
		castNode.setAttribute("class", "\"cast\"");
		castNode.setChildren(new NodeList(new TextNode(movie.getCast().toString())));
		movieInfo.add(castNode);
		movieInfo.add(divEnd);
		
		// Tags
		Div tagsNode = new Div();
		tagsNode.setAttribute("class", "\"tags\"");
		tagsNode.setChildren(new NodeList(new TextNode(movie.getTags().toString())));
		movieInfo.add(tagsNode);
		movieInfo.add(divEnd);
		
		// Plot
		Div plotNode = new Div();
		plotNode.setAttribute("class", "\"plot\"");
		plotNode.setChildren(new NodeList(new TextNode(movie.getPlot())));
		movieInfo.add(plotNode);
		movieInfo.add(divEnd);
		
		// Length
		Div lengthNode = new Div();
		lengthNode.setAttribute("class", "\"length\"");
		lengthNode.setChildren(new NodeList(new TextNode("" + Integer.valueOf(movie.getLength()) + "min")));
		movieInfo.add(lengthNode);
		movieInfo.add(divEnd);
		
		movieInfo.add(divEnd);
		
		movieNode.setChildren(movieInfo);
		return movieNode;
		
	}
	
	private Node generateNotFoundMovieDiv(MovieGerm germ) {
		Div movieNode = new Div();
		movieNode.setAttribute("class", "\"movie\"");
		NodeList movieInfo = new NodeList();

		TagNode divEnd = new TagNode();
		divEnd.setTagName("/div");
		
		// Title
		Div titleNode = new Div();
		titleNode.setAttribute("class", "\"title\"");
		titleNode.setChildren(new NodeList(new TextNode(germ.getFileName())));
		movieInfo.add(titleNode);
		movieInfo.add(divEnd);
		
		// Year
		Div yearNode = new Div();
		yearNode.setAttribute("class", "\"year\"");
		yearNode.setChildren(new NodeList(new TextNode("" + Integer.valueOf(0))));
		movieInfo.add(yearNode);
		movieInfo.add(divEnd);
		
		movieInfo.add(divEnd);
		
		movieNode.setChildren(movieInfo);
		return movieNode;
	}
}
