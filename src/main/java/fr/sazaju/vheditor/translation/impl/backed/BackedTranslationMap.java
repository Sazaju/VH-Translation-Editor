package fr.sazaju.vheditor.translation.impl.backed;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import org.apache.commons.io.FileUtils;

import fr.sazaju.vheditor.translation.TranslationEntry;
import fr.sazaju.vheditor.translation.TranslationMap;
import fr.vergne.parsing.layer.impl.GreedyMode;
import fr.vergne.parsing.layer.impl.Suite;
import fr.vergne.parsing.layer.impl.base.Option;

public class BackedTranslationMap extends Suite implements TranslationMap {

	private File file;

	public BackedTranslationMap(File file) throws IOException {
		this();
		setBaseFile(file);
	}

	public BackedTranslationMap() {
		super(new MapHeader(), new EntryLoop(), new Option<Suite>(new Suite(
				new UnusedTransLine(), new EntryLoop()), GreedyMode.POSSESSIVE));
	}

	@SuppressWarnings("serial")
	public static class EmptyMapException extends IllegalStateException {
		public EmptyMapException(File file) {
			super("The map provided (" + file + ") is empty.");
		}
	}

	@Override
	public Iterator<? extends TranslationEntry> iteratorUsed() {
		EntryLoop usedEntries = get(1);
		return usedEntries.iterator();
	}

	@Override
	public Iterator<? extends TranslationEntry> iteratorUnused() {
		Option<Suite> option = get(2);
		if (option.isPresent()) {
			EntryLoop unusedEntries = option.getOption().get(1);
			return unusedEntries.iterator();
		} else {
			return new LinkedList<MapEntry>().iterator();
		}
	}

	/**
	 * This method set the base file of this {@link BackedTranslationMap}. If it
	 * is different from the previous base file, the file is parsed and the
	 * content of this map updated correspondingly. Otherwise, nothing change.
	 * If you want to reset changes made on this map, use the {@link #reset()}
	 * method.
	 * 
	 * @param file
	 *            the map file to parse
	 * @throws IOException
	 */
	public void setBaseFile(File file) throws IOException {
		if (file.equals(this.file)) {
			// do not reparse it
		} else {
			String content = FileUtils.readFileToString(file);
			if (content.trim().isEmpty()) {
				throw new EmptyMapException(file);
			} else {
				setContent(content);
				this.file = file;
			}
		}
	}

	@Override
	public File getBaseFile() {
		return file;
	}

	/**
	 * Cancel all the modifications made on this map. The base file is parsed in
	 * order to retrieve all the original values.
	 * 
	 * @throws IOException
	 */
	public void reset() throws IOException {
		setContent(FileUtils.readFileToString(file));
	}

	@Override
	public TranslationEntry getUsedEntry(int index) {
		EntryLoop usedEntries = get(1);
		return usedEntries.get(index);
	}

	@Override
	public TranslationEntry getUnusedEntry(int index) {
		Option<Suite> option = get(2);
		if (option.isPresent()) {
			EntryLoop unusedEntries = option.getOption().get(1);
			return unusedEntries.get(index);
		} else {
			throw new NoSuchElementException(
					"This map does not have unused entries.");
		}
	}

	@Override
	public int sizeUsed() {
		EntryLoop usedEntries = get(1);
		return usedEntries.size();
	}

	@Override
	public int sizeUnused() {
		Option<Suite> option = get(2);
		if (option.isPresent()) {
			EntryLoop unusedEntries = option.getOption().get(1);
			return unusedEntries.size();
		} else {
			return 0;
		}
	}

	@Override
	public void save() {
		try {
			FileUtils.write(getBaseFile(), getContent());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
