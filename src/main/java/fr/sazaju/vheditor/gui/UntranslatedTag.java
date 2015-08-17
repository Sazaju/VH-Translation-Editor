package fr.sazaju.vheditor.gui;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

import fr.sazaju.vheditor.VHMap;
import fr.vergne.translation.TranslationEntry;
import fr.vergne.translation.TranslationMetadata;
import fr.vergne.translation.TranslationMetadata.Field;
import fr.vergne.translation.TranslationMetadata.FieldListener;

@SuppressWarnings("serial")
public class UntranslatedTag<Entry extends TranslationEntry<?>> extends JPanel {

	private final TranslationMetadata metadata;

	public UntranslatedTag(final TranslationMetadata metadata) {
		this.metadata = metadata;

		final JLabel tag = new JLabel(getTagLabel());
		final JButton toggleButton = new JButton();
		toggleButton.setAction(new AbstractAction(getButtonLabel()) {

			@Override
			public void actionPerformed(ActionEvent e) {
				Boolean current = metadata.get(VHMap.MARKED_AS_UNTRANSLATED);
				metadata.set(VHMap.MARKED_AS_UNTRANSLATED, !current);

				tag.setText(getTagLabel());
				toggleButton.setText(getButtonLabel());
			}
		});

		metadata.addFieldListener(new FieldListener() {

			@Override
			public <T> void fieldUpdated(Field<T> field, T newValue) {
				if (field == VHMap.MARKED_AS_UNTRANSLATED) {
					tag.setText(getTagLabel());
					toggleButton.setText(getButtonLabel());
				} else {
					// unmanaged field, ignore
				}
			}
			
			@Override
			public <T> void fieldStored(Field<T> field) {
				// ignored
			}
		});

		setLayout(new FlowLayout(FlowLayout.LEADING, 0, 0));
		setOpaque(false);
		add(tag);
		add(new JLabel(" "));
		add(toggleButton);

		/*
		 * In order to avoid the button making the line bigger, its size should
		 * be reduced. If we directly set a reduced size, its content is
		 * generally replaced by "..." because the insets don't allow such a
		 * reduced size. To fix that, we first minimize the insets by setting an
		 * EmptyBorder, then set a real border to have a normal rendering.
		 */
		toggleButton.setBorder(new EmptyBorder(0, 0, 0, 0));
		toggleButton.setBorder(new EtchedBorder());
		// now we can reduce the size of the button
		int length = tag.getFontMetrics(tag.getFont()).getHeight();
		toggleButton.setPreferredSize(new Dimension(length, length));
	}

	public boolean isModified() {
		return metadata.get(VHMap.MARKED_AS_UNTRANSLATED) != metadata
				.getStored(VHMap.MARKED_AS_UNTRANSLATED);
	}

	private String getTagLabel() {
		return metadata.get(VHMap.MARKED_AS_UNTRANSLATED) ? "# UNTRANSLATED"
				: "<html><s># UNTRANSLATED</s></html>";
	}

	private String getButtonLabel() {
		return metadata.get(VHMap.MARKED_AS_UNTRANSLATED) ? "-" : "+";
	}
}
