package cnn;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
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



public class cnn_testing_actual {
	JFrame window = new JFrame();
	cnn please;
	neural_net full_connect;
	
	boolean auto = true;
	
	static Scanner scanner;
	Path path;
	byte[] data;
	
	int read_ind = 1;
	int display_image_ind = 0;
	
	image image_lst[] = new image[50000];
	public class image{
		int image_ind;
		int red[] = new int [1024];
		int green[] = new int [1024];
		int blue[] = new int [1024];
	}
	
	public void read_data(){
		try {
			int read_ind = 0;
			for (int n=0;n<=4;n++){
				path = Paths.get("cifar-10-binary/data_batch_"+(n+1)+".bin");
				data = Files.readAllBytes(path);
				for (int i=0;i<10000;i++){
					image_lst[n*10000+i] =  new image();
					image_lst[n*10000+i].image_ind = data[read_ind];
					//System.out.println(data[read_ind]);
					read_ind++;
					for (int z=0;z<3;z++){
						for (int k=0;k<1024;k++){
							int input = data[read_ind];
							byte b = (byte) input;
							//System.out.println(b); // -22
							int i2 = b & 0xFF;
							if (z==0)image_lst[n*10000+i].red[k] = i2;
							else if (z==1)image_lst[n*10000+i].green[k] = i2;
							else image_lst[n*10000+i].blue[k] = i2;
							read_ind++;
						}
					}
				}
				read_ind = 0;
			}
		}
		catch(Exception e) {
	         // if any I/O error occurs
			e.printStackTrace();
	    }		
	}
	
	
	public static void main(String[] args) throws IOException {
	    new cnn_testing_actual().go();
	}
	
	public double [][][] get_input(){
		double input[][][] = new double[3][32][32];
		
		
		for (int i=0;i<32;i++){
			for (int k=0;k<32;k++){
				input[0][i][k] = image_lst[display_image_ind].red[i*32+k]/255.0;
			}
		}
		for (int i=0;i<32;i++){
			for (int k=0;k<32;k++){
				input[1][i][k] = image_lst[display_image_ind].green[i*32+k]/255.0;
			}
		}
		for (int i=0;i<32;i++){
			for (int k=0;k<32;k++){
				input[2][i][k] = image_lst[display_image_ind].blue[i*32+k]/255.0;
			}
		}
		return input;
	}
	
	int batch_size = 100;
	
	public void go(){
		
		
		//int arr[] = {16*16,30,10};
		int arr[] = {16*16*6,30,10};
		full_connect = new neural_net(arr);
		full_connect.learning_rate = 1;
		full_connect.quadratic = false;
		full_connect.softmax = true;
		full_connect.print = false;
		full_connect.maxnodes = 6;
		full_connect.num_epoch = 1;
		full_connect.train_batch_size = 1;
		full_connect.test_batch_size = 1;
		full_connect.initialize_values();
		//full_connect.create_window();
		
		read_data();
		//after pooling
		//W2 = (W1-F)/S+1
	
		window.setSize(100, 100);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setVisible(true);
		window.setResizable(false);
		window.add(new PewGrid());
		window.repaint();
		//window.addMouseListener(new mouseevent());
		
		int data_depths[] = {3,6,6};
		int data_width_heights[] = {32,32,16};
		String layer_types[] = {"conv","pool"};
		int layer_depths[] = {6,1};
		int layer_width_heights[] = {3,2};
		
		/*
		int data_depths[] = {3,6,6,1};
		int data_width_heights[] = {32,32,16,16};
		String layer_types[] = {"conv","pool","conv"};
		int layer_depths[] = {6,1,1};
		int layer_width_heights[] = {3,2,3};
		*/
		//int data_depths[] = {3,1};
		//int data_width_heights[] = {32,32};
		//String layer_types[] = {"conv"};
		//int layer_depths[] = {1};
		//int layer_width_heights[] = {3};
		please = new cnn(data_depths,data_width_heights,layer_types,layer_depths,layer_width_heights);
		please.window.addMouseListener(new mouseevent());
		
		please.batch_size = batch_size;
		full_connect.batch = batch_size;
		
		double learning_rate = 2;
		
		System.out.println("learning rate "+learning_rate);
		
		if (auto){
			for (int a=0;a<2;a++){
				display_image_ind = 0;
				while(display_image_ind<50000){
					//System.out.println(display_image_ind);
					int correct = 0;
					for (int i=0;i<batch_size;i++){
						double input[][][] = get_input();
						please.feedforward(input);
						double cnn_results[][][] = please.cnn_get_output();
						
						double expected[] = {0,0,0,0,0,0,0,0,0,0};
						expected[image_lst[display_image_ind].image_ind]=1;
						full_connect.feed_and_set_expected(cnn_results, expected);
						
						double produced[] = full_connect.getoutput();
						int max_ind = -1;
						for (int k=0;k<produced.length;k++){
							if (max_ind==-1||produced[max_ind]<produced[k]){
								max_ind = k;
							}
						}
						//System.out.println("Guessed "+max_ind+" ans "+image_lst[display_image_ind].image_ind);
						
						if (max_ind==image_lst[display_image_ind].image_ind)correct++;
						
						full_connect.backpropagate();
						
						double first_error[] = full_connect.get_first_error();
						please.cnn_back_propagate(first_error);
						display_image_ind++;
						full_connect.gradient_descent(learning_rate,100);
						please.cnn_gradient_descent(learning_rate,100);
					}
					System.out.println("epoch "+a+" image ind "+display_image_ind+" "+correct);
					correct=0;
					//full_connect.gradient_descent(1,batch_size);
					//please.cnn_gradient_descent(1,batch_size);
				}
			}
		}
		
					
	}
	
