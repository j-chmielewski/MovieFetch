package jay;

import java.awt.Desktop;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import jay.moviefetch.core.CatalogueGenerator;
import jay.moviefetch.core.CatalogueGeneratorException;
import jay.moviefetch.core.FetcherCatalogueGenerator;
import jay.moviefetch.core.FetcherMovieSeeker;
import jay.moviefetch.core.FileGermCreator;
import jay.moviefetch.core.ImdbMovieFetcher;
import jay.moviefetch.core.MovieFetchException;
import jay.moviefetch.core.MovieFetcher;
import jay.moviefetch.core.MovieFilesSeeker;
import jay.moviefetch.core.MovieGermCreator;
import jay.moviefetch.core.beans.Movie;
import jay.moviefetch.core.beans.MovieGerm;

/**
 * This code was edited or generated using CloudGarden's Jigloo SWT/Swing GUI
 * Builder, which is free for non-commercial use. If Jigloo is being used
 * commercially (ie, by a corporation, company or business for any purpose
 * whatever) then you should purchase a license for each developer using Jigloo.
 * Please visit www.cloudgarden.com for details. Use of Jigloo implies
 * acceptance of these licensing terms. A COMMERCIAL LICENSE HAS NOT BEEN
 * PURCHASED FOR THIS MACHINE, SO JIGLOO OR THIS CODE CANNOT BE USED LEGALLY FOR
 * ANY CORPORATE OR COMMERCIAL PURPOSE.
 */
