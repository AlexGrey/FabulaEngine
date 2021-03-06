package com.macbury.fabula.editor.tiles;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JSeparator;
import javax.swing.JTextField;

import com.badlogic.gdx.Gdx;
import com.macbury.fabula.editor.tiles.TilesetGenerator.TileGeneratorListener;
import com.macbury.fabula.manager.G;
import com.macbury.fabula.terrain.tileset.Tileset;

public class TilesetBuilderDialog extends JDialog implements ActionListener, TileGeneratorListener {
  private static final String TAG = "TilesetBuilderDialog";
  private JComboBox<String> tilesetComboBox;
  private JProgressBar progressBar;
  private JTextField autoTileTextField;
  private JTextField texturesTextField;
  private TilesetGenerator generator;
  private JButton btnSelectAutoTileDirectory;
  private JButton btnSelectTexturesDirectory;
  private JButton btnBuild;
  
  public TilesetBuilderDialog() {
    generator = new TilesetGenerator(this);
    setModalityType(ModalityType.APPLICATION_MODAL);
    setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    setResizable(false);
    setType(Type.POPUP);
    setTitle("Tile Builder");
    setBounds(100, 100, 753, 202);
    getContentPane().setLayout(null);
    
    this.tilesetComboBox = new JComboBox<String>();
    tilesetComboBox.setEditable(true);
    
    DefaultComboBoxModel<String> model = new DefaultComboBoxModel<String>();
    
    for (Tileset tileset : G.db.getTilesets()) {
      model.addElement(tileset.getName());
    }
    
    tilesetComboBox.setModel(model);
    tilesetComboBox.setBounds(75, 11, 662, 20);
    getContentPane().add(tilesetComboBox);
    
    JLabel lblNewLabel = new JLabel("Tilleset");
    lblNewLabel.setBounds(10, 14, 46, 14);
    getContentPane().add(lblNewLabel);
    
    this.btnBuild = new JButton("Build!");
    btnBuild.addActionListener(this);
    btnBuild.setBounds(10, 140, 727, 23);
    getContentPane().add(btnBuild);
    
    this.progressBar = new JProgressBar();
    progressBar.setBounds(75, 106, 626, 14);
    getContentPane().add(progressBar);
    
    JLabel lblNewLabel_1 = new JLabel("Autotiles");
    lblNewLabel_1.setBounds(10, 45, 46, 14);
    getContentPane().add(lblNewLabel_1);
    
    autoTileTextField = new JTextField();
    autoTileTextField.setBounds(75, 42, 626, 20);
    getContentPane().add(autoTileTextField);
    autoTileTextField.setColumns(10);
    autoTileTextField.setText(generator.getTempAbsolutePath("autotiles"));
    
    this.btnSelectAutoTileDirectory = new JButton("...");
    btnSelectAutoTileDirectory.addActionListener(this);
    btnSelectAutoTileDirectory.setBounds(711, 42, 26, 23);
    getContentPane().add(btnSelectAutoTileDirectory);
    
    this.btnSelectTexturesDirectory = new JButton("...");
    btnSelectTexturesDirectory.addActionListener(this);
    btnSelectTexturesDirectory.setBounds(711, 72, 26, 23);
    getContentPane().add(btnSelectTexturesDirectory);
    
    texturesTextField = new JTextField();
    texturesTextField.setColumns(10);
    texturesTextField.setBounds(75, 73, 626, 20);
    texturesTextField.setText(generator.getTempAbsolutePath("textures"));
    getContentPane().add(texturesTextField);
    
    JLabel lblTextures = new JLabel("Textures");
    lblTextures.setBounds(10, 79, 46, 14);
    getContentPane().add(lblTextures);
    
    JLabel lblProgress = new JLabel("Progress");
    lblProgress.setBounds(10, 106, 46, 14);
    getContentPane().add(lblProgress);
    
    JSeparator separator = new JSeparator();
    separator.setBounds(10, 131, 727, 2);
    getContentPane().add(separator);
    
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if (e.getSource() == btnSelectAutoTileDirectory || e.getSource() == btnSelectTexturesDirectory) {
      JTextField targetTextField = e.getSource() == btnSelectAutoTileDirectory ? autoTileTextField : texturesTextField;
      
      JFileChooser chooser = new JFileChooser(targetTextField.getText());
      chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
      Integer opt = chooser.showSaveDialog(this);
      if (opt == JFileChooser.APPROVE_OPTION) {
        targetTextField.setText(chooser.getSelectedFile().getAbsolutePath());
      }
    }
    
    if (e.getSource() == btnBuild) {
      build();
    }
  }

  private void build() {
    btnBuild.setEnabled(false);
    progressBar.setIndeterminate(true);
    Thread thread = new Thread(new Runnable() {
      @Override
      public void run() {
        Tileset tileset = G.db.getTileset((String)tilesetComboBox.getSelectedItem());
        generator.build(tileset, autoTileTextField.getText(), texturesTextField.getText());
      }
    });
    
    thread.start();
  }

  @Override
  public void onProgress(int progress, int max) {
    if (max == 0) {
      progressBar.setIndeterminate(true);
    } else {
      progressBar.setIndeterminate(false);
      progressBar.setMaximum(max);
      progressBar.setValue(progress);
    }
  }

  @Override
  public void onLog(String line) {
    Gdx.app.log(TAG, line);
  }

  @Override
  public void onFinish() {
    progressBar.setIndeterminate(false);
    progressBar.setMaximum(0);
    progressBar.setValue(0);
    btnBuild.setEnabled(true);
  }
}
