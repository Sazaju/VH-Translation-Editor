package fr.sazaju.vheditor.html;

import java.util.Iterator;

import fr.vergne.parsing.layer.standard.Atom;
import fr.vergne.parsing.layer.standard.Loop;
import fr.vergne.parsing.layer.standard.Loop.Generator;
import fr.vergne.parsing.layer.standard.Quantifier;
import fr.vergne.parsing.layer.standard.Suite;

public class MapTable extends Suite implements Iterable<MapRow> {

	public MapTable() {
		super(new Atom("<table><tbody>"), new NewlineLoop(), new MapRow(),
				new NewlineLoop(), new Loop<MapRow>(Quantifier.POSSESSIVE,
						new Generator<MapRow>() {

							@Override
							public MapRow generates() {
								return new MapRow();
							}

						}), new Atom("</tbody></table>"));
	}

	private Loop<MapRow> getRowLoop() {
		return this.<Loop<MapRow>> get(4);
	}

	public int size() {
		return getRowLoop().size();
	}

	@Override
	public Iterator<MapRow> iterator() {
		return getRowLoop().iterator();
	}
}
