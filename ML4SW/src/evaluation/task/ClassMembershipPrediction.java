package evaluation.task;


import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import knowledgeBasesHandler.KnowledgeBase;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLIndividual;

import classifiers.SupervisedLearnable;
import evaluation.CrossValidation;
import evaluation.Evaluation;
import evaluation.ConceptGenerator;
import evaluation.metrics.GlobalPerformanceMetricsComputation;

import utils.Triple;
import utils.Generator;




/**
 * Implementazione della class-membership con ENN
 * @author Giuseppe
 *
 */
public class ClassMembershipPrediction implements Evaluation {
	private static KnowledgeBase kb;
	private OWLIndividual[] allExamples;
	private OWLDescription[] testConcepts;
	private OWLDescription[] negTestConcepts;
	//	private OWLClass[] concetti;
	private int[][] classification;

	//	private String urlOwlFile;
	static final int QUERY_NB = 30;
	
	static final double THRESHOLD = 0.05;
	public PrintStream console = System.out;
	//	private RegolaCombinazione regola;
	public ClassMembershipPrediction(KnowledgeBase k){
		
		kb=k;
		allExamples=kb.getIndividuals();
		ConceptGenerator qg= new ConceptGenerator(kb);
		testConcepts=qg.generateQueryConcepts(Evaluation.NUMGENCONCEPTS);

		negTestConcepts=new OWLDescription[testConcepts.length];
		for (int c=0; c<testConcepts.length; c++) 
			negTestConcepts[c] = kb.getDataFactory().getOWLObjectComplementOf(testConcepts[c]);
		// Classification wrt all query concepts
		System.out.println("\nClassifying all examples ------ ");
		kb.getReasoner();
		//concetti=kb.getClasses();
		//		urlOwlFile=kb.getURL();
		classification=kb.getClassMembershipResult(testConcepts, negTestConcepts,allExamples);

	}


	/* (non-Javadoc)
	 * @see forest.Evaluation#bootstrap(int)
	 */
	@Override
	public  void bootstrap( int nFolds, String className ) throws Exception {
		System.out.println(nFolds+"-fold BOOTSTRAP Experiment on ontology: ");	

		Class<?> classifierClass =ClassLoader.getSystemClassLoader().loadClass(className);
		int nOfConcepts = testConcepts.length;
		
		GlobalPerformanceMetricsComputation gpmc = new GlobalPerformanceMetricsComputation(nOfConcepts,nFolds);

		// main loop on the folds
		int[] ntestExs = new int[nFolds];
		for (int f=0; f< nFolds; f++) {			

			System.out.print("\n\nFold #"+f);
			System.out.println(" **************************************************************************************************");

			Set<Integer> trainingExsSet = new HashSet<Integer>();

			Set<Integer> testingExsSet = new HashSet<Integer>();
			for (int r=0; r<allExamples.length; r++) 
				trainingExsSet.add(Generator.generator.nextInt(allExamples.length));

			for (int r=0; r<allExamples.length; r++) {
				if (! trainingExsSet.contains(r)) 
					testingExsSet.add(r);
			}			
			// splitting in growing and pruning set (70-30 ratio)

			Integer[] trainingExs = new Integer[0];
			Integer[] testExs = new Integer[0];
			trainingExs = trainingExsSet.toArray(trainingExs);
			testExs = testingExsSet.toArray(testExs);
			//			pruningSet=pruningExsSet.toArray(pruningSet);
			ntestExs[f] = testExs.length;
			//			System.setOut(new PrintStream("C:/Users/Utente/Documents/biopax.txt"));

			// training phase: using all examples but those in the f-th partition
			System.out.println("Training is starting...");
			new ArrayList<Triple<Integer, Integer, Integer>>();
			
			
			SupervisedLearnable cl=  (SupervisedLearnable)(classifierClass.getConstructor(KnowledgeBase.class, int.class)).newInstance(kb,testConcepts.length);
			cl.training(trainingExs, testConcepts, negTestConcepts);
//
//			}
			System.out.println("End of Training.\n\n");

			int[][] labels=cl.test(f, testExs, testConcepts);
			
			gpmc.computeMetricsPerFold(f, labels, classification, nOfConcepts, testExs);

		} // for f - fold look

		
		gpmc.computeOverAllResults(nOfConcepts);
	} // bootstrap DLDT induction	




//	private static OWLDescription getRandomConcept() {
//
//		OWLDescription newConcept = null;
//
//		do {
//			newConcept = kb.getClasses()[generator.nextInt(kb.getClasses().length)];
//			if (generator.nextDouble() < 0.5) {
//				OWLDescription newConceptBase = getRandomConcept();
//				if (generator.nextDouble() < 0.5)
//					if (generator.nextDouble() < 0.5) { // new role restriction
//						OWLObjectProperty role = kb.getRoles()[generator.nextInt(kb.getRoles().length)];
//						//					OWLDescription roleRange = (OWLDescription) role.getRange;
//
//						if (generator.nextDouble() < 0.5)
//							newConcept = kb.getDataFactory().getOWLObjectAllRestriction(role, newConceptBase);
//						else
//							newConcept = kb.getDataFactory().getOWLObjectSomeRestriction(role, newConceptBase);
//
//					}
//					else					
//						newConcept = kb.getDataFactory().getOWLObjectComplementOf(newConceptBase);
//			} // else ext
//			//			System.out.printf("-->\t %s\n",newConcept);
//			//		} while (newConcept==null || !(reasoner.getIndividuals(newConcept,false).size() > 0));
//		} while (!kb.getReasoner().isSatisfiable(newConcept));
//
//		return newConcept;		
//	}	



