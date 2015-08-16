package fr.sazaju.vheditor.map;

import fr.sazaju.vheditor.map.VHEntry.MapSaver;
import fr.vergne.parsing.layer.standard.GreedyMode;
import fr.vergne.parsing.layer.standard.Loop;

public class EntryLoop extends Loop<VHEntry> {

	public EntryLoop(final MapSaver saver) {
		super(new Generator<VHEntry>() {

			@Override
			public VHEntry generates() {
				return new VHEntry(saver);
			}
		});
		setMode(GreedyMode.POSSESSIVE);
	}
}
