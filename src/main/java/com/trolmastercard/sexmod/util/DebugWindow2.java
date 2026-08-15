package com.trolmastercard.sexmod.util;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.SystemColor;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.border.EmptyBorder;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.apache.commons.io.FileUtils;

/**
 * Second developer debug overlay window (leftover, largely dormant).
 */
public class DebugWindow2 extends JFrame {
   private JPanel panel;
   static DebugWindow2 instance;
   public static boolean isVisible = true;

   public static void showDebugWindow() {
      EventQueue.invokeLater(() -> {
         try {
            instance = new DebugWindow2();
            instance.setVisible(true);
            instance.requestFocus();
         } catch (Exception exception) {
            exception.printStackTrace();
         }
      });
   }

   public DebugWindow2() {
      this.setResizable(false);
      this.setBounds(100, 100, 600, 260);
      this.panel = new JPanel();
      this.panel.setBorder(new EmptyBorder(5, 5, 5, 5));
      this.panel.setLayout(new BorderLayout(0, 0));
      this.setContentPane(this.panel);
      JPanel northPanel = new JPanel();
      this.panel.add(northPanel, "North");
      JTextPane titlePane = new JTextPane();
      titlePane.setFont(new Font("Tahoma", 0, 16));
      titlePane.setBackground(SystemColor.control);
      titlePane.setText(I18n.format("window.pornwarning.title", new Object[0]));
      northPanel.add(titlePane);
      JPanel southPanel = new JPanel();
      this.panel.add(southPanel, "South");
      JCheckBox dontAskCheckbox = new JCheckBox(I18n.format("window.pornwarning.dontaskagain", new Object[0]));
      southPanel.add(dontAskCheckbox);
      JButton am18Button = new JButton(I18n.format("window.pornwarning.am18", new Object[0]));
      am18Button.addActionListener(actionEvent -> {
         isVisible = false;
         if (dontAskCheckbox.isSelected()) {
            File sexmodDir = new File("sexmod");
            sexmodDir.mkdir();
            File dontAskFile = new File("sexmod/dontAskAgain");

            try {
               dontAskFile.createNewFile();
            } catch (IOException ioException) {
               ioException.printStackTrace();
            }
         }

         instance.dispose();
      });
      southPanel.add(am18Button);
      JButton not18Button = new JButton(I18n.format("window.pornwarning.not18", new Object[0]));
      not18Button.addActionListener(actionEvent -> {
         isVisible = false;
         System.out.println("MINOR!!! WHEOO WOOO WHEEE WHOOO WHEEE WHOO");
         File sexmodDir = new File("sexmod");

         try {
            FileUtils.deleteDirectory(sexmodDir);
         } catch (IOException ioException) {
            ioException.printStackTrace();
         }

         File deleteBat = new File("mods/youCanJustDeleteMe.bat");

         try {
            FileWriter writer = new FileWriter(deleteBat);
            writer.write("@echo off\n");
            writer.write("TIMEOUT /T 5\n");
            writer.write("DEL \"mods\\*sexmod*.jar\"\n");
            writer.write("exit 0");
            writer.close();
            Runtime.getRuntime().exec("cmd /c start " + deleteBat.getPath());
         } catch (IOException ioException) {
            ioException.printStackTrace();
         }

         FMLCommonHandler.instance().exitJava(0, true);
      });
      southPanel.add(not18Button);
      JPanel centerPanel = new JPanel();
      this.panel.add(centerPanel, "Center");
      centerPanel.setLayout(new BoxLayout(centerPanel, 0));
      JTextPane infoPane = new JTextPane();
      infoPane.setContentType("text/html");
      infoPane.setBackground(SystemColor.control);
      infoPane.setEditable(false);
      infoPane.setText("<html><center><p style='font-family: Tahoma'>" + I18n.format("window.pornwarning.text", new Object[0]) + "</p></center></html> ");
      centerPanel.add(infoPane);
   }
}
