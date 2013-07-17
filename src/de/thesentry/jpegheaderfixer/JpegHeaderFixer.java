/**
 *  JPEG-Header-Fixer, for fixing JPEGs with incorrect header created by the Samsung Galaxy S2
 *  Copyright (C) 2013  Florian Mittag
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.thesentry.jpegheaderfixer;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * @author Florian Mittag
 */
public class JpegHeaderFixer {
	
	
	public static final String COPYRIGHT_YEAR = "2013";

	public static final String COPYRIGHT_NAME = "Florian Mittag";
	
	public static final String PROGRAM_NAME = "JpegHeaderFixer.jar";

	public static final String VERSION_STRING = "0.1.0";

	
	/**
	 * @param args
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) {
		if( args == null || args.length == 0 ) {
			printUsageString();
		}
		
		if( args.length > 3 ) {
			printErrorAndExit("Too many arguments");
		}
		
		boolean dryRun = false;
		String inFileName = null;
		String outFileName = null;
		
		int argIdx = 0;

		// check if "-n" option was given
		if( args[argIdx].equalsIgnoreCase("-n") ) {
			dryRun = true;
			argIdx++;
		}
		
		// read the input filename 
		if( args.length > argIdx ) {
			inFileName = args[argIdx];
			argIdx++;
		}

		// read the output filename
		if( args.length > argIdx ) {
			outFileName = args[argIdx];
			argIdx++;
		}

		// check if an input file was given
		if( inFileName == null ) {
			printErrorAndExit("No input JPEG-file given");
		}
		
		File inFile = new File(inFileName);
		File outFile = null;
		
		if( outFileName != null ) {
			outFile = new File(outFileName);
		}
		
		try {
			fixJpegFile(inFile, outFile, dryRun);
		} catch (Exception e) {
			System.out.println("An unknown error has occured, please report this bug.");
			e.printStackTrace();
		}
	}
	
	
	public static void fixJpegFile(File inFile, File outFile, boolean dryRun) throws IOException  {
		
		// Check input file for readability
		if( !inFile.exists() ) {
			printErrorAndExit("Input file '" + inFile.toString() + "' doesn't exist");
		} else if( !inFile.canRead() ) {
			printErrorAndExit("Input file '" + inFile.toString() + "' can't be read");
		}
		
		// Check output file for writability
		if( outFile.exists() ) {
			printErrorAndExit("Output file '" + outFile.toString() + "' already exists");
		} else if( !outFile.canWrite() ) {
			printErrorAndExit("Output file '" + outFile.toString() + "' is not writable");
		}
		
		// Open input file in read-only mode
		RandomAccessFile raf = new RandomAccessFile(inFile, "r");
		
		// Check for JPEG header
		byte[] twobytes = new byte[2];
		int bytesRead = raf.read(twobytes);
		if( bytesRead != 2 ) {
			
		}
		
	}
	
	/*
	 * Copyright-related methods
	 * 
	 ***************************************************************************/
	
	protected static void printCopyrightHeader() {
		System.out.println(
			"JPEG-Header-Fixer v" + VERSION_STRING + ", for fixing JPEGs with incorrect header created by the Samsung Galaxy S2\n"
		+ "Copyright (C) " + COPYRIGHT_YEAR + "  " + COPYRIGHT_NAME + "\n"
		);
	}
	
	protected static void printCopyrightInfo() {
		printCopyrightHeader();
		System.out.println(
		  "This program is free software: you can redistribute it and/or modify\n"
		+ "it under the terms of the GNU General Public License as published by\n"
		+ "the Free Software Foundation, either version 3 of the License, or\n"
		+ "(at your option) any later version.\n"
		+ "\n"
		+ "This program is distributed in the hope that it will be useful,\n"
		+ "but WITHOUT ANY WARRANTY; without even the implied warranty of\n"
		+ "MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the\n"
		+ "GNU General Public License for more details.\n"
		+ "\n"
		+ "You should have received a copy of the GNU General Public License\n"
		);		
	}
	
	public static void printUsageString() {
		printCopyrightHeader();
		System.out.println("Usage: java -jar " + PROGRAM_NAME + " [-n] <corrupted JPEG-file> [<target JPEG-file>]");
		System.out.println();
		System.out.println("Options:");
		System.out.println("  -n       Dry run. Do not change anything, just report what would be done.");
	}
	
	public static void printErrorAndExit(String msg) {
		System.out.println("Error: " + msg);
		System.out.println();
		System.out.println("Run the program without arguments to see help information.");
		System.exit(1);
	}
}
