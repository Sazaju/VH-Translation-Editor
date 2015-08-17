package fr.sazaju.vheditor.gui;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import fr.sazaju.vheditor.VHMap;
import fr.sazaju.vheditor.VHMap.VHEntry;
import fr.sazaju.vheditor.VHMap.VHMetadata;
import fr.vergne.translation.TranslationEntry;
import fr.vergne.translation.editor.content.EntryComponentFactory.EntryComponent;
import fr.vergne.translation.editor.content.MapComponentFactory.MapComponent;
import fr.vergne.translation.editor.content.TranslationArea;

public class VHGuiBuilder {

	private static final Color UNUSED_COLOR = Color.MAGENTA;

	public static MapPanel instantiateMapComponent(VHMap map) {
		MapPanel panel = new MapPanel();
		panel.setOpaque(false);
		panel.setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.weightx = 1;
		constraints.gridx = 0;
		constraints.gridy = GridBagConstraints.RELATIVE;

		panel.add(new JLabel("# RPGMAKER TRANS PATCH FILE VERSION 2.0"),
				constraints);
		int size = map.size() + map.unusedSize();
		for (int index = 0; index < size; index++) {
			VHEntry entry = map.getEntry(index);
			EntryPanel entryPanel = instantiateEntryComponent(entry);

			String extraText = map.getAfterEntry(index);
			addExtraLinesBasedOnText(entryPanel, extraText, constraints);

			panel.add(entryPanel, constraints);
		}
		return panel;
	}

	private static void addExtraLinesBasedOnText(EntryPanel entryPanel,
			String extraText, GridBagConstraints constraints) {
		extraText = extraText.replaceAll(
				"((\r\n)|(\n\r)|((?<!\n)\r(?!\n))|((?<!\r)\n(?!\r)))", "\n");
		extraText = "." + extraText + ".";
		LinkedList<String> tokens = new LinkedList<>(Arrays.asList(extraText
				.split("\n")));
		tokens.removeFirst();
		tokens.removeLast();
		for (String line : tokens) {
			if (line.isEmpty()) {
				line = " ";
			} else {
				// display line as is
			}
			entryPanel.add(new JLabel(line), constraints);
		}
	}

	private static EntryPanel instantiateEntryComponent(VHEntry entry) {
		VHMetadata metadata = entry.getMetadata();

		EntryPanel panel = new EntryPanel();
		if (entry.isUsed()) {
			panel.setOpaque(false);
		} else {
			panel.setOpaque(true);
			panel.setBackground(UNUSED_COLOR);
		}
		panel.setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.anchor = GridBagConstraints.LINE_START;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.weightx = 1;
		panel.add(new JLabel("# TEXT STRING"), constraints);
		panel.add(new UntranslatedTag<TranslationEntry<?>>(metadata),
				constraints);
		panel.add(new JLabel("# CONTEXT : " + metadata.get(VHMap.CONTEXT)),
				constraints);
		Integer limit1 = metadata.get(VHMap.CHAR_LIMIT_NO_FACE);
		Integer limit2 = metadata.get(VHMap.CHAR_LIMIT_FACE);
		if (limit1 == null) {
			/*
			 * We add an empty label to keep the same number of components,
			 * independently of the content of the entry, but this label should
			 * have a zero-size, so it does not appear.
			 */
			panel.add(new JLabel(), constraints);
		} else if (limit2 == null) {
			panel.add(new JLabel("# ADVICE : " + limit1 + " char limit"),
					constraints);
		} else {
			panel.add(new JLabel("# ADVICE : " + limit1 + " char limit ("
					+ limit2 + " if face)"), constraints);
		}
		JTextArea original = new JTextArea(entry.getOriginalContent());
		original.setEditable(false);
		panel.add(original, constraints);
		panel.add(new JLabel("# TRANSLATION"), constraints);
		Collection<Integer> limits = TranslationArea.retrieveLimits(entry,
				Arrays.asList(VHMap.CHAR_LIMIT_NO_FACE, VHMap.CHAR_LIMIT_FACE));
		panel.add(new TranslationArea(entry, limits), constraints);
		panel.add(new JLabel("# END STRING"), constraints);
		return panel;
	}

	@SuppressWarnings("serial")
	public static class EntryPanel extends JPanel implements EntryComponent {

		@Override
		public TranslationArea getTranslationComponent() {
			return (TranslationArea) getComponent(6);
		}
	}

	@SuppressWarnings("serial")
	public static class MapPanel extends JPanel implements MapComponent {

		@Override
		public EntryComponent getEntryComponent(int index) {
			return (EntryComponent) getComponent(index + 1);
		}
	}
}
