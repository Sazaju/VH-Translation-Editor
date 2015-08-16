package fr.sazaju.vheditor.map;

import fr.vergne.parsing.layer.standard.Atom;
import fr.vergne.parsing.layer.standard.Suite;
import fr.vergne.parsing.layer.util.Newline;

public class UnusedTransLine extends Suite {

	public UnusedTransLine() {
		super(new Atom("# UNUSED TRANSLATABLES"), new Newline());
	}
}
