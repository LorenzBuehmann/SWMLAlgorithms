package classifiers;

import org.semanticweb.owlapi.model.OWLClassExpression;
/**
 * A generic supervised learning algorithm
 * @author Utente
 *
 */
public interface SupervisedLearnable {

	/**
	 * A method for training a generic supervised learning algorithm
	 * @param trainingExs, the set of training examples
	 * @param testConcepts, the set of query concept
	 * @param negTestConcepts, the complement of query concepts
	 */
	public abstract void training(int[][] results,Integer[] trainingExs,
			OWLClassExpression[] testConcepts, OWLClassExpression[] negTestConcepts);

	
	
	/**
	 * A  method for testing of a supervised learning algorithm
	 * @param f
	 * @param testExs
	 * @param testConcepts
	 * @return the classification results
	 */
	public abstract int[][] test(int f, Integer[] testExs,
			OWLClassExpression[] testConcepts);

	
	public abstract double[] getComplexityValues();
}