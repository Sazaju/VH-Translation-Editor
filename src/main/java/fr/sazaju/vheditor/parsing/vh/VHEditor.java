package fr.sazaju.vheditor.parsing.vh;

import java.io.File;

import fr.sazaju.vheditor.gui.Editor;
import fr.sazaju.vheditor.gui.content.MapComponentFactory;
import fr.sazaju.vheditor.parsing.vh.gui.VHGuiBuilder;
import fr.sazaju.vheditor.parsing.vh.gui.VHGuiBuilder.MapPanel;
import fr.sazaju.vheditor.parsing.vh.map.VHEntry;
import fr.sazaju.vheditor.parsing.vh.map.VHMap;
import fr.sazaju.vheditor.translation.TranslationMap;
import fr.sazaju.vheditor.util.ProjectLoader;

@SuppressWarnings("serial")
public class VHEditor extends Editor<File, VHEntry, VHMap, VHProject> {

	public VHEditor() {
		super(new ProjectLoader<VHProject>() {

			@Override
			public VHProject load(File directory) {
				return new VHProject(directory);
			}
		}, new MapComponentFactory<MapPanel>() {

			@Override
			public MapPanel createMapComponent(TranslationMap<?> map) {
				return (MapPanel) VHGuiBuilder.instantiateMapGui((VHMap) map);
			}

		});
	}

	public static void main(String[] args) {
		new Runnable() {
			public void run() {
				new VHEditor().setVisible(true);
			}
		}.run();
	}
}
