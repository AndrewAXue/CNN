package cnn;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Scanner;

import javax.swing.JComponent;
import javax.swing.JFrame;


public class draw_image {
	JFrame window = new JFrame();
	static Scanner scanner;
	
	Path path = Paths.get("cifar-10-binary/data_batch_1.bin");
	byte[] data;
	
	int read_ind = 0;
	int display_image_ind = 0;
	
	image image_lst[] = new image[50000];
	public class image{
		int red[] = new int [1024];
		int green[] = new int [1024];
		int blue[] = new int [1024];
	}
	
	public void read_data(){
		int read_ind = 1;
		int image_ind = 0;
		for (int i=0;i<10000;i++){
			image_lst[i] =  new image();
			for (int z=0;z<3;z++){
				for (int k=0;k<1024;k++){
					int input = data[read_ind];
					byte b = (byte) input;
					//System.out.println(b); // -22
					int i2 = b & 0xFF;
					if (z==0)image_lst[image_ind].red[k] = i2;
					else if (z==1)image_lst[image_ind].green[k] = i2;
					else image_lst[image_ind].blue[k] = i2;
					read_ind++;
				}
			}
			read_ind++;
			image_ind++;
		}
	}
	
	public static void main(String[] args) throws IOException {
		new draw_image().go();
	}
	
	void go(){
		try {
			data = Files.readAllBytes(path);
		}
		catch(Exception e) {
	         // if any I/O error occurs
			e.printStackTrace();
	    }
		System.out.println("\nDONE");
		read_data();
		// Opens a default window and adds graphics
		window.setSize(100, 100);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setVisible(true);
		window.setResizable(false);
		window.add(new PewGrid());
		window.repaint();
		window.addMouseListener(new mouseevent());
		//move();
	}
	
	private class PewGrid extends JComponent {
		public void paintComponent(Graphics g){
			Graphics2D grap = (Graphics2D) g;
			grap.setColor(Color.WHITE);
			grap.fillRect(0, 0, 1000,1000);//setting up black background
			grap.setFont(new Font("Arial Black", Font.BOLD, 30));

			
			for (int i=0;i<1024;i++){
				grap.setColor(new Color(image_lst[display_image_ind].red[i],image_lst[display_image_ind].green[i],image_lst[display_image_ind].blue[i]));
				grap.fillRect(i%32, i/32, 1, 1);
			}
		}
	}
	
	private class mouseevent implements MouseListener{
		public void mouseClicked(MouseEvent e) {
			// Scans in the next line in the csv and repeats the process above
			display_image_ind++;
			window.repaint();
		}
		public void mouseEntered(MouseEvent e) {}
		public void mouseExited(MouseEvent e) {}
		public void mousePressed(MouseEvent e) {}
		public void mouseReleased(MouseEvent e) {}
	}
}
