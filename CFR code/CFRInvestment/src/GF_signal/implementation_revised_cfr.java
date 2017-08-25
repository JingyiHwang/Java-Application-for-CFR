 package GF_signal;

import java.util.ArrayList;
import java.util.List;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;
//import org.apache.commons.math3.distribution.*;

public class implementation_revised_cfr {
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
		        
        String csvFilePath="/Users/huangjingyi/Desktop/imply2.csv";
		CsvWriter wr=new CsvWriter(csvFilePath);
		String[][] contents=new String[datalength][9];
					
		
		// 1.regret under training data
		double[] price=new double[datalength];
		int start=4911; //start position
			
		for (int i = 0; i < datalength; i++) {    
		        price[i]= Double.parseDouble(datas[i+start][4]);   //second index is column name, starting from zero
	    }	
			
		double[] regret1= {0,0,0};   //daily regret
		double[] regret2= {0,0,0};
		double[] regret3= {0,0,0};
			
		//regret[0]--short，regret[1]--wait，regret[2]--long
		double[] regretSum1= {0,0,0};  
        double[] regretSum2= {0,0,0};  
		double[] regretSum3= {0,0,0};  
			
		//prob initialization
		 double[] p1= {0, 1/2,1/2};
		 double[] p2= {1/2,1/2,0};
		 double[] p3= {1/3,1/3,1/3};
		 
		 
		 double sum1=0;
		 double sum2=0;
		 double sum3=0;
		 
		//position initialization
		int[] position1=new int[datalength];  //Long-only strategy
		int[] position2=new int[datalength];  //Long-short strategy
		position1[0]=0;
		position2[0]=0;

		
		//parameters
		double k=0.97; //forgetting parameter
		double p=0;
		//double miu=900;
		//double sigma=0.01;
		
		double cdf=0.5;
		//NormalDistribution ndist=new NormalDistribution(miu, sigma);
       
		
		//initial money
		double initial=200000;
		//follow the money change
		double money1=200000;//money1 for long-only strategy. capital
		double money2=200000; //money2 for long-short strategy, capital
		double money3=200000; //money3, long-only, money
		double money4=200000; //money4, long-short, money
		
		contents[0][0]=Double.toString(price[0]);
		contents[0][1]=Double.toString(0);
		contents[0][3]=Double.toString(0);
		wr.writeRecord(contents[0],true);	
		
		double value, value_long,value_short, value_pass;
	
		double cost=0, gain=0, cost2=0;
		
