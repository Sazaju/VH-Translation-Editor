package fr.sazaju.vheditor.translation.parsing;

import java.util.LinkedList;
import java.util.List;

import fr.vergne.parsing.layer.standard.GreedyMode;
import fr.vergne.parsing.layer.standard.Loop;

public class EntryLoop extends Loop<MapEntry> {

	public EntryLoop() {
		super(new Generator<MapEntry>() {

			@Override
			public MapEntry generates() {
				return new MapEntry();
			}
		});
		setMode(GreedyMode.POSSESSIVE);
	}

	public List<MapEntry> toList() {
		List<MapEntry> list = new LinkedList<MapEntry>();
		for (MapEntry mapEntry : this) {
			list.add(mapEntry);
		}
		return list;
	}

}
