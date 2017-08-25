package GF_signal;

import java.util.ArrayList;
import java.util.List;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;


public class implementation {

	public static void main (String[] args) throws Exception {
		
		//read file- function
		String csvFilePath2="/Users/huangjingyi/Desktop/result.csv";
			   
	    CsvReader re=new CsvReader(csvFilePath2);
		       
	    re.readHeaders();   
		  
	    List<String[]> list = new ArrayList<String[]>();   
		while (re.readRecord()) {   
			list.add(re.getValues());   
		}   
	    String[][] datas = new String[list.size()][]; 
	    
		for (int i = 0; i < list.size(); i++) {   
		    datas[i] = list.get(i);   
	    }  
		     
		
		int datalength=1458;
		
        String csvFilePath="/Users/huangjingyi/Desktop/imply.csv";
		CsvWriter wr=new CsvWriter(csvFilePath);
		String[][] contents=new String[datalength][9];
				
        double[] price=new double[datalength];
		int start=4911; //start position
			
		 for (int i = 0; i < datalength; i++) {    
		        price[i]= Double.parseDouble(datas[i+start][4]);   //second index is column, starting from 0
		 }	
			
		double[] regret= {0,0};   //daily regret
		//regret[0]--short，regret[1]--long
		double[] regretSum= {0,0};   
		//prob initialization
		double[] p= {1/2, 1/2};
		double sum=0;
		//position initialization
		int[] position1=new int[datalength];  //timing for long-only
		int[] position2=new int[datalength];  //timing for long-short
		position1[0]=0;
		position2[0]=0;
		
		//forgetting parameter
		double k=0.97;
		//initial money
		double initial=200000;
		//follow the money change
		double money1=200000;//money1 Long-only，capital
		double money2=200000; //money2 Long-short，capital
		double money3=200000; //money3 long-only，money
		double money4=200000; // money4 long-short, money
		
		contents[0][0]=Double.toString(price[0]);
		contents[0][1]=Double.toString(0);
		contents[0][3]=Double.toString(0);
		wr.writeRecord(contents[0],true);	
		
		double value, value_long,value_short;
		
		double cost=0,gain=0,cost2=0;
		
		for(int i=1; i<datalength; i++) {
            //expected utility, long utility, short utility
			value=p[1]*(price[i]-price[i-1])-p[0]*(price[i]-price[i-1]);
			value_long=price[i]-price[i-1];
			value_short=price[i-1]-price[i];
			
			//multiply by k
			regretSum[0]*=k;
			regretSum[1]*=k;
        
			//regret for not buying
			if (value_long>value) {
				regret[1]=value_long-value;	
			}
			else {
				regret[1]=0;
			}
			regretSum[1]+=regret[1];
			
			//regret for not selling
			if (value_short>value) {
				regret[0]=value_short-value;
			}
			else {
				regret[0]=0;
			}
			regretSum[0]+=regret[0];
			
			//remember to reset sum=0
			sum=0;
			for(int j=0;j<2;j++) {
				sum+=regretSum[j];
			}
		
			//whether regretSum>0 or not
			if (sum>0 && regretSum[1]>0 && regretSum[0]>0) {
				for(int j=0;j<2;j++) {
					p[j]=regretSum[j]/sum;	
				}
			}
			
			//timing for Long-only strategy
			if (position1[i-1]==0) {
				if (p[0]<0.45)
					position1[i]=1;
				else
					position1[i]=0;
			}
			else {
				if(p[0]>0.5)
					position1[i]=0;
				else 
					position1[i]=1;
			}

			
			//timing for Long-short strategy
			if (position2[i-1]==0) {
				if(p[0]>0.55)
					position2[i]=-1;
				else if (p[0]<0.45)
					position2[i]=1;
				else
					position2[i]=0;
			}
			else if (position2[i-1]==1) {
				if(p[0]>0.5)
					position2[i]=0;
				else 
					position2[i]=1;
			}
			else {
				if(p[0]<0.5)
					position2[i]=0;
				else
					position2[i]=-1;
			}
			
			//calculating cumulative rate of return
			
			//1.Long-only strategy
			//1.1 transaction cost=10bp
    
			if(position1[i]!=position1[i-1]) {
				money1-=price[i]*100*(0.1*0.01);	
				money3-=price[i]*100*(0.1*0.01);	
			}
			//1.2 holding the stock
			if(position1[i]==1 && position1[i-1]==1) {
				money1+=(price[i]-price[i-1])*100;
			}
			
			//1.3 buying and selling the stock
			if(position1[i]==1 && position1[i-1]==0 ) {     //buy at time i
				cost2=price[i]*100*(1+0);
			}
					
			else if(position1[i]==0 && position1[i-1]==1) {
				money3+=price[i]*100-cost2;
			}

			
			
			//2. Long-short strategy
			//2.1 transaction cost=10bp

			//position2 can be -1，0，1.-1 for selling，0 for waiting，1 for buying
			
			if(position2[i]-position2[i-1]==1 || position2[i]-position2[i-1]==-1) {     //buy at time i
				money2-=price[i]*100*(0.1*0.01);
				money4-=price[i]*100*(0.1*0.01);
			}
			
			//2.2 holding the stock
			if(position2[i]==1 && position2[i-1]==1) {
				money2+=(price[i]-price[i-1])*100;
			}
			else if (position2[i]==-1 && position2[i-1]==-1) {
				money2+=(price[i-1]-price[i])*100;
			}
			
			
			
		   //2.3 buying and selling stocks
			if(position2[i]==1 && position2[i-1]==0 ) {
				cost=price[i]*100*(1+0);
			}
			else if(position2[i]==-1 && position2[i-1]==0) {
                gain=price[i]*100*(1-0);
			}
			
			else if(position2[i]==0 && position2[i-1]==1) {
				money4+=price[i]*100-cost;
			}
			else if(position2[i]==0 && position2[i-1]==-1) {
				money4+=gain-price[i]*100;
			}
			
	
			
			
			
			contents[i][0]=Double.toString(price[i]);
			contents[i][1]=Double.toString(position1[i]);
			contents[i][2]=Double.toString(money1/initial-1);
			contents[i][3]=Double.toString(money3/initial-1);
			contents[i][4]=Double.toString(money3);
			contents[i][5]=Double.toString(position2[i]);
			contents[i][6]=Double.toString(money2/initial-1);
			contents[i][7]=Double.toString(money4/initial-1);
			contents[i][8]=Double.toString(money4);
			
			
			wr.writeRecord(contents[i],true);	
		}
		wr.close();
	}
}



