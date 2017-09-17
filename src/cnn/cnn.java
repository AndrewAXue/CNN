package cnn;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;
import java.util.Scanner;

public class cnn {
	
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
	
	
	
	
	
	
	
	
	
	
	
	
	int conv_stride=2;
	int conv_pad=1;
	
	int pool_stride=1;
	
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
			weight = weightchoose.nextDouble();
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
		double y_value,x_value;
		double y_dev=0;
		double x_dev=0;
		cnn_nodeclass(){
			y_value = valuechoose.nextDouble();
			x_value = valuechoose.nextDouble();
		}
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
		// matrix_values[depth][height][width]
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
				
				if (i==0){
					double test_weights[][][][] = {
							{
							{{1,1,0},{0,0,-1},{0,-1,-1}},
							{{0,0,0},{1,0,0},{0,1,-1}},
							{{1,-1,-1},{1,-1,0},{1,0,0}}},
							
							{{{0,1,0},{-1,0,-1},{1,0,0}},
							{{0,1,-1},{1,1,0},{0,-1,0}},
							{{1,1,1},{0,-1,-1},{1,0,-1}}}
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
							{{2,3},{-1,1}},
							{{3,0},{1,2}}
							}
							};
							
					for (int z=0;z<1;z++){
						for (int a=0;a<2;a++){
							for (int b=0;b<2;b++){
								for (int c=0;c<2;c++){
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
				}
			}
		}
		//int depth;
		//int matrix_width_height;
		for (int i=0;i<data.length-1;i++){
			cnn_layerclass layer_props = cnn_layers[i];
			if (layer_props.function.compareTo("conv")==0){
				for (int a=0;a<layer_props.depth;a++){
					for (int b=0;b<data_width_heights[i+1];b++){
						for (int c=0;c<data_width_heights[i+1];c++){
							int height = b*layer_props.stride-layer_props.zero_padding;
							int width = c*layer_props.stride-layer_props.zero_padding;
							double sum = 0;
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
										//System.out.println("ind is "+a+" "+b+" "+c+" "+sum);
									}
			
								}
								
							//System.out.println();
							}
							//System.out.println((i+1)+" "+a+" "+b+" "+c);
							sum+=layer_props.bias;
							data[i+1][a][b][c].x_value = sum;
							data[i+1][a][b][c].y_value = sum;
							//data[i+1][a][b][c].y_value = sigmoid(sum);							
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
										max = data[i][k][init_x+z][init_y+c].y_value;
										max_inited = true;
									}
									else if (data[i][k][init_x+z][init_y+c].y_value>max){
										max = data[i][k][init_x+z][init_y+c].y_value;
									}
								}
							}
							data[i+1][k][a][b].y_value = max;
						}
					}
				}
			}
		}
	}
	
	double[][] cnn_get_output(){
		double output[][] = new double[data_width_heights[data_width_heights.length-1]][data_width_heights[data_width_heights.length-1]];
	
		for (int k=0;k<data_width_heights[data_width_heights.length-1];k++){
			for (int z=0;z<data_width_heights[data_width_heights.length-1];z++){
				output[k][z] = data[data_width_heights.length-1][0][k][z].y_value;
			}
		}
		return output;
	}
	
	double cnn_learning_rate=0.3;
	int batch_size;
	
	void cnn_gradient_descent(){
		for (int a=0;a<data.length;a++){
			for (int b=0;b<data[a].length;b++){
				for (int c=0;c<data[a][b].length;c++){
					for (int d=0;d<data[a][b][c].length;d++){
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
		//NBP1
		for (int a=0;a<data_width_heights[data_width_heights.length-1];a++){
			for (int b=0;b<data_width_heights[data_width_heights.length-1];b++){
				data[data.length-1][0][a][b].x_dev = last_layer_error[a*data_width_heights[data_width_heights.length-1]+b];
				//data[data.length-1][0][a][b].y_dev = null;
			}
		}
		
		for (int i=data_depths.length-2;i>=1;i--){
			cnn_layerclass layer_props = cnn_layers[i];
			if (layer_props.function.compareTo("pool")==0){
				for (int k=0;k<data_depths[i+1];k++){
					for (int a=0;a<data_width_heights[i+1];a++){
						for (int b=0;b<data_width_heights[i+1];b++){
							int init_x = a*layer_props.stride;
							int init_y = b*layer_props.stride;
							
							for (int z=0;z<layer_props.matrix_width_height;z++){
								for (int c=0;c<layer_props.matrix_width_height;c++){
									//System.out.println(i+" "+k+" "+(init_x+z)+" "+(init_y+c));
									if (data[i][k][init_x+z][init_y+c].y_value==data[i+1][k][a][b].y_value){
										data[i][k][init_x+z][init_y+c].y_dev += data[i+1][k][a][b].y_dev;
										data[i][k][init_x+z][init_y+c].x_dev += data[i][k][init_x+z][init_y+c].y_dev*sigmoidprime(data[i][k][init_x+z][init_y+c].y_value);
									}
								}
							}
						}
					}
				}
			}
			else if (layer_props.function.compareTo("conv")==0){
				
				for (int k=0;k<data_depths[i];k++){
					for (int a=0;a<data_width_heights[i];a++){
						for (int b=0;b<data_width_heights[i];b++){
							cnn_nodeclass active = data[i][k][a][b];
							double sum_y_dev = 0;
							for (int e=0;e<data_depths[i+1];e++){
								for (int c=0;c<cnn_layers[i].matrix_width_height;c++){
									for (int d=0;d<cnn_layers[i].matrix_width_height;d++){
										if (a-c>=0&&b-d>=0)sum_y_dev+=data[i+1][e][a-c][b-d].x_dev*cnn_layers[i].matrix_values[e][k][c][d].weight;
									}
								}
							}
							active.y_dev += sum_y_dev;
							active.x_dev += sum_y_dev*sigmoidprime(active.x_value);
						}
					}
				}
				
				/*
				for (int k=0;k<data_depths[i+1];k++){
					for (int j=0;j<data_depths[i];j++){
						for (int a=0;a<data_width_heights[i+1];a++){
							for (int b=0;b<data_width_heights[i+1];b++){
								cnn_nodeclass active = data[i][k][a][b];
								double sum_y_dev = 0;
								for (int c=0;c<cnn_layers[i].matrix_width_height;c++){
									for (int d=0;d<cnn_layers[i].matrix_width_height;d++){
										//System.out.println((b-d)+" "+i+" "+k+" ");
										if (a-c>=0&&b-d>=0){
											//System.out.println(data[i+1][k][a-c].length+" "+(b-d)+" "+i+" "+k+" ");
											//System.out.println(a+" "+b);
											sum_y_dev+=data[i+1][k][a-c][b-d].x_dev*
													cnn_layers[i]
															.matrix_values[k][j][c][d].weight;
										}
									}
								}
								active.y_dev += sum_y_dev;
								active.x_dev += sum_y_dev*sigmoidprime(active.x_value);
							}
						}
					}
				}
				*/
				
				for (int k=0;k<layer_depths[i];k++){
					//matrix_values[depth][num matrix per depth][height][width]
					for (int a=0;a<data_depths[i+1];a++){
						for (int b=0;b<cnn_layers[i].matrix_width_height;b++){
							for (int c=0;c<cnn_layers[i].matrix_width_height;c++){
								//System.out.println(k+" "+a+" "+b+" "+c);
								weightclass active = cnn_layers[i].matrix_values[k][a][b][c];
								double weight_sum = 0;
								for (int d=0;d<=data_width_heights[i]-cnn_layers[i].matrix_width_height;d++){
									for (int e=0;e<=data_width_heights[i]-cnn_layers[i].matrix_width_height;e++){
										weight_sum+=data[i][k][d][e].x_dev*data[i-1][k][d+b][e+c].y_value;
									}
								}
								active.weightdev += weight_sum;
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
					for (int a=0;a<data_width_heights[i+1];a++){
						for (int b=0;b<data_width_heights[i+1];b++){
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
					for (int a=0;a<data_width_heights[i+1];a++){
						for (int b=0;b<data_width_heights[i+1];b++){
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
}
