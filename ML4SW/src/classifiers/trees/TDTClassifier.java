package classifiers.trees;

import java.util.ArrayList;

import java.util.Stack;

import knowledgeBasesHandler.KnowledgeBase;

import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLDescription;

import utils.Couple;
import utils.Npla;


import classifiers.trees.models.AbstractTree;
import classifiers.trees.models.DLTree;
import evaluation.Evaluation;

public class TDTClassifier extends AbstractTDTClassifier {


	public TDTClassifier(KnowledgeBase k){

		super(k);

	}



	public DLTree induceDLTree(ArrayList<Integer> posExs, ArrayList<Integer> negExs,	ArrayList<Integer> undExs, 
			int dim, double prPos, double prNeg) {		
		System.out.printf("Learning problem\t p:%d\t n:%d\t u:%d\t prPos:%4f\t prNeg:%4f\n", 
				posExs.size(), negExs.size(), undExs.size(), prPos, prNeg);


		Npla<ArrayList<Integer>,ArrayList<Integer>,ArrayList<Integer>, Integer, Double, Double> examples = new Npla<ArrayList<Integer>,ArrayList<Integer>,ArrayList<Integer>, Integer, Double, Double>(posExs, negExs, undExs, dim, prPos, prNeg);
		DLTree tree = new DLTree(); // new (sub)tree
		Stack<Couple<DLTree,Npla<ArrayList<Integer>,ArrayList<Integer>,ArrayList<Integer>, Integer, Double, Double>>> stack= new Stack<Couple<DLTree,Npla<ArrayList<Integer>,ArrayList<Integer>,ArrayList<Integer>, Integer, Double, Double>>>();
		Couple<DLTree,Npla<ArrayList<Integer>,ArrayList<Integer>,ArrayList<Integer>, Integer, Double, Double>> toInduce= new Couple<DLTree,Npla<ArrayList<Integer>,ArrayList<Integer>,ArrayList<Integer>, Integer, Double, Double>>();
		toInduce.setFirstElement(tree);
		toInduce.setSecondElement(examples);
		stack.push(toInduce);

		while(!stack.isEmpty()){
			System.out.printf("Stack: %d \n",stack.size());
			Couple<DLTree,Npla<ArrayList<Integer>,ArrayList<Integer>,ArrayList<Integer>, Integer, Double, Double>> current= stack.pop(); // extract the next element
			DLTree currentTree= current.getFirstElement();
			Npla<ArrayList<Integer>,ArrayList<Integer>,ArrayList<Integer>, Integer, Double, Double> currentExamples= current.getSecondElement();
			// set of negative, positive and undefined example
			posExs=currentExamples.getFirst();
			negExs=currentExamples.getSecond();
			undExs=currentExamples.getThird();
			if (posExs.size() == 0 && negExs.size() == 0) // no exs
				if (prPos >= prNeg) { // prior majority of positives
					currentTree.setRoot(kb.getDataFactory().getOWLThing()); // set positive leaf
				}
				else { // prior majority of negatives
					currentTree.setRoot(kb.getDataFactory().getOWLNothing()); // set negative leaf
				}

			//		double numPos = posExs.size() + undExs.size()*prPos;
			//		double numNeg = negExs.size() + undExs.size()*prNeg;
			else{
				double numPos = posExs.size();
				double numNeg = negExs.size();
				double perPos = numPos/(numPos+numNeg);
				double perNeg = numNeg/(numPos+numNeg);

				if (perNeg==0 && perPos > Evaluation.PURITY_THRESHOLD) { // no negative
					currentTree.setRoot(kb.getDataFactory().getOWLThing()); // set positive leaf
					
				}
				else{
					if (perPos==0 && perNeg > Evaluation.PURITY_THRESHOLD) { // no positive			
						currentTree.setRoot(kb.getDataFactory().getOWLNothing()); // set negative leaf
						
					}		
					// else (a non-leaf node) ...
					else{
						OWLDescription[] cConcepts= new OWLDescription[0];
						ArrayList<OWLDescription> cConceptsL = generateNewConcepts(dim, posExs, negExs);
						//						cConceptsL= getRandomSelection(cConceptsL); // random selection of feature set

						cConcepts = cConceptsL.toArray(cConcepts);

						// select node concept
						OWLDescription newRootConcept = selectBestConcept(cConcepts, posExs, negExs, undExs, prPos, prNeg);

						ArrayList<Integer> posExsT = new ArrayList<Integer>();
						ArrayList<Integer> negExsT = new ArrayList<Integer>();
						ArrayList<Integer> undExsT = new ArrayList<Integer>();
						ArrayList<Integer> posExsF = new ArrayList<Integer>();
						ArrayList<Integer> negExsF = new ArrayList<Integer>();
						ArrayList<Integer> undExsF = new ArrayList<Integer>();

						split(newRootConcept, posExs, negExs, undExs, posExsT, negExsT, undExsT, posExsF, negExsF, undExsF);
						// select node concept
						currentTree.setRoot(newRootConcept);		
						// build subtrees

						//		undExsT = union(undExsT,);
						DLTree posTree= new DLTree();
						DLTree negTree= new DLTree(); // recursive calls simulation
						currentTree.setPosTree(posTree);
						currentTree.setNegTree(negTree);
						Npla<ArrayList<Integer>, ArrayList<Integer>, ArrayList<Integer>, Integer, Double, Double> npla1 = new Npla<ArrayList<Integer>,ArrayList<Integer>,ArrayList<Integer>, Integer, Double, Double>(posExsT, negExsT, undExsT, dim, perPos, perNeg);
						Npla<ArrayList<Integer>, ArrayList<Integer>, ArrayList<Integer>, Integer, Double, Double> npla2 = new Npla<ArrayList<Integer>,ArrayList<Integer>,ArrayList<Integer>, Integer, Double, Double>(posExsF, negExsF, undExsF, dim, perPos, perNeg);
						Couple<DLTree,Npla<ArrayList<Integer>,ArrayList<Integer>,ArrayList<Integer>, Integer, Double, Double>> pos= new Couple<DLTree,Npla<ArrayList<Integer>,ArrayList<Integer>,ArrayList<Integer>, Integer, Double, Double>>();
						pos.setFirstElement(posTree);
						pos.setSecondElement(npla1);
						// negative branch
						Couple<DLTree,Npla<ArrayList<Integer>,ArrayList<Integer>,ArrayList<Integer>, Integer, Double, Double>> neg= new Couple<DLTree,Npla<ArrayList<Integer>,ArrayList<Integer>,ArrayList<Integer>, Integer, Double, Double>>();
						neg.setFirstElement(negTree);
						neg.setSecondElement(npla2);
						stack.push(neg);
						stack.push(pos);
					}
				}
			}
		}
		return tree;

	}