		for(int i=1; i<datalength; i++) {
			value=p3[2]*(price[i]-price[i-1])-p3[0]*(price[i]-price[i-1]);
			//expected utility, long utility, short utility
            value_long=price[i]-price[i-1];
			value_short=price[i-1]-price[i];
			value_pass=Math.abs(price[i]-price[i-1])/2;
			
		
			regretSum3[0]*=k;
			regretSum3[1]*=k;
			regretSum3[2]*=k;
			
			
			//1.Long side
			if (value_long>value) {
				regret1[2]=value_long-value;	
			}
			else {
				regret1[2]=0;
			}
			regretSum1[2]+=regret1[2];
			
			
			//price went down, regret for not waiting
			if (value_short>value) {
				regret1[1]=value_short-value;
			}
			else {
				regret1[1]=0;
			}
			regretSum1[1]+=regret1[1];
			
			
			
			//2.Short side
			if (value_short>value) {
				regret2[0]=value_short-value;	
			}
			else {
				regret2[0]=0;
			}
			regretSum2[0]+=regret2[0];
			
			
			
			if (value_long>value) {
				regret2[1]=value_long-value;
			}
			else {
				regret2[1]=0;
			}
			regretSum2[1]+=regret2[1];
			
			
			//3.price chaser
	
			if (value_long>value) {
				regret3[2]=value_long-value;	
			}
			else {
				regret3[2]=0;
			}
			regretSum3[2]+=regret3[2];
			
			
	
			if (value_short>value) {
				regret3[0]=value_short-value;
			}
			else {
				regret3[0]=0;
			}
			regretSum3[0]+=regret3[0];
			

			if(value_pass>value) {
				regret3[1]=value_pass-value;
			}
			else {
				regret3[1]=0;
			}
			regretSum3[1]+=regret3[1];
			
			//pay attention, sum must return to 0 at this point
			sum1=0;
			sum2=0;
			sum3=0;
			
			for(int j=0;j<3;j++) {
				sum1+=regretSum1[j];
				sum2+=regretSum2[j];
				sum3+=regretSum3[j];
			}
		
			//whether regretSum is bigger than zero
			if (sum1>0 && regretSum1[1]>0 && regretSum1[2]>0) {
				for(int j=0;j<3;j++) {
					p1[j]=regretSum1[j]/sum1;	
				}
			}
			if (sum2>0 && regretSum2[1]>0 && regretSum2[0]>0) {
				for(int j=0;j<3;j++) {
					p2[j]=regretSum2[j]/sum2;	
				}
			}
			if (sum3>0 && regretSum3[1]>0 && regretSum3[2]>0 && regretSum3[0]>0) {
				for(int j=0;j<3;j++) {
					p3[j]=regretSum3[j]/sum3;	
				}
			}
			
			 cdf= 0.5;
			
			//signal for long and short
			 double longsignal=p1[2]*p*(1-cdf)+p3[2]*(1-p);
			 double shortsignal=p2[0]*p*cdf+p3[0]*(1-p);
			 double waitsignal=p1[1]*p*(1-cdf)+p3[1]*(1-p)+p2[1]*p*cdf;
			 
		
			
			System.out.println(longsignal-waitsignal);
			//Long-only strategy
			if (position1[i-1]==0) {
			
				if(longsignal-shortsignal>0.05 && longsignal-waitsignal>0.05)  // (p2[0]<0.45 || p1[2]>0.55 || p3[0]<0.45)
					position1[i]=1;
				else
					position1[i]=0;
			}
			else {
				if (longsignal-shortsignal<-0.02  || longsignal-waitsignal<-0.02)                 // (p2[0]>0.5 || p1[2]<0.5 || p3[0]>0.5)
					position1[i]=0;
				else 
					position1[i]=1;
			}

			
			//Long-short strategy
			if (position2[i-1]==0) {
				if(shortsignal-longsignal>0.02 && shortsignal-waitsignal>0.02)//((p2[0]>0.55 || p1[2]<0.45) && p3[2]<0.45 )
					position2[i]=-1;
				else if(longsignal-shortsignal>0.05 && longsignal-waitsignal>0.05) //((p2[0]<0.45 || p1[2]>0.55 )&& p3[0]<0.45)
					position2[i]=1;
				else
					position2[i]=0;
			}
			else if (position2[i-1]==1) {
				if(longsignal-shortsignal<-0.02 || longsignal-waitsignal<-0.02)          //((p2[0]>0.5 || p1[2]<0.5) && p3[0]>0.5)
					position2[i]=0;
				else 
					position2[i]=1;
			}
			else {
				if(shortsignal-longsignal<-0.02 || shortsignal-waitsignal<-0.02)              //((p2[0]<0.5 || p1[2]>0.5) && p3[0]<0.5)
					position2[i]=0;
				else
					position2[i]=-1;
			}
			
			//cumulative rate of return
			
			//1.Long only rate of return
            
			//1.1 transaction cost=10bp
			if(position1[i]!=position1[i-1]) {
				money1-=price[i]*100*(0.1*0.01);
				money3-=price[i]*100*(0.1*0.01);	
			}
			
			//1.2 holding the stock
			if(position1[i]==1 && position1[i-1]==1) {
				money1+=(price[i]-price[i-1])*100;
			}
			
			//1.3  buy and sell stock
			if(position1[i]==1 && position1[i-1]==0 ) {     //buy at time i
				cost2=price[i]*100*(1+0);
			}
					
			else if(position1[i]==0 && position1[i-1]==1) {
				money3+=price[i]*100-cost2;
			}
			
			
			
			//2. Long-short strategy
            
			//2.1transaction cost=10bp
			if(position2[i]-position2[i-1]==1 || position2[i]-position2[i-1]==-1) {
				money2-=price[i]*100*(0.1*0.01);
				money4-=price[i]*100*(0.1*0.01);
			}
			
			//2.2 holding the stock
			if(position2[i]==1 && position2[i-1]==1) {  //stay long at time i
				money2+=(price[i]-price[i-1])*100;
			}
			else if (position2[i]==-1 && position2[i-1]==-1) {   //stay short at time i
				money2+=(price[i-1]-price[i])*100;
			}
			
			
			
		   //2.3 buy and sell stocks
			if(position2[i]==1 && position2[i-1]==0 ) {     //buy at i
				cost=price[i]*100*(1+0);
			}
			else if(position2[i]==-1 && position2[i-1]==0) {     //sell at i
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



