package fr.sazaju.vheditor.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import fr.sazaju.vheditor.translation.parsing.BackedTranslationMap;
import fr.sazaju.vheditor.translation.parsing.MapEntry;
import fr.vergne.parsing.layer.Layer;

public class GuiBuilder {

	private final List<JComponent> components = new LinkedList<JComponent>();

	public GuiBuilder clear() {
		components.clear();
		return this;
	}

	public JPanel instantiate() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		panel.setAlignmentX(JPanel.LEFT_ALIGNMENT);

		panel.setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.anchor = GridBagConstraints.LINE_START;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.weightx = 1;

		for (Component component : components) {
			panel.add(component, constraints);
		}
		return panel;
	}

	public GuiBuilder setWithMap(BackedTranslationMap map) {
		clear();

		addConstantText(removeLastNewLine(map.getHeader()));
		for (MapEntry entry : map.getUsedEntries()) {
			addCompleteEntry(entry);
		}

		if (map.hasUnusedEntries()) {
			Color oldBackground = getBackground();
			setBackground(Color.MAGENTA);

			addConstantText(removeLastNewLine(map.getUnusedEntriesSeparator()));
			for (MapEntry entry : map.getUnusedEntries()) {
				addCompleteEntry(entry);
			}

			setBackground(oldBackground);
		} else {
			// no unused part
		}

		return this;
	}

	public GuiBuilder addCompleteEntry(MapEntry mapEntry) {
		addConstantText(removeLastNewLine(mapEntry.getStartFlag()));
		addOptionalConstantText(new UntranslatedTag(mapEntry));
		addConstantText(removeLastNewLine(mapEntry.getContextLine()));
		if (mapEntry.hasAdvice()) {
			addConstantText(removeLastNewLine(mapEntry.getAdvice()));
		} else {
			// no advice to add
		}
		addConstantText(mapEntry.getOriginalVersion(), true);
		addConstantText(removeLastNewLine(mapEntry.getTranslationFlag()));
		addEditableText(new TranslationArea(mapEntry));
		addConstantText(removeLastNewLine(mapEntry.getEndFlag()));

		return this;
	}

	private String removeLastNewLine(Layer layer) {
		return layer.getContent().replaceFirst("\n$", "");
	}

	private Color background = null;

	public GuiBuilder setBackground(Color color) {
		background = color;
		return this;
	}

	public Color getBackground() {
		return background;
	}

	public GuiBuilder addConstantText(String content) {
		return addConstantText(content, false);
	}

	public GuiBuilder addConstantText(String content, boolean isHighlighted) {
		JTextArea lines = new JTextArea(content);
		if (isHighlighted) {
			lines.setOpaque(true);
		} else {
			lines.setOpaque(false);
		}
		lines.setEditable(false);
		addAndSet(lines);
		return this;
	}

	public GuiBuilder addEditableText(TranslationArea translationArea) {
		// FIXME use (String content, XxxListener listener)
		addAndSet(translationArea);
		return this;
	}

	public GuiBuilder addOptionalConstantText(UntranslatedTag tag) {
		// FIXME use (String content, OptionListener listener)
		addAndSet(tag);
		return this;
	}

	public static interface OptionListener {
		public void isActivated();

		public void isDeactivated();
	}

	private JComponent addAndSet(JComponent component) {
		components.add(component);
		if (background == null) {
			// keep default background
		} else {
			component.setOpaque(true);
			component.setBackground(background);
		}
		return component;
	}

}
