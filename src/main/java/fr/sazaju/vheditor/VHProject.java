package fr.sazaju.vheditor;

import java.io.File;
import java.io.FileFilter;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import fr.sazaju.vheditor.VHMap.VHEntry;
import fr.sazaju.vheditor.html.MapLabelPage;
import fr.sazaju.vheditor.html.MapRow;
import fr.sazaju.vheditor.html.MapTable;
import fr.vergne.parsing.layer.exception.ParsingException;
import fr.vergne.translation.editor.Editor;
import fr.vergne.translation.editor.tool.FileBasedProperties;
import fr.vergne.translation.impl.MapFilesProject;
import fr.vergne.translation.impl.NoTranslationFilter;
import fr.vergne.translation.impl.PatternFileMap;
import fr.vergne.translation.util.EntryFilter;
import fr.vergne.translation.util.MapNamer;
import fr.vergne.translation.util.MultiReader;
import fr.vergne.translation.util.impl.MapFileNamer;
import fr.vergne.translation.util.impl.SimpleFeature;

public class VHProject extends MapFilesProject<VHEntry, VHMap> {

	public static final Logger logger = Logger.getLogger(VHProject.class
			.getName());
	private static final String CONFIG_LABEL_SOURCE = "labelSource";
	private static final String CACHE_LABEL_PREFIX = "label.";
	private static final String CONFIG_LABEL_LAST_UPDATE = "lastLabelUpdate";
	private final FileBasedProperties cache = new FileBasedProperties(
			"vh-cache", false);

	public VHProject(File directory) {
		super(retrieveFiles(directory), new MultiReader<File, VHMap>() {

			@Override
			public VHMap read(File file) {
				PatternFileMap.logger.setLevel(Level.OFF);
				return new VHMap(file);
			}
		});

		if (Editor.config.containsKey(CONFIG_LABEL_SOURCE)) {
			// source already configured
		} else {
			Editor.config.setProperty(CONFIG_LABEL_SOURCE,
					"https://www.assembla.com/spaces/VH/wiki/Map_List");
		}
		loadLabels(false);

		addMapNamer(new MapFileNamer("File", "Use the file names of the maps."));
		addMapNamer(new MapNamer<File>() {

			@Override
			public String getName() {
				return "Label";
			}

			@Override
			public String getDescription() {
				return "Use the names of the maps as provided in the map "
						+ "list of the Assembla project. It requires an Internet "
						+ "connection to load the last version of the list.";
			}

			@Override
			public String getNameFor(File file) {
				String fileName = file.getName();
				String label = cache.getProperty(CACHE_LABEL_PREFIX + fileName);
				if (label == null) {
					return "[" + fileName + "]";
				} else {
					return label;
				}
			}
		});
		addMapNamer(new MapNamer<File>() {

			@Override
			public String getName() {
				return "Label [Number]";
			}

			@Override
			public String getDescription() {
				return "Use the names of the maps as provided in the map "
						+ "list of the Assembla project followed by the number "
						+ "of the map (the \"Xxx\" in \"MapXxx.txt\"). It "
						+ "requires an Internet connection to load the last "
						+ "version of the list.";
			}

			@Override
			public String getNameFor(File file) {
				String fileName = file.getName();
				String label = cache.getProperty(CACHE_LABEL_PREFIX + fileName);

				if (label == null) {
					return "[" + fileName + "]";
				} else if (fileName.matches("^Map.*\\.txt$")) {
					int number = Integer.parseInt(fileName.substring(3,
							fileName.length() - 4));
					return label + " [" + number + "]";
				} else {
					return label + " [" + fileName + "]";
				}
			}
		});
		addMapNamer(new MapNamer<File>() {

			@Override
			public String getName() {
				return "[Number] Label";
			}

			@Override
			public String getDescription() {
				return "Use the names of the maps as provided in the map "
						+ "list of the Assembla project preceded by the number "
						+ "of the map (the \"Xxx\" in \"MapXxx.txt\"). It "
						+ "requires an Internet connection to load the last "
						+ "version of the list.";
			}

			@Override
			public String getNameFor(File file) {
				String fileName = file.getName();
				String label = cache.getProperty(CACHE_LABEL_PREFIX + fileName);

				if (label == null) {
					return "[" + fileName + "]";
				} else if (fileName.matches("^Map.*\\.txt$")) {
					int number = Integer.parseInt(fileName.substring(3,
							fileName.length() - 4));
					return "[" + number + "] " + label;
				} else {
					return "[" + fileName + "] " + label;
				}
			}
		});

		addFeature(new SimpleFeature("Source",
				"Configure the source where to load the maps' labels from.") {

			@Override
			public void run() {
				String source = Editor.config.getProperty(CONFIG_LABEL_SOURCE);
				Object answer = JOptionPane
						.showInputDialog(
								null,
								"Please provide the location of the page describing the labels (URL or local file):",
								"Label Source", JOptionPane.QUESTION_MESSAGE,
								null, null, source);
				if (answer == null || source.equals(answer)) {
					// no change requested
				} else if (((String) answer).isEmpty()) {
					displayError("An empty location is of no use, so the change is cancelled.");
				} else {
					logger.info("Label source set: " + answer);
					Editor.config.setProperty(CONFIG_LABEL_SOURCE,
							answer.toString());
					Editor.config.setProperty(CONFIG_LABEL_LAST_UPDATE, "" + 0);
				}
			}
		});
		addFeature(new SimpleFeature("Update",
				"Request the update of the labels from the label source.") {

			@Override
			public void run() {
				try {
					cache.clear();
					loadLabels(true);
					// TODO update list panel
				} catch (Exception e) {
					e.printStackTrace();
					String message = e.getMessage() != null ? e.getMessage()
							: "An error occurred ("
									+ e.getClass().getSimpleName()
									+ "). Please read the logs.";
					displayError(message);
				}
			}
		});

		addEntryFilter(new EntryFilter<VHEntry>() {

			@Override
			public String getName() {
				return VHMap.MARKED_AS_UNTRANSLATED.getName();
			}

			@Override
			public String getDescription() {
				return "Search for entries marked with #UNTRANSLATED.";
			}

			@Override
			public boolean isRelevant(VHEntry entry) {
				return entry.getMetadata().get(VHMap.MARKED_AS_UNTRANSLATED);
			}

			@Override
			public String toString() {
				return getName();
			}
		});
		addEntryFilter(new NoTranslationFilter<VHEntry>());
	}

