/*
 * GeometryTokenizer.java
 *
 * PostGIS extension for PostgreSQL JDBC driver - geometry model
 *
 * (C) 2015 Phillip Ross, phillip.w.g.ross@gmail.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 */

package io.github.sebasbaumh.postgis;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Tokenizer for {@link String}s.
 */
@NonNullByDefault
public final class GeometryTokenizer
{

	/**
	 * Tokenize the given {@link String}.
	 * @param string {@link String}
	 * @param delimiter delimiter character
	 * @return {@link List} of {@link String} tokens
	 */
	public static List<String> tokenize(String string, char delimiter)
	{
		List<String> tokens = new ArrayList<>();
		Stack<Character> stack = new Stack<>();
		int consumed = 0;
		for (int position = 0; position < string.length(); position++)
		{
			char character = string.charAt(position);
			if ((character == '(') || (character == '['))
			{
				stack.push(character);
			}
			else if (((character == ')') && (stack.peek() == '(')) || ((character == ']') && (stack.peek() == '[')))
			{
				stack.pop();
			}
			if ((character == delimiter) && stack.isEmpty())
			{
				tokens.add(string.substring(consumed, position));
				consumed = position + 1;
			}
		}
		if (consumed < string.length())
		{
			tokens.add(string.substring(consumed));
		}
		return tokens;
	}

}