	/* (non-Javadoc)
	 * @see forest.Evaluation#crossValidation(int)
	 */
	@Override
	public void crossValidation(int nFolds, String className) {
		System.out.println(nFolds+"-fold CROSS VALIDATION Experiment on ontology:");		
		Class<?> classifierClass = null;
		try {
			classifierClass = ClassLoader.getSystemClassLoader().loadClass(className);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int nExs = allExamples.length;		

		CrossValidation cv = new CrossValidation(nFolds,nExs);

		//	OWLDescription[] testConcepts = allConcepts;
		int nTestConcepts = testConcepts.length;

//		double[][] totMatchingRate 		= new double[nTestConcepts][nFolds]; 	// per OWLDescription per fold
//		double[][] totCommissionRate 	= new double[nTestConcepts][nFolds]; 	// per OWLDescription per fold
//		double[][] totOmissionRate 		= new double[nTestConcepts][nFolds]; 	// per OWLDescription per fold
//		double[][] totInducedRate 		= new double[nTestConcepts][nFolds]; 	// per OWLDescription per fold
//		double[][] totPrecision 		= new double[nTestConcepts][nFolds]; 	// per OWLDescription per fold
//		double[][] totRecall 			= new double[nTestConcepts][nFolds]; 	// per OWLDescription per fold
		GlobalPerformanceMetricsComputation gbpmc= new GlobalPerformanceMetricsComputation(nTestConcepts,nFolds);
		

		// main loop on the folds
		for (int f=0; f< nFolds; f++) {			

//			int[] matchingNum = new int[nTestConcepts]; 	// per OWLDescription
//			int[] commissionNum = new int[nTestConcepts]; 	// per OWLDescription
//			int[] omissionNum = new int[nTestConcepts]; 	// per OWLDescription
//			int[] inducedNum = new int[nTestConcepts]; 		// per OWLDescription
//			double[] trueNum = new double[nTestConcepts]; 	// number of true examples per OWLDescription
//			double[] foundNum = new double[nTestConcepts]; 	// number of examples retrieved as true per OWLDescription
//			double[] hitNum = new double[nTestConcepts]; 	// number of hits per OWLDescription

			System.out.print("\n\nFold #"+f);
			System.out.println(" **************************************************************************************************");

			Integer[] trainingExs = cv.getTrainingExs(f);
			// test phase: test all examples in the f-th partition
//			Classificatore cl= new Classificatore(kb);
//			int indClassification = 1000000;
//			Ensemble<DLTree2>[] forests = new Ensemble [nTestConcepts]; 	
			
			
			
			SupervisedLearnable cl=null;
			try {
				cl = (SupervisedLearnable)(classifierClass.getConstructor(KnowledgeBase.class, int.class)).newInstance(kb,testConcepts.length);
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			cl.training(trainingExs, testConcepts, negTestConcepts);
			
				
				
				
				
				ArrayList<Integer> currentList= new ArrayList<Integer>();

				// keep track of all  test examples in the current split
				for (int te=0; te < cv.nPerFold; te++ ) { 

					int indTestEx = cv.getIndex(f,te);
					if (indTestEx != cv.UNASSIGNED) {
						
						currentList.add(indTestEx);
						
					}
				
				}
				Integer[] currentFold = new Integer[currentList.size()];
				currentFold= currentList.toArray(currentFold);
				
			 System.out.println("No esempi"+ currentFold.length);
			 int[][] labels=cl.test(f, currentFold, testConcepts);
				


//			System.out.println("\n\n >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> OUTCOMES FOLD #"+f);
//			System.out.printf("\n%10s %10s %10s %10s %10s %10s %10s\n", "Query#",  "matching", "commission", "omission", "induction", "precision", "recall");
//
//			double[] precision = new double[nTestConcepts];
//			double[] recall = new double[nTestConcepts];
//			double nCases = (f==cv.nOfFolds-1) ? cv.nPerFold- (cv.nOfPlaces - cv.nOfExs): cv.nPerFold;
//
//			for (int c=0; c < nTestConcepts; c++) {
//
//				totMatchingRate[c][f] = matchingNum[c]/nCases; 
//				totCommissionRate[c][f] = commissionNum[c]/nCases; 
//				totOmissionRate[c][f] = omissionNum[c]/nCases;  
//				totInducedRate[c][f] = inducedNum[c]/nCases;
//
//				totPrecision[c][f] = precision[c] = (hitNum[c]+1)/(foundNum[c]+1);
//				totRecall[c][f] = recall[c] = (hitNum[c]+1)/(trueNum[c]+1);
//
//				System.out.printf("%9d. %10.3f %10.3f %10.3f %10.3f %10.3f %10.3f\n", c, 
//						matchingNum[c]/nCases, commissionNum[c]/nCases, omissionNum[c]/nCases, inducedNum[c]/nCases, 
//						precision[c], recall[c] );
//
//			}
			 gbpmc.computeMetricsPerFold(f, labels, classification, testConcepts.length, currentFold);
			
		} // for f - fold look

		gbpmc.computeOverAllResults(nTestConcepts);


//		System.out.println("\n\n\n @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ OVERALL OUTCOMES");
//		System.out.printf("\n%10s %10s %10s %10s %10s %10s %10s\n", "Query#",  "matching", "commission", "omission", "induction", "precision", "recall");
//
//		double accMatchingAvgs = 0;
//		double accCommissionAvgs = 0;
//		double accOmissionAvgs = 0;
//		double accInductionAvgs = 0;
//		double accPrecisionAvgs = 0;
//		double accRecallAvgs = 0;
//
//		for (int c=0; c < nTestConcepts; c++) {
//			double AvgMatching = MathUtils.avg(totMatchingRate[c]);
//			double AvgCommission = MathUtils.avg(totCommissionRate[c]);
//			double avgOmission = MathUtils.avg(totOmissionRate[c]);
//			double avgInduction = MathUtils.avg(totInducedRate[c]);
//
//			double avgPrecision = MathUtils.avg(totPrecision[c]);
//			double avgRecall = MathUtils.avg(totRecall[c]);
//
//			System.out.printf("%10d %10.3f %10.3f %10.3f %10.3f %10.3f %10.3f \n", 
//					c, AvgMatching, AvgCommission, avgOmission, avgInduction, avgPrecision, avgRecall);
//			accMatchingAvgs += AvgMatching;
//			accCommissionAvgs += AvgCommission;
//			accOmissionAvgs += avgOmission;
//			accInductionAvgs += avgInduction;
//			accPrecisionAvgs += avgPrecision;
//			accRecallAvgs += avgRecall;
//		}
//		System.out.println("----------------------------------------------------------------------------------------------");
//		double matchingAvg 		= accMatchingAvgs/nTestConcepts;
//		double commissionAvg 	= accCommissionAvgs/nTestConcepts;
//		double omissionAvg 		= accOmissionAvgs/nTestConcepts;
//		double inductionAvg 	= accInductionAvgs/nTestConcepts;
//		double precisionAvg 	= accPrecisionAvgs/nTestConcepts;
//		double recallAvg 		= accRecallAvgs/nTestConcepts;
//
//
//		double theFMeasure = 2*precisionAvg*recallAvg / (precisionAvg + recallAvg);
//		System.out.printf("%10s %10.3f %10.3f %10.3f %10.3f %10.3f %10.3f \t F-measure: %10.3f\n", "AVERAGES", 
//				matchingAvg, commissionAvg, omissionAvg, inductionAvg, 
//				precisionAvg, recallAvg, theFMeasure);
	} // leaveOneOut

}