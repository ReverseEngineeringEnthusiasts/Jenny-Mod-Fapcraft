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

public class DebugWindow2 extends JFrame {
   private JPanel c;
   static DebugWindow2 b;
   public static boolean a = true;

   public static void a_clash451() {
      EventQueue.invokeLater(() -> {
         try {
            b = new DebugWindow2();
            b.setVisible(true);
            b.requestFocus();
         } catch (Exception var1) {
            var1.printStackTrace();
         }
      });
   }

   public DebugWindow2() {
      this.setResizable(false);
      this.setBounds(100, 100, 600, 260);
      this.c = new JPanel();
      this.c.setBorder(new EmptyBorder(5, 5, 5, 5));
      this.c.setLayout(new BorderLayout(0, 0));
      this.setContentPane(this.c);
      JPanel var1 = new JPanel();
      this.c.add(var1, "North");
      JTextPane var2 = new JTextPane();
      var2.setFont(new Font("Tahoma", 0, 16));
      var2.setBackground(SystemColor.control);
      var2.setText(I18n.func_135052_a("window.pornwarning.title", new Object[0]));
      var1.add(var2);
      JPanel var3 = new JPanel();
      this.c.add(var3, "South");
      JCheckBox var4 = new JCheckBox(I18n.func_135052_a("window.pornwarning.dontaskagain", new Object[0]));
      var3.add(var4);
      JButton var5 = new JButton(I18n.func_135052_a("window.pornwarning.am18", new Object[0]));
      var5.addActionListener(var1x -> {
         a = false;
         if (var4.isSelected()) {
            File var2x = new File("sexmod");
            var2x.mkdir();
            File var3x = new File("sexmod/dontAskAgain");

            try {
               var3x.createNewFile();
            } catch (IOException var5x) {
               var5x.printStackTrace();
            }
         }

         b.dispose();
      });
      var3.add(var5);
      JButton var6 = new JButton(I18n.func_135052_a("window.pornwarning.not18", new Object[0]));
      var6.addActionListener(var0 -> {
         a = false;
         System.out.println("MINOR!!! WHEOO WOOO WHEEE WHOOO WHEEE WHOO");
         File var1x = new File("sexmod");

         try {
            FileUtils.deleteDirectory(var1x);
         } catch (IOException var5x) {
            var5x.printStackTrace();
         }

         File var2x = new File("mods/youCanJustDeleteMe.bat");

         try {
            FileWriter var3x = new FileWriter(var2x);
            var3x.write("@echo off\n");
            var3x.write("TIMEOUT /T 5\n");
            var3x.write("DEL \"mods\\*sexmod*.jar\"\n");
            var3x.write("exit 0");
            var3x.close();
            Runtime.getRuntime().exec("cmd /c start " + var2x.getPath());
         } catch (IOException var4x) {
            var4x.printStackTrace();
         }

         FMLCommonHandler.instance().exitJava(0, true);
      });
      var3.add(var6);
      JPanel var7 = new JPanel();
      this.c.add(var7, "Center");
      var7.setLayout(new BoxLayout(var7, 0));
      JTextPane var8 = new JTextPane();
      var8.setContentType("text/html");
      var8.setBackground(SystemColor.control);
      var8.setEditable(false);
      var8.setText("<html><center><p style='font-family: Tahoma'>" + I18n.func_135052_a("window.pornwarning.text", new Object[0]) + "</p></center></html> ");
      var7.add(var8);
   }
}
