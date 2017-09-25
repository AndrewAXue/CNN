package cnn;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class only_nn_testing {
	
	neural_net net;
	
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
	
	public double [] get_input(){
		double input[] = new double[3*32*32];
		
		for (int i=0;i<32;i++){
			for (int k=0;k<32;k++){
				input[i*32+k] = image_lst[display_image_ind].red[i*32+k]/255.0;
			}
		}
		for (int i=0;i<32;i++){
			for (int k=0;k<32;k++){
				input[32*32+i*32+k] = image_lst[display_image_ind].green[i*32+k]/255.0;
			}
		}
		for (int i=0;i<32;i++){
			for (int k=0;k<32;k++){
				input[2*32*32+i*32+k] = image_lst[display_image_ind].blue[i*32+k]/255.0;
			}
		}
		return input;
	}
	
	public static void main(String[] args) throws IOException {
		new only_nn_testing().go();
	}
	
	public void go(){
		read_data();
		
		int arr[] = {32*32*3,100,10};
		net = new neural_net(arr);
		net.learning_rate = 1;
		net.quadratic = false;
		net.softmax = true;
		net.print = false;
		net.maxnodes = 6;
		net.num_epoch = 1;
		net.train_batch_size = 1;
		net.test_batch_size = 1;
		net.initialize_values();
		net.create_window();
		
		int batch_size = 100;
		
		for (int i=0;i<10;i++){
			display_image_ind = 0;
			while(display_image_ind<50000){
				//System.out.println(display_image_ind);
				int correct = 0;
				for (int j=0;j<batch_size;j++){
					double input[] = get_input();
					double expected[] = {0,0,0,0,0,0,0,0,0,0};
					expected[image_lst[display_image_ind].image_ind]=1;
					net.feed_and_set_expected(input, expected);
					
					double produced[] = net.getoutput();
					int max_ind = -1;
					for (int k=0;k<produced.length;k++){
						if (max_ind==-1||produced[max_ind]<produced[k]){
							max_ind = k;
						}
					}
					//System.out.println("Guessed "+max_ind);
					
					if (max_ind==image_lst[display_image_ind].image_ind)correct++;
					
					net.backpropagate();
					display_image_ind++;
				}
				System.out.println(display_image_ind+" "+correct);
				correct=0;
				net.gradient_descent(0.1,batch_size);
			}
		}
		
	}
}
