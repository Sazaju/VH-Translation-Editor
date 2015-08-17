package fr.sazaju.vheditor;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Comparator;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import fr.sazaju.vheditor.VHMap.VHEntry;
import fr.vergne.translation.TranslationMap;
import fr.vergne.translation.TranslationMapTest;
import fr.vergne.translation.TranslationMetadata.Field;

public class VHMapTest extends TranslationMapTest<VHEntry> {

	private final File testFolder = new File("src/test/resources");

	@Override
	protected TranslationMap<VHEntry> createTranslationMap() {
		try {
			File templateFile = new File(testFolder, "map.txt");
			File mapFile = File.createTempFile("map", ".txt");
			FileUtils.copyFile(templateFile, mapFile);
			return new VHMap(mapFile);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	protected <T> T createNewEditableFieldValue(Field<T> field, T currentValue) {
		if (field == VHMap.MARKED_AS_UNTRANSLATED) {
			return (T) (Boolean) !((Boolean) currentValue);
		} else {
			throw new RuntimeException("The field " + field
					+ " is not supposed to be editable");
		}
	}

	@Test
	public void testProperlyLoadVHFile() throws IOException {
		File file = File.createTempFile("Map", ".txt");
		PrintStream stream = new PrintStream(file);
		stream.println("# RPGMAKER TRANS PATCH FILE VERSION 2.0");

		stream.println("# TEXT STRING");
		stream.println("# CONTEXT : Dialogue/Message/FaceUnknown");
		stream.println("# ADVICE : 49 char limit (35 if face)");
		stream.println("\\N[0]");
		stream.println("「…」");
		stream.println("# TRANSLATION ");
		stream.println("\\N[0]");
		stream.println("「...」");
		stream.println("# END STRING");

		stream.println();

		stream.println("# TEXT STRING");
		stream.println("# UNTRANSLATED");
		stream.println("# CONTEXT : Custom");
		stream.println("# ADVICE : 49 char limit");
		stream.println("\\N[0]");
		stream.println("「人の気配がしない…」");
		stream.println("# TRANSLATION ");
		stream.println("");
		stream.println("# END STRING");

		stream.close();

		VHMap map = new VHMap(file);
		assertEquals(2, map.size());

		assertEquals("\\N[0]\n「…」", map.getEntry(0).getOriginalContent());
		assertEquals("\\N[0]\n「...」", map.getEntry(0).getCurrentTranslation());
		assertEquals(false,
				map.getEntry(0).getMetadata()
						.get(VHMap.MARKED_AS_UNTRANSLATED));
		assertEquals((Integer) 35,
				map.getEntry(0).getMetadata().get(VHMap.CHAR_LIMIT_FACE));
		assertEquals((Integer) 49,
				map.getEntry(0).getMetadata().get(VHMap.CHAR_LIMIT_NO_FACE));
		assertEquals("Dialogue/Message/FaceUnknown", map.getEntry(0)
				.getMetadata().get(VHMap.CONTEXT));

		assertEquals("\\N[0]\n「人の気配がしない…」", map.getEntry(1)
				.getOriginalContent());
		assertEquals("", map.getEntry(1).getCurrentTranslation());
		assertEquals(true,
				map.getEntry(1).getMetadata()
						.get(VHMap.MARKED_AS_UNTRANSLATED));
		assertEquals(null,
				map.getEntry(1).getMetadata().get(VHMap.CHAR_LIMIT_FACE));
		assertEquals((Integer) 49,
				map.getEntry(1).getMetadata().get(VHMap.CHAR_LIMIT_NO_FACE));
		assertEquals("Custom", map.getEntry(1).getMetadata()
				.get(VHMap.CONTEXT));
	}

	@Test
	public void testReadWriteMap() throws IOException {
		File mapFolder = new File("VH/branches/working/");
		File[] listFiles = mapFolder.listFiles();
		Arrays.sort(listFiles, new Comparator<File>() {
			@Override
			public int compare(File f1, File f2) {
				return f1.getName().compareToIgnoreCase(f2.getName());
			}
		});
		int mapCounter = 0;
		int overflowCounter = 0;
		for (File originalFile : listFiles) {
			if (originalFile.isFile()) {
				mapCounter++;
				System.out.println("Testing " + originalFile.getName() + "...");
				File writtenFile = File.createTempFile(originalFile.getName(),
						".txt");
				FileUtils.copyFile(originalFile, writtenFile);
				final long firstModified = writtenFile.lastModified();

				String originalContent = FileUtils
						.readFileToString(originalFile);
				int numberEntries = originalContent.replaceAll("[ß]", "")
						.replaceAll("# TEXT STRING", "ß")
						.replaceAll("[^ß]", "").length();

				try {
					VHMap map = new VHMap(writtenFile);
					assertEquals(writtenFile.toString(), numberEntries,
							map.size() + map.unusedSize());
					map.saveAll();
					assertTrue("It seems the file " + writtenFile
							+ " has not been rewritten",
							writtenFile.lastModified() > firstModified);
				} catch (StackOverflowError e) {
					overflowCounter++;
					System.out.println("OVERFLOW " + overflowCounter);
					continue;
				}

				String readContent = FileUtils.readFileToString(originalFile);
				String writtenContent = FileUtils.readFileToString(writtenFile);
				assertEquals(originalFile.getName(), readContent,
						writtenContent);

				writtenFile.deleteOnExit();
				writtenFile.delete();
			} else {
				continue;
			}
		}
		assertEquals("TOTAL OVERFLOW: " + overflowCounter + "/" + mapCounter,
				0, overflowCounter);
	}

}
