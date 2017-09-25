package cnn;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.Random;
import java.util.Scanner;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;


public class cnn {
	
	JFrame window = new JFrame("VISUALIZATION");
	JFrame weights_window = new JFrame("VISUALIZATION V2");
	boolean draw = false;
	DecimalFormat round = new DecimalFormat("#.##");
	
static Scanner scanner;
	
	static Path path = Paths.get("cifar-10-binary/data_batch_1.bin");
	static byte[] image_data;
	
	int read_ind = 1;
	
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
			for (int k=0;k<1024;k++){
				int input = image_data[read_ind];
				byte b = (byte) input;
				//System.out.println(b); // -22
				int i2 = b & 0xFF;
				image_lst[image_ind].red[i] = i2;
				read_ind++;
			}
			for (int k=0;k<1024;k++){
				int input = image_data[read_ind];
				byte b = (byte) input;
				//System.out.println(b); // -22
				int i2 = b & 0xFF;
				image_lst[image_ind].green[i] = i2;
				read_ind++;
			}
			for (int k=0;k<1024;k++){
				int input = image_data[read_ind];
				byte b = (byte) input;
				//System.out.println(b); // -22
				int i2 = b & 0xFF;
				image_lst[image_ind].blue[i] = i2;
				read_ind++;
			}
			read_ind++;
			image_ind++;
		}
	}
	
	
	
	
	
	
	
	boolean debug = false;
	boolean sig = true;
	
	
	
	
	int conv_stride=1;
	int conv_pad=1;
	
	int pool_stride=2;
	
	int data_depths[];
	int data_width_heights[];
	int layer_depths[];
	int layer_width_heights[];
	
	int alllayersize[];
	Random biaschoose = new Random();
	Random weightchoose = new Random();
	Random valuechoose = new Random();
	
	public class weightclass{
		double weight;
		double weightdev=0;
		weightclass(){
			weight = weightchoose.nextDouble()*10-5;
		}
		weightclass(weightclass copy){
			weight = copy.weight;
			weightdev = copy.weightdev;
		}
		weightclass(double tempweight){
			weight = tempweight;
		}
	}
	
	public class cnn_nodeclass{
		double y_value=0;
		double x_value=0;
		double y_dev=0;
		double x_dev=0;
	}
	// data[layer][depth][height][width];
	cnn_nodeclass data [][][][];
	cnn_layerclass cnn_layers [];
	
	public class cnn_layerclass{
		//either "pool" or "conv"
		String function;
		// For convolution layers, this is just equal to convolution matrix size
		// For pooling, this is the size of the pooling matrix
		int depth;
		int matrix_width_height;
		// matrix_values[depth][numperdepth = data_depths[i]][height][width]
		weightclass matrix_values[][][][];
		double bias;
		int stride;
		int zero_padding;
		// only "max"
		String pooling_type;
		
		cnn_layerclass(String temp_function,int temp_depth,int temp_matrix_width_height,
						weightclass temp_matrix_values[][][][],double temp_bias,
						int temp_stride,
						int temp_zero_padding,
						String temp_pooling_type){
			function = temp_function;
			depth = temp_depth;
			matrix_width_height = temp_matrix_width_height;
			if (temp_function.compareTo("conv")==0){
				//System.out.println(depth+" "+temp_matrix_values[0].length+" "+temp_matrix_values[0][0].length+" "+temp_matrix_values[0][0].length);
				
				matrix_values = 
						new weightclass[depth][temp_matrix_values[0].length][temp_matrix_values[0][0].length][temp_matrix_values[0][0].length];
				for (int a=0;a<depth;a++){
					for (int i=0;i<temp_matrix_values[0].length;i++){
						for (int k=0;k<temp_matrix_values[0][0].length;k++){
							for (int z=0;z<temp_matrix_values[0][0].length;z++)
							matrix_values[a][i][k][z] = new weightclass(temp_matrix_values[a][i][k][z]);
						}
					}
				}
					
			}
			bias = temp_bias;
			stride = temp_stride;
			zero_padding = temp_zero_padding;
			// only "max"
			pooling_type = temp_pooling_type;
		}
		cnn_layerclass(cnn_layerclass copy){
			function = copy.function;
			depth = copy.depth;
			matrix_width_height = copy.matrix_width_height;
			//matrix_values[depth][num matrix per depth][height][width]
			matrix_values = 
			new weightclass[depth][copy.matrix_values.length][copy.matrix_values[0].length][copy.matrix_values[0][0].length];
			for (int a=0;a<depth;a++){
				for (int i=0;i<copy.matrix_values.length;i++){
					for (int k=0;k<copy.matrix_values[0].length;k++){
						for (int z=0;z<copy.matrix_values[0][0].length;z++)
						matrix_values[a][i][k][z] = new weightclass(copy.matrix_values[a][i][k][z]);
					}
				}
			}
			
			bias = copy.bias;
			stride = copy.stride;
			zero_padding = copy.zero_padding;
			// only "max"
			pooling_type = copy.pooling_type;
		}
	}
	//cnn_layerclass three_conv;
	//cnn_layerclass two_pool;
	
	cnn(int temp_data_depths[],int temp_data_width_heights[],String temp_layer_types[],int temp_layer_depths[],int temp_layer_width_heights[]){

		
		data_depths = new int[temp_data_depths.length];
		data_width_heights = new int[temp_data_width_heights.length];
		layer_depths = new int[temp_layer_depths.length];
		layer_width_heights = new int[temp_layer_width_heights.length];
	
		for (int i=0;i<data_depths.length;i++){
			data_depths[i] = temp_data_depths[i];
		}
		for (int i=0;i<data_width_heights.length;i++){
			data_width_heights[i] = temp_data_width_heights[i];
		}
		for (int i=0;i<layer_depths.length;i++){
			layer_depths[i] = temp_layer_depths[i];
		}
		for (int i=0;i<layer_width_heights.length;i++){
			layer_width_heights[i] = temp_layer_width_heights[i];
		}
		
		//three_conv = new cnn_layerclass("conv",0,3,temp_matrix_values,biaschoose.nextDouble(),1,1,"");
		//two_pool = new cnn_layerclass("pool",1,2,null,0,2,0,"max");
		
		data = new cnn_nodeclass[temp_data_depths.length][][][];
		for (int i=0;i<temp_data_depths.length;i++){
			data[i] = new cnn_nodeclass[temp_data_depths[i]][temp_data_width_heights[i]][temp_data_width_heights[i]];
		}
		for (int i=0;i<temp_data_depths.length;i++){
			for (int k=0;k<temp_data_depths[i];k++){
				for (int a=0;a<temp_data_width_heights[i];a++){
					for (int b=0;b<temp_data_width_heights[i];b++){
						data[i][k][a][b] = new cnn_nodeclass();
					}
				}
			}
		}
		
		
		cnn_layers = new cnn_layerclass[temp_layer_types.length];
		for (int i=0;i<temp_layer_types.length;i++){
			if (temp_layer_types[i].compareTo("pool")==0){
				cnn_layers[i] = new cnn_layerclass("pool",temp_layer_depths[i],temp_layer_width_heights[i],null,0,pool_stride,0,"max");
			}
			else if (temp_layer_types[i].compareTo("conv")==0){
				weightclass temp_matrix_values[][][][] = new weightclass[layer_depths[i]][data_depths[i]][temp_layer_width_heights[i]][temp_layer_width_heights[i]];
				//System.out.println("new "+layer_depths[i]+" "+data_depths[i]+" "+temp_layer_width_heights[i]);
				for (int l=0;l<layer_depths[i];l++){
					for (int a=0;a<data_depths[i];a++){
						for (int k=0;k<temp_layer_width_heights[i];k++){
							for (int z=0;z<temp_layer_width_heights[i];z++){
								temp_matrix_values[l][a][k][z] = new weightclass();
							}
						}
					}
				}
				
				cnn_layers[i] = new cnn_layerclass("conv",temp_layer_depths[i],temp_layer_width_heights[i],
						temp_matrix_values,0,conv_stride,conv_pad,"");
				cnn_layerclass cur = cnn_layers[i];
				if (debug){
					if (i==0){
						double test_weights[][][][] = {
								{
								{{0,0,0},{0,1,0},{0,1,-1}},
								{{1,0,0},{0,-1,-1},{0,1,-1}},
								{{1,1,0},{1,-1,-1},{-1,1,0}}},
								
								{{{1,1,-1},{1,1,-1},{-1,1,1}},
								{{0,-1,-1},{0,-1,1},{-1,1,0}},
								{{-1,1,1},{-1,-1,0},{1,1,-1}}}
								};
								
						for (int z=0;z<2;z++){
							for (int a=0;a<3;a++){
								for (int b=0;b<3;b++){
									for (int c=0;c<3;c++){
										//System.out.println(z+" "+a+" "+b+" "+c);
										cur.matrix_values[z][a][b][c].weight = test_weights[z][a][b][c];
									}
								}
							}
						}
					}
					
					else{
						double test_weights[][][][] = {
								{
								{{2,3,4},{-1,1,-2},{4,2,1}},
								{{3,0,2},{1,2,0},{1,2,0}}
								}
								};
								
						for (int z=0;z<1;z++){
							for (int a=0;a<2;a++){
								for (int b=0;b<3;b++){
									for (int c=0;c<3;c++){
										//System.out.println(z+" "+a+" "+b+" "+c);
										cur.matrix_values[z][a][b][c].weight = test_weights[z][a][b][c];
									}
								}
							}
						}
					}
					
				}
				
				
				
				
			}
		}
		if (draw) create_window();
	}
	
	// sigmoid function for "smoothing out" the output values
	double sigmoid(double x){
		//return Math.tanh(x);
		return 1/(1+Math.exp(-x));
	}
	
	// derivative of the sigmoid function used in backpropagation
	double sigmoidprime(double x){
		//return 1/Math.pow(Math.cosh(x),2);
		double sig = sigmoid(x);
		return sig*(1-sig);
	}
	
	//cnn_nodeclass data [][][][];
	//cnn_layerclass cnn_layers [];
	
	void feedforward(double input_data[][][]){
		if (input_data.length!=data[0].length||input_data[0].length!=data[0][0].length||input_data[0][0].length!=data[0][0][0].length){
			System.out.println("bad sized data");
		}
		for (int i=0;i<input_data.length;i++){
			for (int k=0;k<input_data[0].length;k++){
				for (int z=0;z<input_data[0][0].length;z++){
					data[0][i][k][z].y_value = input_data[i][k][z];
					data[0][i][k][z].x_value = input_data[i][k][z];
				}
			}
		}
		//int depth;
		//int matrix_width_height;
		for (int i=0;i<data.length-1;i++){
			cnn_layerclass layer_props = cnn_layers[i];
			if (layer_props.function.compareTo("conv")==0){
				for (int a=0;a<data_depths[i+1];a++){
					for (int b=0;b<data_width_heights[i+1];b++){
						for (int c=0;c<data_width_heights[i+1];c++){
							int height = b*layer_props.stride-layer_props.zero_padding;
							int width = c*layer_props.stride-layer_props.zero_padding;
							double sum = 0;
							//System.out.println("new sum target "+(i+1)+" "+a+" "+b+" "+c);
							for (int d=0;d<data_depths[i];d++){
								for (int e=0;e<layer_width_heights[i];e++){
									for (int f=0;f<layer_width_heights[i];f++){
										int final_height = height+e;
										int final_width = width+f;
										//System.out.println("height "+final_height+" final weid "+final_width);
										//System.out.println("ind is "+a+" "+d+" "+e+" "+f+" "+layer_props.matrix_values[a][d][e][f].weight);
										if (final_height<0||final_height>=data_width_heights[i]||final_width<0||final_width>=data_width_heights[i]){
											
										}
										else{
											//for (int d=0;d<data_depths[i];d++)
												sum+=data[i][d][final_height][final_width].y_value*layer_props.matrix_values[a][d][e][f].weight;
										}
										//System.out.println("summed "+i+" "+d+" "+final_height+" "+final_width+" to "+sum);
										//System.out.println("ind is "+a+" "+b+" "+c+" "+sum);
									}
			
								}
								
							//System.out.println();
							}
							//System.out.println((i+1)+" "+a+" "+b+" "+c);
							sum+=layer_props.bias;
							//System.out.println("total sum is "+sum);
							data[i+1][a][b][c].x_value = sum;
							data[i+1][a][b][c].y_value = sum;
							if (sig) data[i+1][a][b][c].y_value = sigmoid(sum);							
							
						}
					}
				}
			}
			else if (layer_props.function.compareTo("pool")==0){
				for (int k=0;k<data_depths[i];k++){
					for (int a=0;a<data_width_heights[i+1];a++){
						for (int b=0;b<data_width_heights[i+1];b++){
							int init_x = a*layer_props.stride;
							int init_y = b*layer_props.stride;
							//System.out.println(init_x+" "+init_y);
							boolean max_inited = false;
							double max = 0;
							//System.out.println(init_x+" "+init_y);
							//System.out.println(layer_props.matrix_width_height+" ");
							for (int z=0;z<layer_props.matrix_width_height;z++){
								for (int c=0;c<layer_props.matrix_width_height;c++){
									if (!max_inited){
										max = data[i][k][init_x+z][init_y+c].x_value;
										max_inited = true;
									}
									else if (data[i][k][init_x+z][init_y+c].x_value>max){
										max = data[i][k][init_x+z][init_y+c].x_value;
									}
								}
							}
							data[i+1][k][a][b].x_value = max;
							data[i+1][k][a][b].y_value = sigmoid(max);
						}
					}
				}
			}
		}
	}
	
	double[][][] cnn_get_output(){
		int last_depth = data_depths[data.length-1];
		int last_width_height = data_width_heights[data.length-1];
		double output[][][] = new double[last_depth][last_width_height][last_width_height];
	
		for (int a=0;a<last_depth;a++){
			for (int b=0;b<last_width_height;b++){
				for (int c=0;c<last_width_height;c++){
					output[a][b][c] = data[data.length-1][a][b][c].y_value;
				}
			}
		}
		return output;
	}
	
	double cnn_learning_rate=1;
	int batch_size = 100;
	
	void cnn_gradient_descent(double cnn_learning_rate,int batch_size){
		for (int a=0;a<data.length;a++){
			for (int b=0;b<data_depths[a];b++){
				for (int c=0;c<data_width_heights[a];c++){
					for (int d=0;d<data_width_heights[a];d++){
						data[a][b][c][d].x_dev = 0;
						data[a][b][c][d].y_dev = 0;
					}
				}
			}
		}
		for (int i=0;i<cnn_layers.length;i++){
			//System.out.println(i);
			if (cnn_layers[i].function.compareTo("conv")==0){
				for (int k=0;k<cnn_layers[i].matrix_values.length;k++){
					for (int a=0;a<cnn_layers[i].matrix_values[0].length;a++){
						for (int b=0;b<cnn_layers[i].matrix_values[0][0].length;b++){
							for (int c=0;c<cnn_layers[i].matrix_values[0][0][0].length;c++){
								cnn_layers[i].matrix_values[k][a][b][c].weight-=((cnn_learning_rate/(double)batch_size)*cnn_layers[i].matrix_values[k][a][b][c].weightdev);
								cnn_layers[i].matrix_values[k][a][b][c].weightdev = 0;
							}
						}
					}
				}
			}
			
		}
	}
	
	void cnn_back_propagate(double last_layer_error[]){
		int last_depth = data_depths[data.length-1];
		int last_width = data_width_heights[data.length-1];
		
		int ind = 0;
		
		for (int a=0;a<last_depth;a++){
			for (int b=0;b<last_width;b++){
				for (int c=0;c<last_width;c++){
					data[data.length-1][a][b][c].x_dev = last_layer_error[ind];
					ind++;
				}
			}
		}
		
		for (int a=0;a<data_width_heights[data_width_heights.length-1];a++){
			for (int b=0;b<data_width_heights[data_width_heights.length-1];b++){
				data[data.length-1][0][a][b].x_dev = last_layer_error[a*data_width_heights[data_width_heights.length-1]+b];
				//data[data.length-1][0][a][b].y_dev = null;
			}
		}
		// Only ydevs and xdevs
		for (int a=data_width_heights.length-1;a>=1;a--){
			cnn_layerclass active_layer = cnn_layers[a-1];
			if (cnn_layers[a-1].function.compareTo("conv")==0){
				for (int b=0;b<data_depths[a];b++){
					for (int c=0;c<data_width_heights[a];c++){
						for (int d=0;d<data_width_heights[a];d++){
							// Found correct active_dev
							//System.out.println(a+" "+b+" "+c+" "+d+" "+data[a][b][c][d].x_dev);
							double active_dev = data[a][b][c][d].x_dev;
							if (active_dev!=0){
								int init_x = -active_layer.zero_padding+d*active_layer.stride;
								int init_y = -active_layer.zero_padding+c*active_layer.stride;
								//System.out.println(a+" "+b+" "+c+" "+d+" "+data[a][b][c][d].x_dev+" "+init_x+" "+init_y);
								int solved_x,solved_y;
								for (int e=0;e<data_depths[a-1];e++){
									for (int f=0;f<layer_width_heights[a-1];f++){
										for (int g=0;g<layer_width_heights[a-1];g++){
											solved_x = init_x+g;
											solved_y = init_y+f;
											//System.out.println(e+" "+solved_y+" "+solved_x);
											if (solved_x>=0&&solved_x<data_width_heights[a-1]&&solved_y>=0&&solved_y<data_width_heights[a-1]){
												data[a-1][e][solved_y][solved_x].y_dev+=active_dev*active_layer.matrix_values[b][e][f][g].weight;
												//System.out.println(data[a-1][e][solved_y][solved_x].y_dev);
											}
											else{
												//System.out.println("OUT");
											}
										}
									}
								}
							}
							
						}
					}
				}
				// Consolidating 
				for (int b=0;b<data_depths[a-1];b++){
					for (int c=0;c<data_width_heights[a-1];c++){
						for (int d=0;d<data_width_heights[a-1];d++){
							data[a-1][b][c][d].x_dev = data[a-1][b][c][d].y_dev*sigmoidprime(data[a-1][b][c][d].x_value);
						}
					}
				}
			}
			else if (cnn_layers[a-1].function.compareTo("pool")==0){
				for (int b=0;b<data_depths[a];b++){
					for (int c=0;c<data_width_heights[a];c++){
						for (int d=0;d<data_width_heights[a];d++){
							double target = data[a][b][c][d].y_value;
							
							int init_x = d*active_layer.stride;
							int init_y = c*active_layer.stride;
							
							for (int e=0;e<active_layer.stride;e++){
								for (int f=0;f<active_layer.stride;f++){
									if (target==data[a-1][b][init_y+e][init_x+f].y_value){
										data[a-1][b][init_y+e][init_x+f].y_dev = data[a][b][c][d].y_dev;
										data[a-1][b][init_y+e][init_x+f].x_dev = data[a][b][c][d].x_dev;
									}
								}
							}
						}
					}
				}
			}
		}
		
		/*
		int data_depths[];
		int data_width_heights[];
		int layer_depths[];
		int layer_width_heights[];
		
		cnn_layers
		
		// matrix_values[depth][numperdepth = data_depths[i]][height][width]
		*/
		//Finding weight devs
		/*
		for (int a=cnn_layers.length-1;a>=0;a--){
			cnn_layerclass active_layer = cnn_layers[a];
			if (active_layer.function.compareTo("conv")==0){
				for (int b=0;b<active_layer.depth;b++){
					for (int c=0;c<data_depths[a];c++){
						for (int d=0;d<active_layer.matrix_width_height;d++){
							for (int e=0;e<active_layer.matrix_width_height;e++){
								weightclass active_weight = cnn_layers[a].matrix_values[b][c][d][e];
								
								int init_x = -cnn_layers[a].zero_padding+e;
								int init_y = -cnn_layers[a].zero_padding+d;
								
								System.out.println("weight "+active_weight.weight+" "+init_x+" "+init_y);
								
								for (int f=0;f<data_width_heights[a+1];f++){
									for (int g=0;g<data_width_heights[a+1];g++){
										active_weight.weightdev+=
										init_x+=cnn_layers[a].matrix_width_height;
										init_y+=cnn_layers[a].matrix_width_height;
									}
								}
							}
						}
					}
				}
			}
			
		}
		*/
		
		/*
		int data_depths[];
		int data_width_heights[];
		int layer_depths[];
		int layer_width_heights[];
		
		cnn_layers
		
		// matrix_values[depth][numperdepth = data_depths[i]][height][width]
		*/
		
		//Finding weight devs v2
		
		for (int a=cnn_layers.length-1;a>=0;a--){
			cnn_layerclass active_layer = cnn_layers[a];
			if (active_layer.function.compareTo("conv")==0){
				for (int b=0;b<data_depths[a+1];b++){
					for (int c=0;c<data_width_heights[a+1];c++){
						for (int d=0;d<data_width_heights[a+1];d++){
							double cur_devx = data[a+1][b][c][d].x_dev;
							//Initial x value affected by weight matrix, at the top left
							int init_x = -cnn_layers[a].zero_padding+d*cnn_layers[a].stride;
							int init_y = -cnn_layers[a].zero_padding+c*cnn_layers[a].stride;
							//System.out.println("current devx "+cur_devx+" at "+b+" "+c+" "+d+" has init "+init_x+" "+init_y);
							
							
							for (int e=0;e<data_depths[a];e++){
								for (int f=0;f<layer_width_heights[a];f++){
									for (int g=0;g<layer_width_heights[a];g++){
										int final_x = init_x+g;
										int final_y = init_y+f;
										if (final_x>=0&&final_x<data_width_heights[a]&&final_y>=0&&final_y<data_width_heights[a]){
											//System.out.println("blamed "+data[a][e][final_y][final_x].y_value);
											active_layer.matrix_values[b][e][f][g].weightdev+=cur_devx*data[a][e][final_y][final_x].y_value;
										}
									}
								}
							}
						}
					}
				}
			}
		}
		
	}
	
	void print_values(){
		for (int i=0;i<data.length;i++){
			for (int k=0;k<data_depths[i];k++){
				for (int a=0;a<data_width_heights[i];a++){
					for (int b=0;b<data_width_heights[i];b++){
						System.out.print(data[i][k][a][b].y_value+"   ");
					}
					System.out.print('\n');
				}
				System.out.println("block done");
			}
			System.out.println("layer done\n\n\n");
		}
	}
	
	void print_xdev(){
		for (int i=0;i<data.length;i++){
			for (int k=0;k<data_depths[i];k++){
				for (int a=0;a<data_width_heights[i];a++){
					for (int b=0;b<data_width_heights[i];b++){
						System.out.print(" xdev "+data[i][k][a][b].x_dev+" ydev "+data[i][k][a][b].y_dev);
					}
					System.out.print('\n');
				}
				System.out.println("block done");
			}
			System.out.println("layer done\n\n\n");
		}
	}
	
	void print_weights(){
		for (int i=0;i<cnn_layers.length;i++){
			for (int z=0;z<cnn_layers[i].depth;z++){
				for (int k=0;k<data_depths[i];k++){
					for (int a=0;a<cnn_layers[i].matrix_width_height;a++){
						for (int b=0;b<cnn_layers[i].matrix_width_height;b++){
							if (cnn_layers[i].function.compareTo("conv")==0)System.out.print(cnn_layers[i].matrix_values[z][k][a][b].weight+"   ");
						}
						System.out.print('\n');
					}
					System.out.println("block done");
				}
				System.out.println("one depth done\n\n");
			}
			
			System.out.println("layer done\n\n\n");
		}
	}
	
	void print_weight_dev(){
		for (int i=0;i<cnn_layers.length;i++){
			for (int z=0;z<cnn_layers[i].depth;z++){
				for (int k=0;k<data_depths[i];k++){
					for (int a=0;a<layer_width_heights[i];a++){
						for (int b=0;b<layer_width_heights[i];b++){
							if (cnn_layers[i].function.compareTo("conv")==0)System.out.print(cnn_layers[i].matrix_values[z][k][a][b].weightdev+"   ");
						}
						System.out.print('\n');
					}
					System.out.println("block done");
				}
				System.out.println("one depth done\n\n");
			}
			
			System.out.println("layer done\n\n\n");
		}
	}
	
	protected void create_window(){
		//Set characteristics of window
		window = new JFrame("VISUALIZATION");
		window.setSize(1000, 1000);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setResizable(false);
		window.setIconImage(new ImageIcon("neural.jpg").getImage());
		window.add(new VISUALIZATION());
		window.setVisible(true);
		
		weights_window = new JFrame("VISUALIZATION");
		weights_window.setSize(1000, 1000);
		weights_window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		weights_window.setResizable(false);
		weights_window.setIconImage(new ImageIcon("neural.jpg").getImage());
		weights_window.add(new VISUALIZATION_WEIGHTS());
		weights_window.setVisible(true);
	}
	
	private class VISUALIZATION extends JComponent {
			
		VISUALIZATION() {
            setPreferredSize(new Dimension(1000, 1000));
        }
		
		public void paintComponent(Graphics g){
			super.paintComponents(g);
			Graphics2D grap = (Graphics2D) g; 	
			window.setBackground(Color.WHITE);
			
			/*
			 * int data_depths[];
	int data_width_heights[];
	int layer_depths[];
	int layer_width_heights[];
			 */
			int num_width_height = 60;
			grap.setColor(Color.BLACK);
			
			int rect_x_ind = 0;
			int rect_y_ind = 0;
			int draw_x_ind = 0;
			int draw_y_ind = 0;
			for (int i=0;i<data.length;i++){
				//System.out.println("layer "+i);
				for (int a=0;a<data[i].length;a++){
					//System.out.println("Depth "+a);
					//System.out.println(rect_x_ind+" "+rect_y_ind+" ");
					grap.drawRect(rect_x_ind, rect_y_ind, data[i][0].length*num_width_height, data[i][0].length*num_width_height);
					draw_x_ind = rect_x_ind;
					draw_y_ind = rect_y_ind;
					for (int b=0;b<data[i][0].length;b++){
						for (int c=0;c<data[i][0].length;c++){
							grap.drawRect(draw_x_ind, draw_y_ind, num_width_height, num_width_height);
							grap.drawString("x "+Double.valueOf(round.format(data[i][a][b][c].x_value)), draw_x_ind+5, draw_y_ind+10);
							grap.drawString("y "+Double.valueOf(round.format(data[i][a][b][c].y_value)), draw_x_ind+5, draw_y_ind+10+num_width_height/4);
							grap.drawString("dx "+Double.valueOf(round.format(data[i][a][b][c].x_dev)), draw_x_ind+5, draw_y_ind+10+2*num_width_height/4);
							grap.drawString("dy "+Double.valueOf(round.format(data[i][a][b][c].y_dev)), draw_x_ind+5, draw_y_ind+10+3*num_width_height/4);

							draw_x_ind+=num_width_height;
						}
						draw_y_ind+=num_width_height;
						draw_x_ind = rect_x_ind;
					}
					rect_y_ind+=data[i][0].length*num_width_height+20;
				}
				rect_x_ind+=data[i][0].length*num_width_height+20;
				rect_y_ind = 0;
			}	
			//System.out.println("Done drawing");
		}
	}
	
	private class VISUALIZATION_WEIGHTS extends JComponent {
		
		VISUALIZATION_WEIGHTS() {
            setPreferredSize(new Dimension(1000, 1000));
        }
		
		public void paintComponent(Graphics g){
			super.paintComponents(g);
			Graphics2D grap = (Graphics2D) g; 	
			window.setBackground(Color.WHITE);
			
			/*
			 * int data_depths[];
	int data_width_heights[];
	int layer_depths[];
	int layer_width_heights[];
			 */
			int num_width_height = 60;
			grap.setColor(Color.BLACK);
			
			int rect_x_ind = 0;
			int rect_y_ind = 0;
			int draw_x_ind = 0;
			int draw_y_ind = 0;
			
			// matrix_values[depth][numperdepth = data_depths[i]][height][width]
			for (int i=0;i<cnn_layers.length;i++){
				//System.out.println("layer "+i);
				grap.setStroke(new BasicStroke(1));
				if (cnn_layers[i].function.compareTo("conv")==0){
					grap.drawRect(rect_x_ind, rect_y_ind, num_width_height*cnn_layers[i].matrix_width_height*cnn_layers[i].matrix_values.length, num_width_height*cnn_layers[i].matrix_width_height*cnn_layers[i].matrix_values[0].length);
					draw_x_ind = rect_x_ind;
					draw_y_ind = rect_y_ind;
					for (int a=0;a<cnn_layers[i].matrix_values.length;a++){
						weightclass cur_mat[][][][] = cnn_layers[i].matrix_values;
						for (int b=0;b<cnn_layers[i].matrix_values[0].length;b++){
							grap.setStroke(new BasicStroke(2));
							grap.drawRect(draw_x_ind, draw_y_ind, num_width_height*cnn_layers[i].matrix_width_height, num_width_height*cnn_layers[i].matrix_width_height);
							grap.setStroke(new BasicStroke(1));
							int temp_draw_x_ind = draw_x_ind;
							for (int c=0;c<cnn_layers[i].matrix_width_height;c++){
								for (int d=0;d<cnn_layers[i].matrix_width_height;d++){
									grap.drawRect(draw_x_ind, draw_y_ind, num_width_height, num_width_height);
									grap.drawString("W "+Double.valueOf(round.format(cur_mat[a][b][c][d].weight)), draw_x_ind, draw_y_ind+20);
									grap.drawString("dW "+Double.valueOf(round.format(cur_mat[a][b][c][d].weightdev)), draw_x_ind, draw_y_ind+40);
									draw_x_ind+=num_width_height;
								}
								draw_x_ind=temp_draw_x_ind;
								draw_y_ind+=num_width_height;
							}
						}
						draw_y_ind=0;
						draw_x_ind+=num_width_height*cnn_layers[i].matrix_width_height;
					}
					rect_x_ind+=num_width_height*cnn_layers[i].matrix_width_height*cnn_layers[i].matrix_values.length+10;
					rect_y_ind=0;
				}
				else{
					grap.drawString("POOL", rect_x_ind, rect_y_ind+20);
					rect_x_ind+=40;
				}
				
			}	
			//System.out.println("Done drawing");
		}
	}
}
