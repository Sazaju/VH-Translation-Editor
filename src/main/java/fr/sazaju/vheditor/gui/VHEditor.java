package fr.sazaju.vheditor.gui;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import fr.sazaju.vheditor.VHMap;
import fr.sazaju.vheditor.VHMap.VHEntry;
import fr.sazaju.vheditor.VHProject;
import fr.sazaju.vheditor.gui.VHGuiBuilder.MapPanel;
import fr.vergne.translation.TranslationMap;
import fr.vergne.translation.editor.Editor;
import fr.vergne.translation.editor.content.MapComponentFactory;
import fr.vergne.translation.util.ProjectLoader;

@SuppressWarnings("serial")
public class VHEditor extends Editor<File, VHEntry, VHMap, VHProject> {

	private static final Logger logger = Logger.getLogger(VHEditor.class
			.getName());

	public VHEditor() {
		super(new ProjectLoader<VHProject>() {

			@Override
			public VHProject load(File directory) {
				return new VHProject(directory);
			}
		}, new MapComponentFactory<MapPanel>() {

			@Override
			public MapPanel createMapComponent(TranslationMap<?> map) {
				return (MapPanel) VHGuiBuilder
						.instantiateMapComponent((VHMap) map);
			}

		});
	}

	public static void main(String[] args) {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		PrintStream printer = new PrintStream(stream);
		printer.println(".level = INFO");
		printer.println("java.level = OFF");
		printer.println("javax.level = OFF");
		printer.println("sun.level = OFF");

		printer.println("handlers = java.util.logging.FileHandler, java.util.logging.ConsoleHandler");

		printer.println("java.util.logging.FileHandler.pattern = vh-editor.%u.%g.log");
		printer.println("java.util.logging.FileHandler.level = ALL");
		printer.println("java.util.logging.FileHandler.formatter = fr.vergne.logging.OneLineFormatter");

		printer.println("java.util.logging.ConsoleHandler.level = ALL");
		printer.println("java.util.logging.ConsoleHandler.formatter = fr.vergne.logging.OneLineFormatter");

		File file = new File("logging.properties");
		if (file.exists()) {
			try {
				printer.println(FileUtils.readFileToString(file));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			// use only default configuration
		}
		printer.close();

		LogManager manager = LogManager.getLogManager();
		try {
			manager.readConfiguration(IOUtils.toInputStream(new String(stream
					.toByteArray(), Charset.forName("UTF-8"))));
		} catch (SecurityException | IOException e) {
			throw new RuntimeException(e);
		}

		new Thread(new Runnable() {
			public void run() {
				try {
					new VHEditor().setVisible(true);
				} catch (Exception e) {
					logger.log(Level.SEVERE, null, e);
				}
			}
		}).start();
	}
}
