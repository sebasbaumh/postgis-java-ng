package io.github.sebasbaumh.postgis.binary;

import java.io.ByteArrayOutputStream;

/**
 * Allows writing values to a byte array in little endian format.
 * @author Sebastian Baumhekel
 */
public class BinaryValueSetter extends ValueSetter
{
	private final ByteArrayOutputStream out = new ByteArrayOutputStream();

	/**
	 * Gets the written value.
	 * @return value
	 */
	public byte[] getValue()
	{
		return out.toByteArray();
	}

	@Override
	public void setByte(byte b)
	{
		out.write(b);
	}

	@Override
	public void setInt(int value)
	{
		out.write((value >>> 0) & 0xFF);
		out.write((value >>> 8) & 0xFF);
		out.write((value >>> 16) & 0xFF);
		out.write((value >>> 24) & 0xFF);
	}

	@Override
	public void setLong(long value)
	{
		out.write((int) ((value >>> 0) & 0xFF));
		out.write((int) ((value >>> 8) & 0xFF));
		out.write((int) ((value >>> 16) & 0xFF));
		out.write((int) ((value >>> 24) & 0xFF));
		out.write((int) ((value >>> 32) & 0xFF));
		out.write((int) ((value >>> 40) & 0xFF));
		out.write((int) ((value >>> 48) & 0xFF));
		out.write((int) ((value >>> 56) & 0xFF));
	}

}
