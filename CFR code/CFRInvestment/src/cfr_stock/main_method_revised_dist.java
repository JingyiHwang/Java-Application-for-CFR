package cfr_stock;

import java.util.ArrayList;
import java.util.List;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;

import cfr_stock.DeepLearning;

// this revised main_method drops the assumption that long and short sides follow normal dist，now we just assume that they are half half.
//if we use normal dist, cdf is always 1, because it will be from minus infinity to infinity. and learningsheet shows that std is always 0.01(because i set it as >=0.01), and miu is always the initial value before minimization
//for training the parameter, use "learningsheet2.xlsx"

public class main_method_revised_dist {
public static void main (String[] args) throws Exception {
		
		//read and write file function
		String csvFilePath2="/Users/huangjingyi/Desktop/result.csv"; //read price data
		String csvFilePath3="/Users/huangjingyi/Documents/Summer/HUATAI/learningsheet.csv"; //write a file for step 2.
		CsvWriter wr2=new CsvWriter(csvFilePath3);
		
	   
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
		        
		        
		//A file used to show the final result--"test.csv"    
		String csvFilePath="/Users/huangjingyi/Desktop/test.csv";
		CsvWriter wr=new CsvWriter(csvFilePath);
		
		//length of training data
		int traininglen=100;
		//forecasting length for the future price
		int forecastlen=50;
		
		String[] contents=new String[forecastlen];
		
		//A file used to run step 2 in the learning sheet--"learningsheet.csv"	
		String[][] contents2=new String[traininglen][3];
				
		
		// 1.Calculate the regret under training data
		//learn 100 days price data & actions of the three parties.
		double[] price=new double[traininglen]; //price
		int[]   card1=new int[traininglen];     //card means different parties' action. Card1 is long-buyer.
		int[]   card2=new int[traininglen];     //Card2 is short-seller
		int[]   card3=new int[traininglen];     //Card3 is following the herd--chasing after going up and killing drop
		
		//start position index
		int position=887;
		
	
		//At least, we strat from MACD AND RSI != NAN
		for (int i = 0; i < traininglen; i++) {    
			   //Can check whether close price is in column 4
			   price[i]= Double.parseDouble(datas[i+position][4]);   //the second index is column index, it starts from 0
			   //System.out.println(price[i]);   //output check
			   
			   //card1 represents for long side's actions
			   //card2 represents for short side's actions
			   if(Double.parseDouble(datas[i+position][8]) < Double.parseDouble(datas[i+position][9]) ){
				   card1[i]=2;
				   card2[i]=1;
			   }
			   else {
				   card1[i]=1;
				   card2[i]=0;
			   }
			   
			   //card3 represents for price chaser's actions
			   if(Double.parseDouble(datas[i+position][10])>0 && Double.parseDouble(datas[i+position][11])>0 && Double.parseDouble(datas[i+position][11])>Double.parseDouble(datas[i+position-1][11])) {
				   card3[i]=2;
			   }
			   else if(Double.parseDouble(datas[i+position][10])<0 && Double.parseDouble(datas[i+position][11])<0 && Double.parseDouble(datas[i+position][11])<Double.parseDouble(datas[i+position-1][11])) {
				   card3[i]=0;
			   }
			   else
				   card3[i]=1;
			  
			  //learningsheet2, for parameter learning
			  contents2[i][0]=Double.toString(card1[i]);
			  contents2[i][1]=Double.toString(card2[i]);
			  contents2[i][2]=Double.toString(card3[i]);
			  wr2.writeRecord(contents2[i],true);
			  
			  
		}	
		wr2.close();
		
		
		
		//iterate for 20 times, using monte carlo to simulate the next move
		//the iteration length can be changed, depending on how many simulations you want
		for(int iteration=0; iteration<20; iteration++) {	
			
			//daily regret
			double[] regret1= {0,0,0};   //long
			double[] regret2= {0,0,0};  //short
			double[] regret3= {0,0,0};  //price chaser
			
			//cumulator regret function
			double[] regretSum1= {0,0,0};   
			double[] regretSum2= {0,0,0}; 
			double[] regretSum3= {0,0,0};
				
	        //learning the training data
			
			//Long side
			
			for(int i=1; i<traininglen; i++) {
				//the minus base is your actual utility, because your action at that time is known
	
				//pay attention to the direction of the equation. The bigger regret this action got, the higher prob it is gonna happen in the future.
                //eg. if price got higher, regret for action 2(buy); if price got lower, regret for action 0(sell)
				
				//buy the stock, long side may be regret for not waiting, because price got down
				if (card1[i]==2) {
					regret1[1]=price[i-1]-price[i]>0? price[i-1]-price[i]:0;
					regretSum1[1]+=regret1[1];
				}
				
				else {
					regret1[2]=price[i]-price[i-1]>0? price[i]-price[i-1]:0;
					regretSum1[2]+=regret1[2];
				}
			}
			
			//Short side
			
			for(int i=1; i<traininglen; i++) {
				
				if (card2[i]==0) {
					regret2[1]=price[i]-price[i-1]>0? (price[i]-price[i-1]):0;
					regretSum2[1]+=regret2[1];
				}
				
				
				else {
					regret2[0]=price[i-1]-price[i]>0? price[i-1]-price[i]:0;
					regretSum2[0]+=regret2[0];
				}
			}
			
			//Price chaser
			
			for(int i=1; i<traininglen;i++) {
				
				if(card3[i]==1) {
					regret3[2]=price[i]-price[i-1]>0? (price[i]-price[i-1])/2:0;
					regret3[0]=price[i-1]-price[i]>0? (price[i-1]-price[i])/2:0;
				    regretSum3[2]+=regret3[2];
				    regretSum3[0]+=regret3[0];
				    
				}
				
				else if(card3[i]==2) {
					regret3[1]=price[i-1]-price[i]>0? (price[i-1]-price[i])/2:0;
					regret3[0]=price[i-1]-price[i]>0? (price[i-1]-price[i]):0;
					regretSum3[1]+=regret3[1];	
					regretSum3[0]+=regret3[0];
				}
				
				else {
					regret3[2]=price[i]-price[i-1]>0? price[i]-price[i-1]:0;
					regret3[1]=price[i]-price[i-1]>0? (price[i]-price[i-1])/2 :0;
					regretSum3[1]+=regret3[1];
					regretSum3[2]+=regret3[2];
				}
		
			}
			
			
			double[] p1= {1/3,1/3,1/3};  //Long side, prob initialization
			double[] p2= {1/3,1/3,1/3}; // Short side, prob initialization
			double[] p3= {1/3,1/3,1/3}; //Price chaser, prob initialization
			
			//if the summation of cumulative regret is not positive, use prob initialization
            double summ1=0;
			double summ2=0;
			double summ3=0;
			for(int i=0;i<3;i++) {
				summ1=regretSum1[i]+summ1;
				summ2=regretSum2[i]+summ2;
				summ3=regretSum3[i]+summ3;
			}
			
			if (summ1>0) {
				for(int j=0;j<3;j++) {
					p1[j]=regretSum1[j]/summ1;	
				}
			}
			if(summ2>0) {
				for(int j=0;j<3;j++) {
					p2[j]=regretSum2[j]/summ2;
				}
			}
			if (summ3>0) {
				for(int j=0;j<3;j++) {
					p3[j]=regretSum3[j]/summ3;	
				}
			}
			
			
		// 2.use training data again to train the parameter
        //It is in learningsheet2.xlxs, we can use data->solver
		double p=1;
		double range=0.01;//range must be close to the real range in training data. Because the later regretSum is based on the regretSum we got from training, if it is so not close, the bigger part will occupy a dominant

		double[] rate= {0.5*p,0.5*p,1-p};
		double cdf=0.5;
    
        rate[0]=p*(1-cdf);
		rate[1]=p*cdf;
		
		
		// 3. calculate the updated regret by monte carlo, and forecast future price
        //pass +0; fall -1; bet +1.
		DeepLearning test3 = new DeepLearning();
		DeepLearning test4 = new DeepLearning();
		DeepLearning test6 = new DeepLearning();
		int b1 = test3.getWeightedRandom(p1);  //Long side, next move
		int b2 = test4.getWeightedRandom(p2); //Short side, next move
		int b3 = test6.getWeightedRandom(p3); //Price chaser, next move
		
		//output test.
		//System.out.println(b1);
		//System.out.println(b2);	
		//System.out.println(b3);	
		
		double[] delta = new double[3];
		
		delta[0]=test3.getDelta(b1);
		delta[1]=test3.getDelta(b2);
		delta[2]=test3.getDelta(b3);
		
		// want to forcast how many-- “forcastlen” --of future price
		
		double[] price2=new double[forecastlen];
		

		price2[0]= price[traininglen-1];
		
		
		for (int i=1; i<forecastlen; i++) {
			price2[i]=price2[i-1]+(range*delta[0]*rate[0])+(range*delta[1]*rate[1])+(rate[2]*range*delta[2]);
			DeepLearning.Node test5= test4.new Node(); //inner class
			
			for(int j=0; j<3;j++) {
				regretSum1[j] += test5.getRegret1(p1, price2[i], price2[i-1])[j];
				regretSum2[j] += test5.getRegret2(p2, price2[i], price2[i-1])[j];
				regretSum3[j] += test5.getRegret3(p3, price2[i], price2[i-1])[j];
			}
			
			//System.out.println(test5.getRegret3(p3, price2[i], price2[i-1])[0]);
			//System.out.println(regretSum3[0]);
			
			p1=test5.getStrategy(regretSum1);
			p2=test5.getStrategy(regretSum2);
			p3=test5.getStrategy(regretSum3);
			
			b1=test3.getWeightedRandom(p1);  //Long side next move
			b2=test4.getWeightedRandom(p2); //Short side next move
			b3=test6.getWeightedRandom(p3);   //price chaser next move
			
			delta[0]=test4.getDelta(b1);
			delta[1]=test4.getDelta(b2);
			delta[2]=test4.getDelta(b3);
			
			//update rate of people in the market
            cdf= 0.5;
			
            rate[0]=p*(1-cdf);
			rate[1]=p*cdf;
			
			//System.out.println(b3);
			System.out.println(rate[1]);
			
			
			//System.out.print(price2[i]+",");	
			contents[i]=Double.toString(price2[i]);
			
		}
	
		wr.writeRecord(contents,true);	
	}
	wr.close();
	}
}


