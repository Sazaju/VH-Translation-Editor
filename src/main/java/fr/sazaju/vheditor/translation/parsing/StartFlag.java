package fr.sazaju.vheditor.translation.parsing;

import fr.vergne.parsing.layer.standard.Atom;
import fr.vergne.parsing.layer.standard.Suite;
import fr.vergne.parsing.layer.util.Newline;

public class StartFlag extends Suite {

	public StartFlag() {
		super(new Atom("# TEXT STRING"), new Newline());
	}
}
