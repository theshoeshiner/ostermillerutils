/*
 * An InputStreamReader that does no character encoding translations.
 * Copyright (C) 2001-2010 Stephen Ostermiller
 * http://ostermiller.org/contact.pl?regarding=Java+Utilities
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * See COPYING.TXT for details.
 */

package com.Ostermiller.util;

import java.io.*;

/**
 * A StraightStreamReader is a bridge from byte streams to character streams: It reads bytes
 * and translates them into characters without using a character encoding.  The characters
 * that a StraightStreamReader returns may not be valid Unicode characters but they are
 * guaranteed to be in the 0x00 to 0xFF range.
 * More information about this class is available from <a target="_top" href=
 * "http://ostermiller.org/utils/StraightStreamReader.html">ostermiller.org</a>.
 * <P>
 * Most of the time you want to do character encoding translation when translating bytes to
 * characters.  If you are planning on displaying the text, you should always do this and should
 * use an InputStreamReader for the purpose.  Sometimes it is useful to treat characters as bytes
 * with some extra bits.  In these cases you would want to use a StraightStreamReader.
 * <P>
 * For top efficiency, consider wrapping an StraightStreamReader within a BufferedReader. For example:<br>
 * <code>BufferedReader in = new BufferedReader(new StraightStreamReader(System.in));</code>
 *
 * @author Stephen Ostermiller http://ostermiller.org/contact.pl?regarding=Java+Utilities
 * @since ostermillerutils 1.00.00
 */
public class StraightStreamReader extends Reader{

	/**
	 * The input stream from which all methods in this class read.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	private InputStream in;

	/**
	 * A byte array to be used for calls to the InputStream.  This
	 * is cached as a class variable to avoid object creation and
	 * deletion each time a read is called.  This buffer may be
	 * null and may not be large enough.  Make sure to check i
	 * before using it.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	private byte[] buffer;

	/**
	 * Create a StraightStreamReader from an InputStream
	 *
	 * @param in InputStream to wrap a Reader around.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	public StraightStreamReader(InputStream in) {
		this.in = in;
	}

	/**
	 * Close the stream.
	 *
	 * @throws IOException If an I/O error occurs
	 *
	 * @since ostermillerutils 1.00.00
	 */
	@Override public void close() throws IOException {
		in.close();
	}

	/**
	 * Mark the present position in the stream. Subsequent calls to reset()
	 * will attempt to reposition the stream to this point. Not all
	 * character-input streams support the mark() operation.
	 *
	 * @param readAheadLimit Limit on the number of characters that may be read
	 *    while still preserving the mark. After reading this many characters,
	 *    attempting to reset the stream may fail.
	 * @throws IOException If the stream does not support mark(), or if some other I/O error occurs
	 *
	 * @since ostermillerutils 1.00.00
	 */
	@Override public void mark(int readAheadLimit) throws IOException {
		in.mark(readAheadLimit);
	}

	/**
	 * Tell whether this stream supports the mark() operation.
	 *
	 * @return true if and only if this stream supports the mark operation.
	 *
	 * @since ostermillerutils 1.00.00
	 */
	@Override public boolean markSupported(){
		return in.markSupported();
	}

	/**
	 * Read a single character. This method will block until a character is available, an
	 * I/O error occurs, or the end of the stream is reached.
	 *
	 * @return The character read, as an integer in the range 0 to 256 (0x00-0xff), or -1 if
	 *    the end of the stream has been reached
	 * @throws IOException If an I/O error occurs
	 *
	 * @since ostermillerutils 1.00.00
	 */
	@Override public int read() throws IOException {
		return in.read();
	}

	/**
	 * Read characters into an array. This method will block until some input is available,
	 * an I/O error occurs, or the end of the stream is reached.
	 *
	 * @param cbuf Destination buffer
	 * @return The number of bytes read, or -1 if the end of the stream has been reached
	 * @throws IOException If an I/O error occurs
	 *
	 * @since ostermillerutils 1.00.00
	 */
	@Override public int read(char[] cbuf) throws IOException {
		return read(cbuf, 0, cbuf.length);
	}

	/**
	 * Read characters into an array. This method will block until some input is available,
	 * an I/O error occurs, or the end of the stream is reached.
	 *
	 * @param cbuf Destination buffer
	 * @param off Offset at which to start storing characters
	 * @param len Maximum number of characters to read
	 * @return The number of bytes read, or -1 if the end of the stream has been reached
	 * @throws IOException If an I/O error occurs
	 *
	 * @since ostermillerutils 1.00.00
	 */
	@Override public int read(char[] cbuf, int off, int len) throws IOException {
		// ensure the capacity of the buffer that we will be using
		// to read from the input stream
		if (buffer == null || buffer.length < len){
			buffer = new byte[len];
		}
		// read from the input stream and copy it to the character array
		int length = in.read(buffer, 0, len);
		for (int i=0; i<length; i++){
			cbuf[off+i] = (char)(0xFF & buffer[i]);
		}
		return length;
	}

	/**
	 * Tell whether this stream is ready to be read.
	 *
	 * @return True if the next read() is guaranteed not to block for input, false otherwise.
	 *    Note that returning false does not guarantee that the next read will block.
	 * @throws IOException If an I/O error occurs
	 *
	 * @since ostermillerutils 1.00.00
	 */
	@Override public boolean ready() throws IOException {
		return (in.available() > 0);
	}

	/**
	 * Reset the stream. If the stream has been marked, then attempt to reposition it at the mark.
	 * If the stream has not been marked, then attempt to reset it in some way appropriate to the
	 * particular stream, for example by repositioning it to its starting point. Not all
	 * character-input streams support the reset() operation, and some support reset()
	 * without supporting mark().
	 *
	 * @throws IOException If the stream has not been marked, or if the mark has been invalidated,
	 *    or if the stream does not support reset(), or if some other I/O error occurs
	 *
	 * @since ostermillerutils 1.00.00
	 */
	@Override public void reset() throws IOException {
		in.reset();
	}

	/**
	 * Skip characters. This method will block until some characters are available,
	 * an I/O error occurs, or the end of the stream is reached.
	 *
	 * @param n The number of characters to skip
	 * @return The number of characters actually skipped
	 * @throws IllegalArgumentException If n is negative
	 * @throws IOException If an I/O error occurs
	 *
	 * @since ostermillerutils 1.00.00
	 */
	@Override public long skip(long n) throws IOException {
		return in.skip(n);
	}
}
