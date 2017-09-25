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
	cnn please;
	
	public static void main(String[] args) throws IOException {
	    new cnn_testing().go();
	}

	int batch_size = 100;
	
	public void go(){		 
		/*
		
		int data_depths[] = {3,2};
		int data_width_heights[] = {5,3};
		String layer_types[] = {"conv"};
		int layer_depths[] = {2};
		int layer_width_heights[] = {3};
		please = new cnn(data_depths,data_width_heights,layer_types,layer_depths,layer_width_heights);
		
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
		please = new cnn(data_depths,data_width_heights,layer_types,layer_depths,layer_width_heights);
		
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
		
		
		/*
		int data_depths[] = {3,2,2,1};
		int data_width_heights[] = {4,4,2,2};
		String layer_types[] = {"conv","pool","conv"};
		
		int conv_constant_wid_hid = 3;
		
		int pool_constant_depth = 1;
		int pool_constant_wid_hid = 2;
		
		int layer_depths[] = {2,pool_constant_depth,1};
		int layer_width_heights[] = {conv_constant_wid_hid,pool_constant_wid_hid,conv_constant_wid_hid};
		please = new cnn(data_depths,data_width_heights,layer_types,layer_depths,layer_width_heights);
		
		double input_data[][][] = {
				{{0.2,0.2,0.1,0},{0.2,0.1,0,0.2},{0.2,0,0.1,0.2},{0,0.1,0.2,0}},
				{{0,0.1,0.2,0.1},{0.2,0.1,0.2,0.2},{0.1,0.1,0.1,0},{0.1,0.2,0.1,0.1}},
				{{0.2,0.1,0.1,0.2},{0.2,0,0.1,0.2},{0.1,0.2,0.1,0.2},{0.2,0.1,0.1,0}}
		};
		
		please.feedforward(input_data);
		
		double back_prop_error[] = {-1,2,4,0};
		please.cnn_back_propagate(back_prop_error);
		//please.print_values();
		//please.print_weights();
		//please.print_weight_dev();
		//please.print_xdev();
		*/
		
		int data_depths[] = {3,2,2,2};
		int data_width_heights[] = {4,4,2,2};
		String layer_types[] = {"conv","pool","conv"};
		
		int conv_constant_wid_hid = 3;
		
		int pool_constant_depth = 1;
		int pool_constant_wid_hid = 2;
		
		int layer_depths[] = {2,pool_constant_depth,2};
		int layer_width_heights[] = {conv_constant_wid_hid,pool_constant_wid_hid,conv_constant_wid_hid};
		please = new cnn(data_depths,data_width_heights,layer_types,layer_depths,layer_width_heights);
		
		please.draw = true;
		
		/*for (int i=0;i<100;i++){
			please.feedforward(input_data);
			
			double output[][] = please.cnn_get_output();
			double back_prop_error[] = {output[0][0],output[0][1],output[1][0],output[1][1]};
			
			please.cnn_back_propagate(back_prop_error);
			please.cnn_gradient_descent();
		}*/
		
		//please.print_values();
		//please.print_weights();
		//please.print_weight_dev();
		//please.print_xdev();
		
		please.window.addMouseListener(new mouseevent());
		
		
	}
	
	private class mouseevent implements MouseListener{
		// Toggles whether the weights of a certain node should be shown. Done by clicking the node.
		double input_data[][][] = {
				{{0.2,0.2,0.1,0},{0.2,0.1,0,0.2},{0.2,0,0.1,0.2},{0,0.1,0.2,0}},
				{{0,0.1,0.2,0.1},{0.2,0.1,0.2,0.2},{0.1,0.1,0.1,0},{0.1,0.2,0.1,0.1}},
				{{0.2,0.1,0.1,0.2},{0.2,0,0.1,0.2},{0.1,0.2,0.1,0.2},{0.2,0.1,0.1,0}}
		};
		
		double two_input_data[][][] = {
				{{2,0.2,0.1,0},{0.2,0.1,0,0.2},{0.2,0,0.1,0.2},{0,0.1,0.2,0}},
				{{0,1,0.2,0.1},{0.2,0.1,0.2,0.2},{0.1,0.1,0.1,0},{0.1,0.2,0.1,0.1}},
				{{2,1,0.1,0.2},{0.2,0,0.1,0.2},{0.1,0.2,0.1,0.2},{0.2,0.1,0.1,0}}
		};
		public void mouseClicked(MouseEvent e) {
			// If the mouse was clicked on one of the nodes, toggle the drawweight property of the node and repaint
			// the window to reflect the change
			System.out.println(e.getPoint());
			if (e.getPoint().getX()<100){
				please.feedforward(two_input_data);
			}
			if (e.getPoint().getX()>800){
				please.cnn_gradient_descent(1,1);
			}
			if (e.getPoint().getY()<100){
				double output[][][] = please.cnn_get_output();
				double back_prop_error[] = {output[0][0][0],output[0][0][1],output[0][1][0],output[0][1][1],output[1][0][0],output[1][0][1],output[1][1][0],output[1][1][1]};
				please.cnn_back_propagate(back_prop_error);
				
			}
			if (e.getPoint().getY()>800){
				please.feedforward(input_data);
			}
			please.window.repaint();
			please.weights_window.repaint();
		}
		public void mouseEntered(MouseEvent e) {}
		public void mouseExited(MouseEvent e) {}
		public void mousePressed(MouseEvent e) {}
		public void mouseReleased(MouseEvent e) {}
	}
	
}
