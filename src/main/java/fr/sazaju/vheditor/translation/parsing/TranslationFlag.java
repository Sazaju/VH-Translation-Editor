package fr.sazaju.vheditor.translation.parsing;

import fr.vergne.parsing.layer.standard.Atom;
import fr.vergne.parsing.layer.standard.Suite;
import fr.vergne.parsing.layer.util.Newline;

public class TranslationFlag extends Suite {

	public TranslationFlag() {
		super(new Atom("# TRANSLATION "), new Newline());
	}
}
