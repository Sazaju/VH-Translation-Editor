package fr.sazaju.vheditor.translation.parsing;

import fr.vergne.parsing.layer.standard.Atom;
import fr.vergne.parsing.layer.standard.Suite;
import fr.vergne.parsing.layer.util.Newline;

public class UntranslatedFlag extends Suite {

	public UntranslatedFlag() {
		super(new Atom("# UNTRANSLATED"), new Newline());
	}
}