	private class PewGrid extends JComponent {
		public void paintComponent(Graphics g){
			Graphics2D grap = (Graphics2D) g;
			grap.setColor(Color.WHITE);
			grap.fillRect(0, 0, 1000,1000);//setting up black background
			grap.setFont(new Font("Arial Black", Font.BOLD, 30));

			if (display_image_ind!=0){
				display_image_ind--;
				double[][][] input = get_input();
				display_image_ind++;
				for (int a=0;a<32;a++){
					for (int b=0;b<32;b++){
						grap.setColor(new Color((int)(input[0][a][b]*255),(int)(input[1][a][b]*255),(int)(input[2][a][b]*255)));
						grap.fillRect(b, a, 1, 1);
					}
				}
			}
			
			
		}
	}
	
	private class mouseevent implements MouseListener{
		public void mouseClicked(MouseEvent e) {
			batch_size = 1;
			// Scans in the next line in the csv and repeats the process above
			Point point = e.getPoint();
			if (point.getX()<100){
				double input[][][] = get_input();				
				please.feedforward(input);
				/*
				for (int a=0;a<32;a++){
					for (int b=0;b<32;b++){
						System.out.print(please.data[1][0][a][b].x_value+" ");
					}
					System.out.println('\n');
				}
				*/
				display_image_ind++;
				
				double cnn_results[][] = please.cnn_get_output();
				/*
				for (int a=0;a<cnn_results.length;a++){
					for (int b=0;b<cnn_results[0].length;b++){
						System.out.print(cnn_results[a][b]+" ");
					}
					System.out.println();
				}
				System.out.println('\n');
*/				
				double expected[] = {0,0,0,0,0,0,0,0,0,0};
				expected[image_lst[display_image_ind].image_ind]=1;
				full_connect.feed_and_set_expected(cnn_results, expected);
			
				double produced[] = full_connect.getoutput();
				int max_ind = -1;
				for (int k=0;k<produced.length;k++){
					if (max_ind==-1||produced[max_ind]<produced[k]){
						max_ind = k;
					}
				}
				System.out.println("Guessed "+max_ind+" actual "+image_lst[display_image_ind].image_ind);
			}
			if (point.getY()<100){
				full_connect.backpropagate();
				
				double first_error[] = full_connect.get_first_error();
				please.cnn_back_propagate(first_error);
				display_image_ind++;
			}
			if (point.getX()>800){
				full_connect.gradient_descent(0.1,batch_size);
				please.cnn_gradient_descent(0.1,batch_size);
				//full_connect.cleardev();
			}
			please.window.repaint();
			please.weights_window.repaint();
			full_connect.window.repaint();
			window.repaint();
		}
		public void mouseEntered(MouseEvent e) {}
		public void mouseExited(MouseEvent e) {}
		public void mousePressed(MouseEvent e) {}
		public void mouseReleased(MouseEvent e) {}
	}
	
}
