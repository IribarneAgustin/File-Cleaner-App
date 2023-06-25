package app.cleaner;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class FileDeletionApp extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private List<String> paths;
	private static File configFile;
	private DefaultListModel<String> listModel;
	private JList<String> pathList;

	public FileDeletionApp() {

		paths = new ArrayList<>();
		configFile = new File("config.txt");
		initGUI();
		loadPathsFromConfig();
	}

	private void initGUI() {

		JPanel contentPane = new JPanel(new BorderLayout());
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("File Cleaner");

		//Components
		listModel = new DefaultListModel<>();
		pathList = new JList<>(listModel);
		JLabel label = new JLabel("Enter path:");
		final JTextField textField = new JTextField(20);
		JButton addButton = new JButton("Add Path");
		JButton deleteButton = new JButton("Clean");
		JButton deleteSelectedButton = new JButton("Remove Path");

		// Add Path action
		addButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String path = textField.getText();
				if ((path.contains("Desktop") || path.contains("Escritorio")) && !isPathAlreadySaved(path)) {
					paths.add(path);
					listModel.addElement(path);
					savePathsToConfig(path);
				} else {
					JOptionPane.showMessageDialog(FileDeletionApp.this,
							"Please enter a path that contains 'Desktop' or 'Escritorio' and check if it does not already exist.",
							"Invalid Path", JOptionPane.ERROR_MESSAGE);
				}
				textField.setText("");
			}
		});

		// Delete Files action
		deleteButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Integer result = deleteFilesInPaths();
				JOptionPane.showMessageDialog(FileDeletionApp.this, result + " files deleted",
						"Files deletion", JOptionPane.INFORMATION_MESSAGE);
			}
		});

		//Delete path action
		deleteSelectedButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int selectedIndex = pathList.getSelectedIndex();
				if (selectedIndex != -1) {
					String selectedPath = listModel.getElementAt(selectedIndex);
					deletePath(selectedPath);
					listModel.removeElementAt(selectedIndex);
					removePathFromConfig(selectedPath);
				} else {
					JOptionPane.showMessageDialog(FileDeletionApp.this, "Please select a path to delete.",
							"No Path Selected", JOptionPane.WARNING_MESSAGE);
				}
			}
		});

		// Layout setup
		JPanel inputPanel = new JPanel();
		inputPanel.add(label);
		inputPanel.add(textField);
		inputPanel.add(addButton);
		inputPanel.add(deleteSelectedButton);
		inputPanel.add(deleteButton);

		contentPane.add(inputPanel, BorderLayout.NORTH);
		contentPane.add(new JScrollPane(pathList), BorderLayout.CENTER);

		setContentPane(contentPane);
		pack();
		setLocationRelativeTo(null);
	}

	public static void savePathsToConfig(String content) {
		try {

			FileWriter writer = new FileWriter(configFile, true);
			writer.write(content + System.lineSeparator()); // add a new line separator after each path
			writer.close();
			System.out.println("Path saved to configFile.txt successfully.");

		} catch (IOException e) {
			System.out.println("An error occurred while writing to configFile.txt: " + e.getMessage());
		}
	}

	public boolean isPathAlreadySaved(String path) {
		boolean response = false;
		try {
			FileReader fileReader = new FileReader(configFile);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			String line;

			while ((line = bufferedReader.readLine()) != null) {
				if (line.equalsIgnoreCase(path)) {
					bufferedReader.close();
					response = true;
				}
			}

			bufferedReader.close();
		} catch (IOException e) {
			System.out.println("An error occurred while reading the config file: " + e.getMessage());
		}

		return response;
	}

	public Integer deleteFilesInPaths() {
		Integer count = 0;
		try {
			FileReader fileReader = new FileReader(configFile);
			BufferedReader bufferedReader = new BufferedReader(fileReader);

			String path;
			while ((path = bufferedReader.readLine()) != null) {
				File directory = new File(path);
				File[] files = directory.listFiles();

				if (files != null) {
					for (File file : files) {
						if (file.isFile()) {
							file.delete();
							count++;
							System.out.println("Deleted file: " + file.getAbsolutePath());
						}
					}
				}
			}
			bufferedReader.close();
		} catch (IOException e) {
			System.out.println("An error occurred while reading the config file: " + e.getMessage());
		}
		
		return count;
	}

	public void loadPathsFromConfig() {
		try {
			FileReader fileReader = new FileReader(configFile);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			String path;
			while ((path = bufferedReader.readLine()) != null) {
				paths.add(path);
				listModel.addElement(path);
			}

			bufferedReader.close();
		} catch (IOException e) {
			System.out.println("An error occurred while reading the config file: " + e.getMessage());
		}
	}

	private void deletePath(String path) {
		File directory = new File(path);
		File[] files = directory.listFiles();

		if (files != null) {
			for (File file : files) {
				if (file.isFile()) {
					file.delete();
					System.out.println("Deleted file: " + file.getAbsolutePath());
				}
			}
		}
	}

	private void removePathFromConfig(String path) {
		try {
			File tempFile = new File("temp.txt");
			FileReader fileReader = new FileReader(configFile);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			FileWriter writer = new FileWriter(tempFile, true);

			String line;
			while ((line = bufferedReader.readLine()) != null) {
				if (!line.equals(path)) {
					writer.write(line + System.lineSeparator());
				}
			}

			bufferedReader.close();
			writer.close();

			if (configFile.delete()) {
				if (!tempFile.renameTo(configFile)) {
					System.out.println("Failed to rename temporary file.");
				}
			} else {
				System.out.println("Failed to delete the original config file.");
			}
		} catch (IOException e) {
			System.out.println("An error occurred while removing the path from config file: " + e.getMessage());
		}
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				FileDeletionApp app = new FileDeletionApp();
				app.setVisible(true);
			}
		});
	}

}