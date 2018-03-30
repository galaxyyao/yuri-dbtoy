package com.galaxyyao.yuri_dbtoy;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.galaxyyao.yuri_dbtoy.ui.MainPanel;

public class DbToolApplication {
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				UIManager.put("swing.boldMetal", Boolean.FALSE);
				MainPanel.createAndShowGUI();
			}
		});
	}
}
