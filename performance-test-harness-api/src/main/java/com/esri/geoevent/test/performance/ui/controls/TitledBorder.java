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

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

public class TitledBorder extends StackPane
{
	private Label			titleLabel	= new Label();
	private StackPane	contentPane	= new StackPane();
	private Node			content;

	public void setContent(Node content)
	{
		content.getStyleClass().add("bordered-titled-content");
		contentPane.getChildren().add(content);
	}

	public Node getContent()
	{
		return content;
	}

	public void setTitle(String title)
	{
		titleLabel.setText(" " + title + " ");
	}

	public String getTitle()
	{
		return titleLabel.getText();
	}

	public TitledBorder()
	{
		titleLabel.setText("default title");
		titleLabel.getStyleClass().add("bordered-titled-title");
		StackPane.setAlignment(titleLabel, Pos.TOP_CENTER);

		getStyleClass().add("bordered-titled-border");
		getChildren().addAll(titleLabel, contentPane);
	}

}
