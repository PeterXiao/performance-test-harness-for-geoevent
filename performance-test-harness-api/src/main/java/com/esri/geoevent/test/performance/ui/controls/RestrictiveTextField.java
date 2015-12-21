/*
  Copyright 1995-2015 Esri

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

  For additional information, contact:
  Environmental Systems Research Institute, Inc.
  Attn: Contracts Dept
  380 New York Street
  Redlands, California, USA 92373

  email: contracts@esri.com
 */
package com.esri.geoevent.test.performance.ui.controls;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TextField;

/**
 * A text field, which restricts the user's input.
 * <p>
 * The restriction can either be a maximal number of characters which the user is allowed to input or a regular
 * expression class, which contains allowed characters.
 * </p>
 * 
 * <b>Sample, which restricts the input to maximal 10 numeric characters</b>:
 * 
 * <pre>
 * {
 * 	&#064;code
 * 	RestrictiveTextField textField = new RestrictiveTextField();
 * 	textField.setMaxLength(10);
 * 	textField.setRestrict(&quot;[0-9]&quot;);
 * }
 * </pre>
 */
public class RestrictiveTextField extends TextField
{

	private IntegerProperty	maxLength	= new SimpleIntegerProperty(this, "maxLength", -1);
	private StringProperty	restrict	= new SimpleStringProperty(this, "restrict");

	public RestrictiveTextField()
	{

		textProperty().addListener(new ChangeListener<String>()
			{
				private boolean	ignore;

				@Override
				public void changed(ObservableValue<? extends String> observableValue, String s, String s1)
				{
					if (ignore || s1 == null)
						return;
					if (maxLength.get() > -1 && s1.length() > maxLength.get())
					{
						ignore = true;
						setText(s1.substring(0, maxLength.get()));
						ignore = false;
					}

					if (restrict.get() != null && !restrict.get().equals("") && !s1.matches(restrict.get() + "*"))
					{
						ignore = true;
						setText(s);
						ignore = false;
					}
				}
			});
	}

	/**
	 * The max length property.
	 *
	 * @return The max length property.
	 */
	public IntegerProperty maxLengthProperty()
	{
		return maxLength;
	}

	/**
	 * Gets the max length of the text field.
	 *
	 * @return The max length.
	 */
	public int getMaxLength()
	{
		return maxLength.get();
	}

	/**
	 * Sets the max length of the text field.
	 *
	 * @param maxLength
	 *          The max length.
	 */
	public void setMaxLength(int maxLength)
	{
		this.maxLength.set(maxLength);
	}

	/**
	 * The restrict property.
	 *
	 * @return The restrict property.
	 */
	public StringProperty restrictProperty()
	{
		return restrict;
	}

	/**
	 * Gets a regular expression character class which restricts the user input.
	 * 
	 *
	 * @return The regular expression.
	 * @see #getRestrict()
	 */
	public String getRestrict()
	{
		return restrict.get();
	}

	/**
	 * Sets a regular expression character class which restricts the user input.
	 * 
	 * E.g. [0-9] only allows numeric values.
	 *
	 * @param restrict
	 *          The regular expression.
	 */
	public void setRestrict(String restrict)
	{
		this.restrict.set(restrict);
	}
}
