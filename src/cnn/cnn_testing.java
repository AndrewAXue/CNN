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

public class cnn_testing {
	JFrame window = new JFrame();
	
	static Scanner scanner;
	
	Path path = Paths.get("cifar-10-binary/data_batch_1.bin");
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
	    new cnn_testing().go();
	}
	
	public double [][][] get_input(){
		double input[][][] = new double[3][32][32];
		
		
		for (int i=0;i<32;i++){
			for (int k=0;k<32;k++){
				input[0][i][k] = image_lst[display_image_ind].red[i]/255.0;
			}
		}
		for (int i=0;i<32;i++){
			for (int k=0;k<32;k++){
				input[1][i][k] = image_lst[display_image_ind].green[i]/255.0;
			}
		}
		for (int i=0;i<32;i++){
			for (int k=0;k<32;k++){
				input[2][i][k] = image_lst[display_image_ind].blue[i]/255.0;
			}
		}
		return input;
	}
	
	int batch_size = 100;
	
	public void go(){
		/*
		int arr[] = {16*16,30,10};
		neural_net full_connect = new neural_net(arr);
		full_connect.initialize_values();
		
		read_data();
		//after pooling
		//W2 = (W1-F)/S+1
	
		int data_depths[] = {3,4,4,1};
		int data_width_heights[] = {32,32,16,16};
		String layer_types[] = {"conv","pool","conv"};
		int layer_depths[] = {4,1,1};
		int layer_width_heights[] = {3,2,3};
		cnn please = new cnn(data_depths,data_width_heights,layer_types,layer_depths,layer_width_heights);
		
		please.batch_size = batch_size;
		full_connect.batch = batch_size;
		
		while(display_image_ind<50000){
			//System.out.println(display_image_ind);
			int correct = 0;
			for (int i=0;i<batch_size;i++){
				//System.out.println(i);
				double input[][][] = get_input();
				please.feedforward(input);
				double cnn_results[][] = please.cnn_get_output();
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
				if (max_ind==image_lst[display_image_ind].image_ind)correct++;
				
				full_connect.backpropagate();
				
				double first_error[] = full_connect.get_first_error();
				please.cnn_back_propagate(first_error);
				display_image_ind++;
			}
			System.out.println(display_image_ind+" "+correct);
			correct=0;
			full_connect.gradient_descent(batch_size);
			please.cnn_gradient_descent();
			
		}
		
		
		System.out.println("DONE");
		
		
		//please.print_values();
		 * */
		 
		/*
		
		int data_depths[] = {3,2};
		int data_width_heights[] = {5,3};
		String layer_types[] = {"conv"};
		int layer_depths[] = {2};
		int layer_width_heights[] = {3};
		cnn please = new cnn(data_depths,data_width_heights,layer_types,layer_depths,layer_width_heights);
		
		double input_data[][][] = {
				{{2,2,1,0,2},{2,1,0,2,0},{2,0,1,2,0},{0,1,2,0,1},{2,2,1,1,2}},
				{{0,1,2,1,1},{2,1,2,2,1},{1,1,1,0,0},{1,2,1,1,0},{1,1,2,0,2}},
				{{2,1,1,2,1},{2,0,1,2,1},{1,2,1,2,2},{2,1,1,0,1},{1,0,1,1,0}}
		};
		
		please.feedforward(input_data);
		
		//please.cnn_back_propagate();
		//please.print_values();
		//please.print_weights();
		 
		*/
		/*
		int data_depths[] = {3,2,2,1};
		int data_width_heights[] = {5,3,2,2};
		String layer_types[] = {"conv","pool","conv"};
		int layer_depths[] = {2,1,1};
		int layer_width_heights[] = {3,2,2};
		cnn please = new cnn(data_depths,data_width_heights,layer_types,layer_depths,layer_width_heights);
		
		double input_data[][][] = {
				{{2,2,1,0,2},{2,1,0,2,0},{2,0,1,2,0},{0,1,2,0,1},{2,2,1,1,2}},
				{{0,1,2,1,1},{2,1,2,2,1},{1,1,1,0,0},{1,2,1,1,0},{1,1,2,0,2}},
				{{2,1,1,2,1},{2,0,1,2,1},{1,2,1,2,2},{2,1,1,0,1},{1,0,1,1,0}}
		};
		
		please.feedforward(input_data);
		
		double back_prop_error[] = {-1,2,4,0};
		please.cnn_back_propagate(back_prop_error);
		//please.print_values();
		//please.print_weights();
		//please.print_weight_dev();
		//please.print_xdev();
		 */
		int data_depths[] = {3,2,2,1};
		int data_width_heights[] = {4,4,2,2};
		String layer_types[] = {"conv","pool","conv"};
		
		int conv_constant_wid_hid = 3;
		
		int pool_constant_depth = 1;
		int pool_constant_wid_hid = 2;
		
		int layer_depths[] = {2,pool_constant_depth,1};
		int layer_width_heights[] = {conv_constant_wid_hid,pool_constant_wid_hid,conv_constant_wid_hid};
		cnn please = new cnn(data_depths,data_width_heights,layer_types,layer_depths,layer_width_heights);
		
		double input_data[][][] = {
				{{2,2,1,0},{2,1,0,2},{2,0,1,2},{0,1,2,0}},
				{{0,1,2,1},{2,1,2,2},{1,1,1,0},{1,2,1,1}},
				{{2,1,1,2},{2,0,1,2},{1,2,1,2},{2,1,1,0}}
		};
		
		please.feedforward(input_data);
		
		double back_prop_error[] = {-1,2,4,0};
		please.cnn_back_propagate(back_prop_error);
		//please.print_values();
		//please.print_weights();
		//please.print_weight_dev();
		//please.print_xdev();
		
		 
		
		
	}
	
}
