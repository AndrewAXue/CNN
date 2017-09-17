package cnn;
import java.io.IOException;

public class moretesting {
	public static void main(String[] args) throws IOException {
	    new moretesting().go();
	}
	int lst[] = {3,5};
	public void go(){
		int temp[] = returnarr();
		temp[0] = -1;
		System.out.println(lst[0]+" "+lst[1]);
	}
	int[] returnarr(){
		return lst;
	}
}

