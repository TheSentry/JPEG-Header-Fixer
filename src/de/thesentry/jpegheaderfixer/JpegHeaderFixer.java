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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;

/**
 * @author Florian Mittag
 */
public class JpegHeaderFixer {
	
	
	public static final String COPYRIGHT_YEAR = "2013";

	public static final String COPYRIGHT_NAME = "Florian Mittag";
	
	public static final String PROGRAM_NAME = "JpegHeaderFixer.jar";

	public static final String VERSION_STRING = "0.1.0";

	
	public static final byte[] BYTES_START_OF_IMAGE = new byte[]{(byte)0xFF, (byte)0xD8};
	public static final byte[] BYTES_END_OF_IMAGE = new byte[]{(byte)0xFF, (byte)0xD9};
	public static final byte[] BYTES_APP1_MARKER = new byte[]{(byte)0xFF, (byte)0xE1};
	public static final byte[] BYTES_DEFINE_QUANTIZATION_TABLE = new byte[]{(byte)0xFF, (byte)0xDB};
	
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
		
		
		// Open input file in read-only mode
		RandomAccessFile raf = new RandomAccessFile(inFile, "r");
		
		/* Check for JPEG header
		 * 
		 * FF D8 FF E1  xx xx 45 78  69 66 00 00  yy yy yy yy
		 * 
		 * FF D8    Start of Image (SOI) marker
		 * FF E1    APP1 Marker
		 * xx xx    APP1 length (this is the problem we're going to fix)
		 * 45 78    "Ex"
		 * 69 66    "if" => Exif
		 * 00 00
		 * yy yy    APP1 body
		 */

		int app1length;
		
		assertBytes(raf, BYTES_START_OF_IMAGE, "No JPEG header (SOI Marker)");
		assertBytes(raf, BYTES_APP1_MARKER, "No APP1 Marker");
		app1length = raf.readUnsignedShort();
		assertBytes(raf, new byte[]{(byte)0x45, (byte)0x78, (byte)0x69, (byte)0x66}, "No Exif marker");
		assertBytes(raf, new byte[]{(byte)0x00, (byte)0x00}, "No 00 00 padding");
		System.out.println(raf.getFilePointer());
		
		System.out.println("Header structure seems fine");
		System.out.println("APP1 segment length according to header: " + app1length);
		
		
		/*
		 * Now check for the end of the APP1 segment and other SOI markers
		 */
		
		
		byte[] twoBytes = new byte[2];
		while( raf.read(twoBytes) >= 0 ) {
			if( Arrays.equals(twoBytes, BYTES_END_OF_IMAGE)) {
				System.out.println("End of image (EOI) marker found at " + (raf.getFilePointer() - 2) );
			} else if( Arrays.equals(twoBytes, BYTES_START_OF_IMAGE)) {
				System.out.println("Start of image (SOI) marker found at " + (raf.getFilePointer() - 2) );
			} else if( Arrays.equals(twoBytes, BYTES_DEFINE_QUANTIZATION_TABLE)) {
				System.out.println("Define Quantization Table (DQT) marker found at " + (raf.getFilePointer() - 2) );
			}
		}
		
		
		// Check output file for writability
		if( outFile.exists() ) {
			printErrorAndExit("Output file '" + outFile.toString() + "' already exists");
		} else if( !dryRun ) {
			if( !outFile.createNewFile() ) {
				printErrorAndExit("Output file '" + outFile.toString() + "' is not writable");
			}
		}

	}
	
	
	/**
	 * Reads the next {@code expected.length} bytes from the {@code RandomAccessFile}
	 * and compares then to the expected values. If now equal, print the message
	 * and exit.
	 * 
	 * @param raf
	 * @param expected
	 * @param msg
	 * @return
	 * @throws IOException 
	 */
	public static boolean assertBytes(RandomAccessFile raf, byte[] expected, String msg) throws IOException {
		byte[] b = new byte[expected.length];
		int bytesRead = raf.read(b);
		
		if( bytesRead == expected.length && Arrays.equals(expected, b) ) {
			return true;
		}

		System.out.println(msg);
		System.out.println("This tool cannot handle the problem with this JPEG file");
		System.exit(2);
		return false;
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

	
	/*
	 * Usage and help output methods
	 * 
	 ***************************************************************************/

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