public class FetcherMainFrame extends javax.swing.JFrame implements
		ActionListener, Runnable {

	private static enum GUI_STATE {
		READY, FETCHING
	};

	private static final String templateDir = "template";
	private static final String templatePath = "template/template.html";
	private static final String catalogueDir = "MovieFetch";
	private static final String catalogueFileName = "myMovies.html";

	private JPanel mainPanel;
	private JTextField pathField;
	private JScrollPane jScrollPane1;
	private JTextArea statusArea;
	private JProgressBar pBar;
	private JButton fetchButton;
	private JButton browseButton;
	JFileChooser chooser = new JFileChooser();

	private MovieFetcher fetcher = new ImdbMovieFetcher();
	private MovieFilesSeeker seeker = new FetcherMovieSeeker();
	private MovieGermCreator germCreator = new FileGermCreator();
	private CatalogueGenerator cGenerator = new FetcherCatalogueGenerator();

	private Thread fetchThread;
	private GUI_STATE guiState = GUI_STATE.READY;

	/**
	 * Auto-generated main method to display this JFrame
	 */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				FetcherMainFrame inst = new FetcherMainFrame();
				inst.setLocationRelativeTo(null);
				inst.setVisible(true);
			}
		});
	}

	public FetcherMainFrame() {
		super();
		fetcher = new ImdbMovieFetcher();
		initGUI();
	}

	public void run() {

		List<File> mFiles = seeker.findMovieFiles(new File(pathField
				.getText()));
		statusArea.setText("");
		statusArea.append("Found " + mFiles.size() + " video files");
		scrollDown();
		if (mFiles.size() != 0) {
			statusArea.append("\nParsing filenames\n");
			scrollDown();
			List<MovieGerm> germs = new ArrayList<MovieGerm>();
			for (File file : mFiles) {
				MovieGerm germ = germCreator.createGerm(file);
				if (!germs.contains(germ))
					germs.add(germ);
			}

			List<Movie> movies = new ArrayList<Movie>();
			pBar.setMinimum(0);
			pBar.setMaximum(germs.size());
			pBar.setValue(0);
			int counter = 0;
			int downloaded = 0;
			pBar.setString("" + counter + "/" + pBar.getMaximum());
			List<MovieGerm> notFound = new ArrayList<MovieGerm>();
			statusArea.append("----- Fetch started -----\n");
			for (MovieGerm germ : germs) {
				scrollDown();

				// Exception-driven control, refactor:
				Movie movie = null;
				try {
					movie = fetcher.fetchMovie(germ);
				} catch (MovieFetchException ex) {
					statusArea.append("Failed: " + germ.getFileName() + "\n");
					notFound.add(germ);
					continue;
				} finally {
					pBar.setValue(++counter);
					pBar.setString("" + counter + "/" + pBar.getMaximum());
				}
				downloaded++;
				statusArea.append("Fetched: " + movie.getTitle() + "\n");
				if (!movies.contains(movie))
					movies.add(movie);
			}

			Collections.sort(movies);
			Collections.sort(notFound);
			String catalogueHtml = null;
			try {
				catalogueHtml = cGenerator.generateCatalogue(movies, notFound,
						templatePath);
			} catch (CatalogueGeneratorException e) {
				showError("Cannot generate catalogue: " + e.getMessage());
				guiState = GUI_STATE.READY;
				updateGui();
				return;
			}

			String fullCataloguePath = pathField.getText() + "/" + catalogueDir
					+ "/" + catalogueFileName;
			statusArea.append("----- Fetch finished -----");
			statusArea.append("\nSaving catalogue to " + fullCataloguePath);
			scrollDown();

			try {
				copyDirectory(new File(templateDir),
						new File(pathField.getText() + "/" + catalogueDir));
				BufferedWriter out = new BufferedWriter(new FileWriter(
						fullCataloguePath));
				out.write(catalogueHtml);
				out.close();
			} catch (IOException e) {
				showError("Cannot generate catalogue: " + e.getMessage());
				guiState = GUI_STATE.READY;
				updateGui();
				return;
			}

			statusArea.append("\nDownloaded: " + downloaded + " out of "
					+ germs.size() + "\n");
			scrollDown();
			try {
				Desktop.getDesktop().open(new File(fullCataloguePath));
			} catch (IOException ex) {
				// Eat it...
			}
		}

		guiState = GUI_STATE.READY;
		updateGui();
	}

	private void initGUI() {
		GridLayout thisLayout = new GridLayout(1, 1);
		thisLayout.setHgap(5);
		thisLayout.setVgap(5);
		thisLayout.setColumns(1);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		getContentPane().setLayout(thisLayout);
		this.setTitle("MovieFetch");
		this.setResizable(false);
		{
			mainPanel = new JPanel();
			GroupLayout mainPanelLayout = new GroupLayout(
					(JComponent) mainPanel);
			getContentPane().add(mainPanel);
			mainPanel.setLayout(mainPanelLayout);
			mainPanel.setPreferredSize(new java.awt.Dimension(349, 254));
			{
				pathField = new JTextField();
				pathField.setEditable(false);
				pathField.setText("c:\\movies");
			}
			{
				pBar = new JProgressBar();
				pBar.setString("0/0");
				pBar.setStringPainted(true);
			}
			{
				jScrollPane1 = new JScrollPane();
				{
					statusArea = new JTextArea();
					jScrollPane1.setViewportView(statusArea);
					statusArea.setEditable(false);
				}
			}
			{
				browseButton = new JButton();
				browseButton.setText("Browse");
				browseButton.addActionListener(this);
			}
			{
				fetchButton = new JButton();
				fetchButton.setText("Fetch!");
				fetchButton.addActionListener(this);
			}
			mainPanelLayout
					.setVerticalGroup(mainPanelLayout
							.createSequentialGroup()
							.addContainerGap(12, 12)
							.addComponent(jScrollPane1,
									GroupLayout.PREFERRED_SIZE, 330,
									GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(
									LayoutStyle.ComponentPlacement.RELATED)
							.addGroup(
									mainPanelLayout
											.createParallelGroup(
													GroupLayout.Alignment.BASELINE)
											.addComponent(
													pathField,
													GroupLayout.Alignment.BASELINE,
													GroupLayout.PREFERRED_SIZE,
													28,
													GroupLayout.PREFERRED_SIZE)
											.addComponent(
													browseButton,
													GroupLayout.Alignment.BASELINE,
													GroupLayout.PREFERRED_SIZE,
													27,
													GroupLayout.PREFERRED_SIZE)
											.addComponent(
													fetchButton,
													GroupLayout.Alignment.BASELINE,
													GroupLayout.PREFERRED_SIZE,
													GroupLayout.DEFAULT_SIZE,
													GroupLayout.PREFERRED_SIZE))
							.addPreferredGap(
									LayoutStyle.ComponentPlacement.UNRELATED)
							.addComponent(pBar, GroupLayout.PREFERRED_SIZE, 28,
									GroupLayout.PREFERRED_SIZE)
							.addContainerGap(18, 18));
			mainPanelLayout
					.setHorizontalGroup(mainPanelLayout
							.createSequentialGroup()
							.addContainerGap(12, 12)
							.addGroup(
									mainPanelLayout
											.createParallelGroup()
											.addGroup(
													mainPanelLayout
															.createSequentialGroup()
															.addComponent(
																	pathField,
																	GroupLayout.PREFERRED_SIZE,
																	378,
																	GroupLayout.PREFERRED_SIZE)
															.addPreferredGap(
																	LayoutStyle.ComponentPlacement.RELATED)
															.addComponent(
																	browseButton,
																	GroupLayout.PREFERRED_SIZE,
																	106,
																	GroupLayout.PREFERRED_SIZE)
															.addPreferredGap(
																	LayoutStyle.ComponentPlacement.UNRELATED)
															.addComponent(
																	fetchButton,
																	GroupLayout.PREFERRED_SIZE,
																	98,
																	GroupLayout.PREFERRED_SIZE))
											.addComponent(
													pBar,
													GroupLayout.Alignment.LEADING,
													GroupLayout.PREFERRED_SIZE,
													599,
													GroupLayout.PREFERRED_SIZE)
											.addComponent(
													jScrollPane1,
													GroupLayout.Alignment.LEADING,
													GroupLayout.PREFERRED_SIZE,
													599,
													GroupLayout.PREFERRED_SIZE))
							.addContainerGap(12, 12));
		}
		pack();
		this.setSize(633, 464);
		guiState = GUI_STATE.READY;
		updateGui();
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		if (event.getSource() == browseButton) {
			chooser = new JFileChooser();
			chooser.setCurrentDirectory(new java.io.File("."));
			chooser.setDialogTitle("Choose movies direcrory");
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

			chooser.setAcceptAllFileFilterUsed(false);
			if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
				updatePath(chooser.getSelectedFile().toString());

		} else if (event.getSource() == fetchButton) {
			if (guiState == GUI_STATE.READY) {
				fetchThread = new Thread(this);
				guiState = GUI_STATE.FETCHING;
				updateGui();
				fetchThread.start();
			} else if (guiState == GUI_STATE.FETCHING) {
				fetchThread.interrupt();
				guiState = GUI_STATE.READY;
				updateGui();
			}
		}

	}

	private void showError(String message) {
		statusArea.append("\n" + message);
		scrollDown();
		JOptionPane.showMessageDialog(this, message, "Error",
				JOptionPane.ERROR_MESSAGE);
	}

	private void updatePath(String path) {
		pathField.setText(path);
	}

	private void updateGui() {
		if (guiState == GUI_STATE.READY) {
			fetchButton.setEnabled(true);
		} else if (guiState == GUI_STATE.FETCHING) {
			fetchButton.setEnabled(false);
		}
	}

	private void scrollDown() {
		statusArea.setCaretPosition(statusArea.getDocument().getLength());
	}

	private void copyDirectory(File sourceLocation, File targetLocation)
			throws IOException {

		if (sourceLocation.isDirectory()) {
			if (!targetLocation.exists()) {
				targetLocation.mkdir();
			}

			String[] children = sourceLocation.list();
			for (int i = 0; i < children.length; i++) {
				copyDirectory(new File(sourceLocation, children[i]), new File(
						targetLocation, children[i]));
			}
		} else {

			InputStream in = new FileInputStream(sourceLocation);
			OutputStream out = new FileOutputStream(targetLocation);

			// Copy the bits from instream to outstream
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			in.close();
			out.close();
		}
	}

}
