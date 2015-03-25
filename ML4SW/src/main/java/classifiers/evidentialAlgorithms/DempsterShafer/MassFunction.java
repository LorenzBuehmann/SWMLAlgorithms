package classifiers.evidentialAlgorithms.DempsterShafer;


import java.util.List;
import utils.Combination;
import utils.SetUtils;

/**
 * A class for representing a BBA
 * @author Giuseppe
 *
 * @param <S>
 * @param <T>
 */
public class MassFunction <T extends Comparable<? super T>> {
	private  List<T> frameOfDiscernement;//frame of Discernement
	private  List<List<T>> powerSet;
	private double[] values;// contiene i valori   assunti dalla funzione considerando un certo 
	// esempio, un individuo da classificare ed un frame of Discernement
	
	
//	public static void setFrameOfDiscernement(){
//		
//	}
//	
	/**
	 * Constructor
	 * @param set
	 */
	public MassFunction(List<T> set){
		frameOfDiscernement=set;
		generatePowerSet();
		values= new double[powerSet.size()];
		
	}
	/**
	 * Genera l'insieme potenza di un certo insieme
	 * @return
	 */
	public void  generatePowerSet(){

		powerSet=Combination.findCombinations(frameOfDiscernement);
	}
	
	
	/**
	 * Restituisce l'insieme potenza ottenuto a partire dal frame of discernement
	 * @return insieme potenza
	 */
	@SuppressWarnings({ "unchecked", "rawtypes", "unused" })
	public  List<T>[] getSubsetsOfFrame(){
		List[] result= new List[powerSet.size()];
		int i=0;
		for(List<T> elem:powerSet){
			
			result[i]=powerSet.get(i);
			i++;
		}
		return result;
	}
	
	public List<T> getFrame(){
		return frameOfDiscernement;
		
	}
	/**
	 * Set a specific value for this BBA
	 */
	public void setValues(List<T> label,double value){
		int pos= SetUtils.find(label,powerSet);
		values[pos]=value;
		
		
	}
	
	
	/**
	 * Returns the value of a BBA for a specific element of class 
	 * @param class
	 * @return the value of a bba or NaN 
	 */
	public double getValue(List<T> label){
		//System.out.println(valori.get(categoria));
		int pos= SetUtils.find(label, powerSet);
		return values[pos];
	
	}
	
	
	public double getNonSpecificityMeasureValue(){
		double result=0;
		for(List<T> label: powerSet){
			if(!label.isEmpty())
				result+=(values[SetUtils.find(label, powerSet)]*Math.log(label.size())); 
		}
//		System.out.println("Non-sp: "+result);
		return result;
	}
	
	
	public double getRandomnessMeasure(){
		double result=0.0;
		for (List<T> c: powerSet){
			double pignisticValue=getPignisticTransformation(c);
			int posCategoria = SetUtils.find(c, powerSet);
			 result+= -1* (values[posCategoria]*Math.log(pignisticValue));
		
		}
		return result;
		
		
			
	}
	
	public double getPignisticTransformation(List<T> cl){
		// it works certainly for {-1,+1} as a frame of discernement
		 double result=0.0;
		for(T element: cl){
			double pignisticValueForElement=0; // initialization
			for(List<T> categoria: powerSet){

				if(!categoria.isEmpty()){
					if (categoria.contains(element)){
						int posCategoria = SetUtils.find(categoria, powerSet);
						pignisticValueForElement += values[posCategoria]/categoria.size();
					}
				}

			}
			result+=pignisticValueForElement;
			
		}
		return result;
	}
	
	
	public double getGlobalUncertaintyMeasure(){
		
		double nonSpecificity= this.getNonSpecificityMeasureValue();
		double randomness= this.getRandomnessMeasure();
		final double LAMBDA= 0.1;
		double result= ((1-LAMBDA)*nonSpecificity)+(LAMBDA*randomness);
		return result;
		
	}
	
	/**
	 * The method computes a confusion measure described in Smarandache et.al as discordant measure
	 * @return
	 */
	public double getConfusionMeasure(){
		double result=0;
		for(List<T> labels: powerSet){
			if(!labels.isEmpty())
				result-=(values[SetUtils.find(labels, powerSet)]*Math.log(this.computeBeliefFunction(labels))); 
		}
//		System.out.println("Non-sp: "+result);
		return result;
	}
	
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	/**
	 * combine two BBAs according to the Dempster rule
	 * @param function
	 * @return 
	 */
	public MassFunction combineEvidences(MassFunction function){
		MassFunction result= new MassFunction(frameOfDiscernement);
		double conflitto=getConflict(function);
		// per l'iesima ipotesi dell'insieme potenza
		for(List<T> elem:powerSet){
			int pos=SetUtils.find(elem, powerSet);
			// trovo gli insiemi intersecanti ipotesi1 e ipotesi2
			for(List<T>ipotesi1: powerSet){
				for(List<T>ipotesi2:powerSet){
					List<T> ipotesi12=SetUtils.intersection(ipotesi1, ipotesi2);
						// se l'intersezione è quella che mi aspetto e non è vuota
						if(!(ipotesi12.isEmpty())&&(SetUtils.areEquals(ipotesi12, elem))){
							SetUtils.find(ipotesi1, powerSet);
							SetUtils.find(ipotesi2, powerSet);
							double prodottoMasse=getValue(ipotesi1)*function.getValue(ipotesi2)/conflitto;	
							result.values[pos]+=prodottoMasse;
							
						}
//						System.out.println("Valori"+pos+"----"+result.valori[pos]);
					}
					
				}
				
				
			}
			
		return result;
			
		}
		
		
		
	
	
	
	
