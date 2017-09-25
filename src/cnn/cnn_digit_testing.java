package cnn;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;


public class cnn_digit_testing {
	private class train_data{
		double solution[] = new double[10];
		double pixels[][] = new double[28][28];
		int answer;
		train_data(double tempsolution[],double temppixels[][]){
			solution = tempsolution;
			pixels = temppixels;
		}
	}
	// Array containing all of the training data
	train_data all_train_data [] = new train_data[42000];
	// Loads all the training data
	public void load_training() throws IOException{
		BufferedReader read = null;
		try {
			read = new BufferedReader(new FileReader("C:/Users/Andrew/workspace/neural net/digit_data/train.csv"));
		} catch (FileNotFoundException exp) {
			System.out.println("FILE NOT FOUND!");
			exp.printStackTrace();
		}
		read.readLine();
		for (int i=0;i<all_train_data.length;i++){
			String string_data[] = read.readLine().split(",");
			
			int correct = Integer.parseInt(string_data[0]);
			double ans[] = {0,0,0,0,0,0,0,0,0,0};
			ans[correct] = 1;
			// Set up the input data and feed it through the network
	    	double doublst[][] = new double[28][28];
	    	for (int a=0;a<28;a++){
	    		for (int b=0;b<28;b++){
	    			doublst[a][b] = Double.parseDouble(string_data[a*28+b+1])/255.0;
	    		}
	    	}
	    	all_train_data[i] = new train_data(ans,doublst);
	    	all_train_data[i].answer = correct;
		}
		System.out.println("DONE LOADING DATA");
	}
	
	public static void main(String[] args) throws IOException {
	    new cnn_digit_testing().go();
	}
	
	neural_net full_connect;
	cnn please;
	
	public void go() throws IOException{

		load_training();
		//int arr[] = {16*7*7,70,10};
		int arr[] = {12*7*7,70,10};
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
		full_connect.create_window();
		
		int data_depths[] = {1,6,6,12,12};
		int data_width_heights[] = {28,28,14,14,7};
		String layer_types[] = {"conv","pool","conv","pool"};
		int layer_depths[] = {6,1,12,1};
		int layer_width_heights[] = {3,2,3,2};
		
		/*
		int data_depths[] = {1,6,6,16,16};
		int data_width_heights[] = {28,28,14,14,7};
		String layer_types[] = {"conv","pool","conv","pool"};
		int layer_depths[] = {6,1,16,16};
		int layer_width_heights[] = {3,2,3,2};
		*/
		please = new cnn(data_depths,data_width_heights,layer_types,layer_depths,layer_width_heights);

		int image_ind = 0;
		
		for (int i=0;i<1;i++){
			image_ind=0;
			while(image_ind<42000){
				int correct = 0;
				for (int a=0;a<100;a++){
					double input[][][] = new double[1][][];
					double answer[] = {0,0,0,0,0,0,0,0,0,0};
					answer[all_train_data[image_ind].answer] = 1;
					input[0] = all_train_data[image_ind].pixels;
					please.feedforward(input);
					double cnn_output[][][] = please.cnn_get_output();
					full_connect.feed_and_set_expected(cnn_output, answer);
					double full_output[] = full_connect.getoutput();
					int max_ind=0;
					for (int k=0;k<10;k++){
						//System.out.print(full_output[k]+" ");
						if (full_output[k]>full_output[max_ind]){
							max_ind = k;
						}
					}
					//System.out.println();
					if (max_ind==all_train_data[image_ind].answer)correct++;
					//System.out.println("Guessed "+max_ind+" answer "+all_train_data[image_ind].answer);
					full_connect.backpropagate();
					double first_error[] = full_connect.get_first_error();
					please.cnn_back_propagate(first_error);
					image_ind++;
					please.cnn_gradient_descent(0.3, 100);
					full_connect.gradient_descent(0.3, 100);
				}
				System.out.println("Epoch "+i+" image_ind "+image_ind+" Correct "+correct);
			}
		}
	}
}
