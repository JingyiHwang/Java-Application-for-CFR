package cfr_stock;
import java.util.Random;
//import java.util.Arrays;
//import java.util.TreeMap;


public class DeepLearning {
	//Define three behaviors
	public static final int Fall=0, PASS=1, BET=2, NUM_ACTIONS=3;
	public static final Random random=new Random();
	
	//Shuffle cards
	//Testing numbers
	public  void train(int[] cardds,int iterations) {
		for (int i=0; i<iterations; i++) {
			// Shuffle cards
			for (int c1=cardds.length-1; c1>0;c1--) {
				Random random=new Random();
				int c2=random.nextInt(c1+1); //Develop random numbers from 0 to c1
				int tmp=cardds[c1];
				cardds[c1]=cardds[c2];
				cardds[c2]=tmp;				
				}
		}
	}
	
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
	
	//Information set node class definition
	public class Node{
		// Kuhnpoker node definitions
		String  infoSet;
		double[] regret=new double[NUM_ACTIONS];  
		double[]	 Strategy=new double[NUM_ACTIONS];
		double[] strategySum=new double[NUM_ACTIONS];
		double value=0;
		
		//Build regret function
		//parameter
		//Long side
		public double[] getRegret1(double[] p,double p1,double p0) {
			value=p[2]*(p1-p0);
			//Higer price, but did not buy
            regret[2]=p1-p0-value>0? (p1-p0-value):0;
				    
		   //Lower price, but did not wait
			regret[1]=p0-p1-value >0? (p0-p1-value):0;
			
            //regret[0] just need to be zero
			regret[0]=0;
			
			return regret;
		}
		
			
		//Short side
		public double[] getRegret2(double[] p, double p1,double p0) {
			
			 value=p[0]*(p0-p1);   
			//Price went down, but did not sell
			regret[0]=p0-p1-value>0?(p0-p1-value):0;
			//price went up, but did not wait
			regret[1]=p1-p0-value>0? (p1-p0-value):0;
			//regret[2], just need to be zero
			regret[2]=0;
			return regret;
		}
		
		public double[] getRegret3(double[] p, double p1, double p0) {
			//price chaser
			value=p[0]*(p0-p1)+p[2]*(p1-p0);
			//price went down, but did not sell
			regret[0]=p0-p1-value>0? (p0-p1-value):0;
			//price went up and down, but did not wait
			regret[1]=Math.abs(p1-p0)-value>0? ((Math.abs(p1-p0)-value)/2):0; 
			//price went up, but did not buy
			regret[2]=p1-p0-value>0? (p1-p0-value):0;
			
			return regret;	
		}

			
		
		// Get current information set mixed strategy through regret-matching 
		public double[] getStrategy(double[] regretSum) {
				double summ=0;
				for (int a=0;a<NUM_ACTIONS; a++) {
					//Strategy[a]=regret[a]>0? regret[a]:0;
					//regretSum[a] +=Strategy[a];
					summ +=regretSum[a];
				}
				//still need to avoid the consequence caused by some prob=1
				
				if(summ>0 && (regretSum[1]/summ)<1 && (regretSum[0]/summ)<1 && (regretSum[2]/summ)<1) {
					for (int i=0; i<NUM_ACTIONS; i++) {
						Strategy[i]=regretSum[i]/summ;
					}
				}
				else {
					for(int i=0; i<NUM_ACTIONS; i++) {
						Strategy[i]=1.0/NUM_ACTIONS;
					}
				}
						
				//   strategySum[a] +=realizationWeight*Strategy[a];
				return Strategy;
		}
	}
	
	//fall means price -1, pass means price stay unchanged, bet means price +1
    //Delta is just a presentation for up and down.(+1/-1)，
	public double getDelta(int b) {
		double delta;
		if (b==0)
			delta=-1;
		else if (b==1)
			delta=0;
		else
			delta=1;
		return delta;
	}
	
	public double getDelta2(int b2) {
		double delta;
		if(b2==0)
			delta=0.5;
		else if (b2==1)
			delta=0;
		else
			delta=-1;
		return delta;
	}
	
	public class TrainingNode{
		int getAction (double a1, double a0) {
			if(Math.abs(a1-a0)<1)
				return 1;
			else if(a1-a0>1)
				return 2;
			else
				return 0;	
		}
		
	}
}

		

	
