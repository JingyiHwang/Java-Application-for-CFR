package GF_cfr;


import com.csvreader.CsvWriter;

import java.util.ArrayList;
import java.util.List;

import com.csvreader.CsvReader;

import GF_cfr.forecast_funcs;
//import cfr_stock.DeepLearning.Node;

//Note for use：
//csv files：test_gf，result
//1.looking for the parameter “position”，it is the index where you start; and“traininglen”  “forecastlen”
//2.looking for "range" in step 2，follow the learning result in cfr_stock, put the value in code
//3.run the code again, get the forecasting result in "test_gf"

public class mainfunction {
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
	        
		String csvFilePath="/Users/huangjingyi/Desktop/test_gf.csv";
		CsvWriter wr=new CsvWriter(csvFilePath);
		//length for training and forecasting
		int traininglen=100;
		int forecastlen=50;
		
		String[] contents=new String[forecastlen];
			
	
	// 1.calculate the regret under training data
	double[] price=new double[traininglen];
	
	//start position
	int position=887;
	
	//iterate for 20 times
	for(int iteration=0; iteration<20; iteration++) {	
		 for (int i = 0; i < 100; i++) {    
	            price[i]= Double.parseDouble(datas[i+position][4]);   //second index is the column index, it starts from zero
		 }
	         
		
		double[] regret= {0,0};   //record daily regret
		//regret[0] for short，regret[1] for long
		double[] regretSum= {0,0};   //cumulative regret
		
		
		for(int i=1; i<traininglen; i++) {
			//price went high, regret for not buying
			if (price[i]>price[i-1]) {
				regret[1]=price[i]-price[i-1];
				regretSum[1]+=regret[1];
			}
			//price went down, regret for not selling
			else {
				regret[0]=price[i-1]-price[i];
				regretSum[0]+=regret[0];
			}
		}
		
		double[] p= {1/2,1/2};  //prob initialization
	
		//whether the sum of cumulative regret is zero
		double summ=0;
	
		for(int i=0;i<2;i++) {
			summ=regretSum[i]+summ;
		}
		
		if (summ>0) {
			for(int j=0;j<2;j++) {
				p[j]=regretSum[j]/summ;	
			}
		}
		
	// 2. simulate by monte carlo
        
    double range=0.025;
	
	//3. forcast future price
	forecast_funcs test4 = new forecast_funcs();
	
	int b=test4.getWeightedRandom(p);  //next move
	
	double delta=0;
	
	delta=test4.getDelta(b);
	
	//forecast next "forecastlen" prices
	
	double[] price2=new double[forecastlen];
	

	price2[0]= price[traininglen-1];
	
	//regret function
	double value=0;
	for (int i=1; i<forecastlen; i++) {
		price2[i]=price2[i-1]+delta*range;
		value=(p[1]*delta-p[0]*delta)*range;
		if(delta>0)
			regretSum[1] += delta*range-value;
		else
			regretSum[0] += -delta*range-value;
		
		
		
		p=test4.getStrategy(regretSum);
		b=test4.getWeightedRandom(p);  //next move
		
		delta=test4.getDelta(b);
		
		
		System.out.println(p[0]);	
		contents[i]=Double.toString(price2[i]);
		
		}
	//	System.out.println();	
		wr.writeRecord(contents,true);	
	}
	wr.close();
	
}
}

