package com.vh.translation.impl;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

import com.vh.translation.TranslationMap;

public class Main {

	public static void main(String[] args) throws IOException {
		ImmutableTranslationMap.logger.setLevel(Level.OFF);
		File mapFolder = new File("VH/branches/working/");
		File[] listFiles = mapFolder.listFiles();
		for (File file : listFiles) {
			// TODO consider all *.txt files
			if (file.isFile() && file.getName().startsWith("Map")) {
				TranslationMap translationMap = new ImmutableTranslationMap(
						file);
				System.out.println(file.getName() + ": " + translationMap);
			} else {
				continue;
			}
		}

	}
}