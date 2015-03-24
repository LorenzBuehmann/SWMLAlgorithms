

import java.io.PrintStream;

import evaluation.Evaluation;
import evaluation.Parameters;
import evaluation.task.AffiliationPrediction;
import evaluation.task.MutagenicPrediction;
import evaluation.task.ClassMembershipPrediction;
import evaluation.task.PoliticianGenerator;
import evaluation.task.PoliticianPrediction;

import knowledgeBasesHandler.KnowledgeBase;




public class Main {
	
static KnowledgeBase kb;
//	static int[][] classification;
 public  PrintStream console = System.out;
	
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		Parameters.loadParameters(); //loading from property file
		
//		Locale.setDefault(Locale.US);
		
		
		Evaluation vcm= null; 
		switch (Parameters.task) {
		case CLASSMEMBERSHIPREDICTION:
			kb = new KnowledgeBase(Parameters.urlOwlFile);
			vcm= new ClassMembershipPrediction(kb);
		break;
		
		case MUTAGENICPREDICTION:
			kb = new KnowledgeBase(Parameters.urlOwlFile);
			if (Parameters.mutagenicAsDataPropertyPrediction)
				vcm=new MutagenicPrediction(kb);
			else
				vcm=new MutagenicPrediction(kb,true);
		 break;

		case POLITICIANPREDICTION: 
			kb = new KnowledgeBase(Parameters.urlOwlFile);
			vcm= new PoliticianPrediction(kb);
			break;
		 
		case AFFILIATIONPROBLEM:
			 kb = new KnowledgeBase(Parameters.urlOwlFile);
			 vcm= new AffiliationPrediction(kb);
			break;
		default:
//			kb =new KnowledgeBaseForRoughConceptLearning(Parameters.urlOwlFile);
//			vcm= new PUClassMembershipPrediction(kb);
		}
//		
	String className =  Parameters.algorithm.toString(); 		// package name
//		
//		
		switch (Parameters.design) {
		case BOOTSTRAP:
		
		vcm.bootstrap(Parameters.NFOLDS,className); // n. of folds		
		break;
		
		default:
			vcm.crossValidation(Parameters.NFOLDS,className); 
		
		break;	
		}
////		
	System.out.println("\n\nEnding: "+Parameters.urlOwlFile);

	} // main
	
	


} // class DLTreeInducer