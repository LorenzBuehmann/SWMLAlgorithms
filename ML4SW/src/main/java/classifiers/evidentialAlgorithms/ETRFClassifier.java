package classifiers.evidentialAlgorithms;




import java.util.ArrayList;

import org.semanticweb.owlapi.model.OWLClassExpression;

import knowledgeBasesHandler.KnowledgeBase;
import samplers.BalancedDataset;
import utils.Triple;
import classifiers.ensemble.Ensemble;
import classifiers.evidentialAlgorithms.DempsterShafer.MassFunction;
import classifiers.evidentialAlgorithms.models.DSTDLTree;
import evaluation.Parameters;

public class ETRFClassifier {
	

	private DSTTDTClassifier data;
	public ETRFClassifier(KnowledgeBase kb) {
		 data = new DSTTDTClassifier(kb);
	}

	
	public Ensemble<DSTDLTree> induceDLForest(ArrayList<Integer> posExs, ArrayList<Integer> negExs,	ArrayList<Integer> undExs, int dim,int dimForest, double prPos, double prNeg ){
		
		
		ArrayList<Triple<ArrayList<Integer>, ArrayList<Integer>, ArrayList<Integer>>> splitting= new ArrayList<Triple<ArrayList<Integer>, ArrayList<Integer>, ArrayList<Integer>>>();
		// step 1: boostrap sampling with undersampling of the uncertainty instances
		for (int i = 0; i < dimForest; i++) {
			Triple<ArrayList<Integer>, ArrayList<Integer>, ArrayList<Integer>> triple= new Triple<ArrayList<Integer>, ArrayList<Integer>, ArrayList<Integer>>();

			ArrayList<Integer> posExsEns= new ArrayList<Integer>();
			ArrayList<Integer> negExsEns= new ArrayList<Integer>();
			ArrayList<Integer> undExsEns= new ArrayList<Integer>();


			BalancedDataset<Integer> bd= new BalancedDataset<Integer>(); // a balance of th)e instances
			bd.balanceTheDataset(posExs, negExs, undExs, posExsEns, negExsEns, undExsEns, Parameters.samplingrate); //no sampling
			
			// performing undersampling on uncertainty instances
			
			// under sampling of the uncertainty instances
	

			triple.setFirstElem(posExsEns);
			triple.setSecondElem(negExsEns);
			triple.setThirdElem(undExsEns);
			
			System.out.println(posExsEns.size()+"--"+negExsEns.size()+"---"+undExsEns.size());
			splitting.add(triple);
		}

		// induction if single terminological decision tree
		Ensemble<DSTDLTree> forest= new Ensemble<DSTDLTree>();
		for (int i = 0; i < dimForest; i++) {
			// examples used to induce a single tree
			ArrayList<Integer> posExsEns= splitting.get(i).getFirstElem();
			ArrayList<Integer> negExsEns= splitting.get(i).getSecondElem();
			ArrayList<Integer> undExsEns= splitting.get(i).getThirdElem();
			System.out.printf(" %d Training set composition: %d %d %d", i, posExsEns.size(),negExsEns.size(), undExsEns.size());
			DSTDLTree tree=data.induceDSTDLTree(posExsEns, negExsEns, undExsEns, dim, prPos, prNeg, null);
			forest.addClassifier(tree);

		}
		System.out.println("forest size: "+ forest.getSize());

		return forest;
	}

	


	@SuppressWarnings({ "rawtypes", "unchecked" })
	public int classifyEnsemble(int indTestEx, Ensemble<DSTDLTree> forest){
		MassFunction[] functions= new MassFunction[forest.getSize()];
		for (int tree=0; tree<forest.getSize(); tree++){
			functions[tree]=data.getBBA(indTestEx, forest.getClassifier(tree));
		}
		
		MassFunction bba= getBBA(functions);
		
		ArrayList<Integer> hypothesis= new ArrayList<Integer>();
		hypothesis.add(+1);
		
		double confirmationFunctionValuePos = bba.getConfirmationFunctionValue(hypothesis);
		//		double confirmationFunctionValuePos = bba.calcolaBeliefFunction(ipotesi);
		// not concept
		ArrayList<Integer> hypothesis2= new ArrayList<Integer>();
		hypothesis2.add(-1);
		double confirmationFunctionValueNeg = bba.getConfirmationFunctionValue(hypothesis2);
		//		double confirmationFunctionValueNeg = bba.calcolaBeliefFunction(ipotesi2);
		ArrayList<Integer> hypothesis3= new ArrayList<Integer>();
		hypothesis3.add(-1);
		hypothesis3.add(+1);
		double confirmationFunctionValueUnc = bba.getConfirmationFunctionValue(hypothesis3);
		//		double confirmationFunctionValueUnc = bba.calcolaBeliefFunction(ipotesi3);

		if((confirmationFunctionValueUnc>confirmationFunctionValuePos)&&(confirmationFunctionValueUnc>confirmationFunctionValueNeg))
			if (confirmationFunctionValuePos>confirmationFunctionValueNeg)
				return +1;
			else if (confirmationFunctionValuePos<confirmationFunctionValueNeg)
				return -1;
			else  return 0;
		else if(confirmationFunctionValuePos>=confirmationFunctionValueNeg)
			return +1;
		else
			return -1;


	}

	@SuppressWarnings("unchecked")
	public	void classifyExamples(int indTestEx, @SuppressWarnings("rawtypes") Ensemble[] forests, int[] results, OWLClassExpression[] testConcepts, int...rclass) {

		for (int c=0; c < testConcepts.length; c++) {
			
			results[c] = classifyEnsemble(indTestEx, forests[c]);
			System.out.println(forests[c].printVotes());
		} // for c



	}


	@SuppressWarnings({ "rawtypes", "unchecked" })
	public MassFunction<Integer> getBBA(MassFunction... function) {
		MassFunction bba= function[0];

		MassFunction<Integer>[] others= new MassFunction[(function.length-1)];
		System.out.println("_____________BBA TO COMBINE______________________");
		System.out.println("BBA: "+bba);
		for(int i=1; i<function.length;i++){
			// applicare la regola di combinazione

			others[i-1]=function[i];
		}
		if(others.length>=1){
			bba=bba.combineEvidences(others);

		}
		//  apply combination rule for BBA


		return bba;

	}


}