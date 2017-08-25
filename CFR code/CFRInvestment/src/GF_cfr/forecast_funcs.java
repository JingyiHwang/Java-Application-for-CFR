package GF_cfr;
import java.util.Random;

public class forecast_funcs {
	
	public static final int Fall=0, BET=1, NUM_ACTIONS=2;
	public static final Random random=new Random();
	//public TreeMap<String,Node> nodeMap=new TreeMap<String,Node>();
	
	
	//simulate next move
	public int getWeightedRandom (double[] prob) { 
		
			/*double ii=0;
			for(int i=0; i<prob.length;i++) {
        			ii+=i;	
			}
		    	//might be the reason of precision
			if (ii!=1) {
				System.out.println("Wrong input in getWeightedRandom!");
		      	return -1;
			}  */
		    		
		   double stepWeightSum = 0;  
		   double a=Math.random();
		   for (int i = 0; i < prob.length; i++) { 
		        stepWeightSum += prob[i];
		        	if (a <= stepWeightSum) {   
		            return i; 
		         }  
		   } 
		   System.out.println("Error in getWeightedRandom");
		   return -1;
	}
	
	

	double[] Strategy= {1/2,1/2};	
		
		// Get current information set mixed strategy through regret-matching 
	public double[] getStrategy(double[] regretSum) {
			double summ=0;
			for (int a=0;a<NUM_ACTIONS; a++) {
				summ += regretSum[a];			
			}
			for (int a=0; a<NUM_ACTIONS; a++) {
				if (summ>0)
					Strategy[a]=regretSum[a]/summ;
				else
					Strategy[a]=1.0/NUM_ACTIONS;
				//   strategySum[a] +=realizationWeight*Strategy[a];
			}
		
			return Strategy;
	}

	//fall =price-1，pass=price unchanged，bet=price+1
	//Delta is just a signal, describing the change direction of the pirce
	public double getDelta(int b) {
		double delta;
		if (b==0)
			delta=-1;
		else
			delta=1;
		return delta;
	}
		

}