	/**
	 * Dempster rule for more BBAs
	 * @param function
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public MassFunction combineEvidences(MassFunction... function){
		if(function.length==0)
			throw new RuntimeException("At least a mass function is required");
		MassFunction result=this.combineEvidences(function[0]);
		// l'operazione sfrutta l'associatività della regola di Dempster
		for(int i=1;i<function.length;i++){
			// applico una regola non normalizzata fino alla n-1esima funzione
			
			
			result= result.combineEvidences(function[i]);
			
		}
		// faccio la normalizzazionesulla base del conflitto tra la combinata delle prime n-1 e l'ultima
		
	
		
		return result;
		
	}
	
	/**
	 * Implementation of Dubois-Prade combination rule 
	 * @param function
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public MassFunction<T> combineEvidencesDuboisPrade (MassFunction function){
		
		MassFunction<T> result= new MassFunction(frameOfDiscernement);
		
		// per l'iesima ipotesi dell'insieme potenza
		for(List<T> elem:powerSet){
			int pos=SetUtils.find(elem, powerSet);
			// trovo gli insiemi intersecanti ipotesi1 e ipotesi2
			for(List<T>ipotesi1: powerSet){
				for(List<T>ipotesi2:powerSet){
					List<T> ipotesi12=SetUtils.union(ipotesi1, ipotesi2);
						// se l'unione è quella che mi aspetto e non è vuota!ipotesi12.isEmpty()&&
						if((SetUtils.areEquals(ipotesi12, elem))){
							SetUtils.find(ipotesi1, powerSet);
							SetUtils.find(ipotesi2, powerSet);
							double prodottoMasse=getValue(ipotesi1)*function.getValue(ipotesi2);	
							result.values[pos]+=prodottoMasse;
							
						}
						
					}
					
				}
//				result.valori[pos]=result.valori[pos];
				
			}
			
		return result;
		
	}
	
	@SuppressWarnings("rawtypes")
	public MassFunction combineEvidencesDuboisPrade(MassFunction... function){
		if(function.length==0)
			throw new RuntimeException("Occorre almeno passare una funzione di massa");
		MassFunction result=this.combineEvidencesDuboisPrade(function[0]);
		// l'operazione sfrutta l'associatività della regola di Dempster
		for(int i=1;i<function.length;i++)
			result= result.combineEvidencesDuboisPrade(function[i]);
			
		
		
		
	
		
		return result;
		
	}
	
	
	
	/**
	 * Compute the conflict between two hypotheses
	 * @param function
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public double getConflict(MassFunction function){
		double massaVuota=0;
		for(List<T> ipotesi1:powerSet){
//			System.out.println("***************");
//			System.out.println("Ipotesi 1:"+ipotesi1);
			for(List<T> ipotesi2:powerSet){
//				System.out.println("Ipotesi 2:"+ipotesi2);
				List<T>intersezione=SetUtils.intersection(ipotesi1,ipotesi2);
				if(!intersezione.isEmpty()){
//					System.out.println("Intersezione vuota");
					massaVuota+= (getValue(ipotesi1)*function.getValue(ipotesi2));
//					System.out.println(massaVuota);
				}
				
				
			}
			
			
		}
	
		return (massaVuota);
	}
	/**
	 * Compute the belief function value
	 * @param hypothesis
	 * @return
	 */
	public double computeBeliefFunction(List<T> hypothesis){
		double bel_hypothesis=0;
		for(List<T> elem:powerSet){
			// per ogni sottoinsieme non vuotodi ipotesi
			if(!elem.isEmpty()&& hypothesis.containsAll(elem)){
				// somma le masse
//				System.out.println("m("+elem+")="+bel_ipotesi);
				bel_hypothesis+=getValue(elem);
				
			}
		}
//			System.out.println("Belief:"+bel_ipotesi);
		
		return bel_hypothesis;
	}
	/**
	 * Compute the plausibility function value
	 * @param ipotesi
	 * @return
	 */
	public double calcolaPlausibilityFunction(List<T> ipotesi){
		// applicando la definizione abbiamo
		double pl_ipotesi=0;
		for(List<T> elem:powerSet){
			
			if(!(SetUtils.intersection(ipotesi,elem)).isEmpty())
				// somma le masse
				pl_ipotesi+=getValue(elem);
//			System.out.println(pl_ipotesi);
		}
			
//		System.out.println("Plausibility"+pl_ipotesi);
		return pl_ipotesi;
		
		
	}
	/**
	 * calcola il valore della confirmation function
	 * @param ipotesi
	 * @return
	 */
	public double getConfirmationFunctionValue(List<T>ipotesi){
		return (computeBeliefFunction(ipotesi)+calcolaPlausibilityFunction(ipotesi)-1);
		
	}
	
	public String toString(){
		String res="";
		for(int i=0;i<powerSet.size();i++){
			String string = ""+powerSet.get(i)+values[i];
			res+= string;
		}
		return res;
	}

	
	
	
	
	
}