	@Override
	public void prune(Integer[] pruningSet, AbstractTree tree,
			AbstractTree subtree, OWLDescription testConcept) {



		DLTree treeDL= (DLTree) tree;

		Stack<DLTree> stack= new Stack<DLTree>();
		stack.add(treeDL);
		// array list come pila
		double nodes= treeDL.getNodi();
		if(nodes>1){
			while(!stack.isEmpty()){
				DLTree current= stack.pop(); // leggo l'albero corrente

				DLTree pos= current.getPosSubTree();
				DLTree neg= current.getNegSubTree();
				System.out.println("Current: "+pos+" ----- "+neg+"visited? "+current.isVisited());

				if(current.isVisited()){
					System.out.println("Valutazione");
					int comissionRoot=current.getCommission();
					int comissionPosTree= pos.getCommission();
					int comissionNegTree= neg.getCommission();


					int gainC=comissionRoot-(comissionPosTree+comissionNegTree);

					if(gainC<0){

						int posExs=current.getPos();
						int negExs= current.getNeg();
						// rimpiazzo rispetto alla classe di maggioranza
						if(posExs<=negExs){

							current.setRoot(kb.getDataFactory()	.getOWLNothing());
						}
						else{

							current.setRoot(kb.getDataFactory()	.getOWLThing());
						}

						current.setNegTree(null);
						current.setPosTree(null);	



					}
				}
				else{
					current.setAsVisited();
					stack.push(current); // rimetto in  pila  e procedo alle chiamate ricorsive
					if(pos!=null){
						if((pos.getNegSubTree()!=null)||(pos.getPosSubTree()!=null))
							stack.push(pos);

					}
					if(neg!=null){
						if((neg.getNegSubTree()!=null)||(neg.getPosSubTree()!=null))
							stack.push(neg);

					}
				}

			}				
		}

	}