	private static List<File> retrieveFiles(File directory) {
		File[] f = directory.listFiles(new FileFilter() {

			@Override
			public boolean accept(File file) {
				return file.isFile() && file.length() > 0;
			}

		});

		if (f == null) {
			throw new RuntimeException("Impossible to retrieve the files from "
					+ directory);
		} else {
			LinkedList<File> files = new LinkedList<>();
			for (File file : f) {
				files.add(new File(file.getPath()));
			}
			return files;
		}
	}

	private void loadLabels(boolean force) {
		long lastUpdate = Long.parseLong(Editor.config.getProperty(
				CONFIG_LABEL_LAST_UPDATE, "0"));
		if (!force && System.currentTimeMillis() < lastUpdate + 86400000) {
			// not old enough
		} else {
			String source = Editor.config.getProperty(CONFIG_LABEL_SOURCE);
			URL url;
			try {
				url = new URL(source);
			} catch (MalformedURLException e) {
				try {
					url = new File(source).toURI().toURL();
				} catch (MalformedURLException e1) {
					throw new RuntimeException("Malformed URL: " + source, e);
				}
			}
			try {
				loadLabelsFrom(url);
			} finally {
				Editor.config.setProperty(CONFIG_LABEL_LAST_UPDATE,
						"" + System.currentTimeMillis());
			}
		}
	}

	private void loadLabelsFrom(URL pageUrl) {
		logger.info("Loading page from " + pageUrl + "...");
		String pageContent;
		try {
			URLConnection connection = pageUrl.openConnection();
			Pattern pattern = Pattern
					.compile("text/html;\\s+charset=([^\\s]+)\\s*");
			Matcher matcher = pattern.matcher(connection.getContentType());
			String charset = matcher.matches() ? matcher.group(1) : "UTF-8";
			Reader reader = new InputStreamReader(connection.getInputStream(),
					charset);
			StringBuilder buffer = new StringBuilder();
			int ch;
			while ((ch = reader.read()) >= 0) {
				buffer.append((char) ch);
			}
			pageContent = buffer.toString();
			reader.close();
		} catch (Exception e) {
			throw new RuntimeException("Impossible to read the source "
					+ pageUrl, e);
		}

		logger.info("Parsing content...");
		MapLabelPage mapLabelPage = new MapLabelPage();
		try {
			mapLabelPage.setContent(pageContent);
		} catch (ParsingException e) {
			throw new RuntimeException("Impossible to find map labels in "
					+ pageUrl, e);
		}
		logger.info("Content parsed.");

		logger.info("Saving labels...");
		MapTable table = mapLabelPage.getTable();
		int total = 0;
		for (MapRow row : table) {
			String name = "Map" + row.getId() + ".txt";
			String label = row.getEnglishLabel();
			String key = CACHE_LABEL_PREFIX + name;
			if (!label.matches("[^a-zA-Z]*[a-zA-Z]+.*")) {
				logger.finest("- " + name + " = " + label + " (ignored)");
				cache.remove(key);
			} else {
				logger.finest("- " + name + " = " + label);
				cache.setProperty(key, label);
				total++;
			}
		}
		cache.save();
		logger.info("Labels saved: " + total);
	}

	private void displayError(String message) {
		JOptionPane.showOptionDialog(null, message, "Loading Failed",
				JOptionPane.PLAIN_MESSAGE, JOptionPane.QUESTION_MESSAGE, null,
				new Object[] { "OK" }, "OK");
	}
}
