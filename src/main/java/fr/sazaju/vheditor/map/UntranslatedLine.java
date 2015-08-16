package fr.sazaju.vheditor.map;

import fr.vergne.parsing.layer.standard.Atom;
import fr.vergne.parsing.layer.standard.Suite;
import fr.vergne.parsing.layer.util.Newline;

public class UntranslatedLine extends Suite {

	public UntranslatedLine() {
		super(new Atom("# UNTRANSLATED"), new Newline());
	}
}