	/**
	 * Implementation of a REP-pruning algorithm for TDT
	 * @param pruningset
	 * @param tree
	 * @param testconcept
	 * @return
	 */
	public int[] doREPPruning(Integer[] pruningset, DLTree tree, OWLDescription testconcept){
		// step 1: classification
		System.out.println("Number of Nodes  Before pruning"+ tree.getNodi());
		int[] results= new int[pruningset.length];
		//for each element of the pruning set
		for (int element=0; element< pruningset.length; element++){
			//  per ogni elemento del pruning set
			// versione modificata per supportare il pruning
			classifyExampleforPruning(pruningset[element], tree,testconcept); // classificazione top down

		}

		prune(pruningset, tree, tree, testconcept);
		System.out.println("Number of Nodes  After pruning"+ tree.getNodi());

		return results;
	}

	/**
	 * Ad-hoc implementation for evaluation step in REP-pruning. the method count positive, negative and uncertain instances 
	 * @param indTestEx
	 * @param tree
	 * @param testconcept
	 * @return
	 */
	public int classifyExampleforPruning(int indTestEx, DLTree tree,OWLDescription testconcept) {
		Stack<DLTree> stack= new Stack<DLTree>();
		OWLDataFactory dataFactory = kb.getDataFactory();
		stack.add(tree);
		int result=0;
		boolean stop=false;
		while(!stack.isEmpty() && !stop){
			DLTree currentTree= stack.pop();

			OWLDescription rootClass = currentTree.getRoot();
			//			System.out.println("Root class: "+ rootClass);
			if (rootClass.equals(dataFactory.getOWLThing())){
				if(kb.getReasoner().hasType(kb.getIndividuals()[indTestEx], testconcept)){
					currentTree.setMatch(0);
					currentTree.setPos();
				}
				else if (kb.getReasoner().hasType(kb.getIndividuals()[indTestEx], dataFactory.getOWLObjectComplementOf(testconcept))){
					currentTree.setCommission(0);
					currentTree.setNeg(0);
				}else{
					currentTree.setInduction(0);
					currentTree.setUnd();
				}
				stop=true;
				result=+1;

			}
			else if (rootClass.equals(dataFactory.getOWLNothing())){

				if(kb.getReasoner().hasType(kb.getIndividuals()[indTestEx], testconcept)){
					
					currentTree.setPos();
					currentTree.setCommission(0);
				}
				else if (kb.getReasoner().hasType(kb.getIndividuals()[indTestEx], dataFactory.getOWLObjectComplementOf(testconcept))){
					currentTree.setNeg(0);
					currentTree.setMatch(0);
				}
				else{
					currentTree.setUnd();
					currentTree.setInduction(0);
				}
				stop=true;
				result=-1;

			}else if (kb.getReasoner().hasType(kb.getIndividuals()[indTestEx], rootClass)){
				if(kb.getReasoner().hasType(kb.getIndividuals()[indTestEx], testconcept)){
					currentTree.setMatch(0);
					currentTree.setPos();
				}else if (kb.getReasoner().hasType(kb.getIndividuals()[indTestEx], dataFactory.getOWLObjectComplementOf(testconcept))){
					currentTree.setCommission(0);
					currentTree.setNeg(0);
				}else{
					currentTree.setUnd();
					currentTree.setInduction(0);
				}
				stack.push(currentTree.getPosSubTree());

			}
			else if (kb.getReasoner().hasType(kb.getIndividuals()[indTestEx], dataFactory.getOWLObjectComplementOf(rootClass))){

				if(kb.getReasoner().hasType(kb.getIndividuals()[indTestEx], testconcept)){
					currentTree.setPos();
					currentTree.setCommission(0);
				}else if (kb.getReasoner().hasType(kb.getIndividuals()[indTestEx], dataFactory.getOWLObjectComplementOf(testconcept))){
					currentTree.setNeg(0);
					currentTree.setMatch(0);
				}else{
					currentTree.setUnd();
					currentTree.setInduction(0);
				}
				stack.push(currentTree.getNegSubTree());

			}
			else {
				if(kb.getReasoner().hasType(kb.getIndividuals()[indTestEx], testconcept)){
					currentTree.setPos();
					currentTree.setInduction(0);
				}else if (kb.getReasoner().hasType(kb.getIndividuals()[indTestEx], dataFactory.getOWLObjectComplementOf(testconcept))){
					currentTree.setNeg(0);
					currentTree.setInduction(0);
				}else{
					currentTree.setUnd();
					currentTree.setMatch(0);
				}
				stop=true;
				result=0; 

			}
		};


		return result;

	}

}


