package knowledgeBasesHandler;

import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLConstant;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLOntology;

public interface IKnowledgeBase {

	public abstract OWLOntology initKB();
//
//	/**
//	 * Restituisce i risultati della classificazione effettuata dal reasoner
//	 * @return matrice testConcepts.length x esempi.length
//	 */
//	public abstract int[][] getClassMembershipResult(
//			OWLDescription[] testConcepts, OWLIndividual[] esempi);

	/**
	 * Returns the classification results
	 * @param ruoli
	 * @param esempi
	 * @return
	 */
	public abstract int[][][] getRoleMembershipResult(
			OWLObjectProperty[] ruoli, OWLIndividual[] esempi);

	//********************METODI DI ACCESSO  ALLE COMPONENTI DELL'ONTOLOGIA*******************************//
	/**
	 * Returns object properties
	 * @return
	 */
	public abstract OWLObjectProperty[] getRoles();

	/**
	 * Returns primitve concepts
	 * @return
	 */
	public abstract OWLClass[] getClasses();

	/**
	 * Returns the individuals of an ontology
	 * @return 
	 */
	public abstract OWLIndividual[] getIndividuals();

	public abstract OWLDataProperty[] getDataProperties();

	public abstract OWLIndividual[][] getDomains();

	public abstract OWLConstant[][] getDataPropertiesValue();

	public abstract String getURL();

	/**
	 * Random choice of data type properties
	 * @param numQueryProperty, 
	 * @return
	 */
	public abstract int[] getRandomProperty(int numQueryProperty);

	public abstract int[] getRandomRoles(int numRegole);



	int[][] getClassMembershipResult(OWLDescription[] testConcepts,
			OWLDescription[] negTestConcepts, OWLIndividual[] esempi);

}