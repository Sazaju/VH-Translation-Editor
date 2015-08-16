package fr.sazaju.vheditor.html;

import fr.vergne.parsing.layer.standard.GreedyMode;
import fr.vergne.parsing.layer.standard.Loop;
import fr.vergne.parsing.layer.util.Newline;

public class NewlineLoop extends Loop<Newline> {

	public NewlineLoop() {
		super(new Generator<Newline>() {
			@Override
			public Newline generates() {
				return new Newline();
			}
		});
		setMode(GreedyMode.POSSESSIVE);
	}
}
