package com.ingr3.ingr3;

import java.applet.Applet;
import java.awt.Button;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

//event listener for the url submit button
public class SubmitButton extends Applet implements ActionListener
{
	Button submit = new Button("Submit url");
  	TextField text = new TextField(20);
	public void init()
  {
  	
  	this.add(submit);
  	this.add(text);
  	submit.addActionListener(this);
  }
	
	@Override
	public void actionPerformed(ActionEvent e) 
	{
		FoodItem item = new FoodItem(text.getText());
		text.setText(App.formJsonForGraph(item));
		
	}
	
}