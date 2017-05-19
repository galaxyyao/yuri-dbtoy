package com.galaxyyao.yuri_dbtoy;

import javax.swing.SwingUtilities;

import com.galaxyyao.yuri_dbtoy.ui.GuiMain;

public class YuriDbToyApplication {
	public static void main(String[] args) {
		// Schedule a job for the event dispatch thread:
		// creating and showing this application's GUI.
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				// Turn off metal's use of bold fonts
				// UIManager.put("swing.boldMetal", Boolean.FALSE);
				GuiMain.createAndShowGUI();
			}
		});
	}
}
