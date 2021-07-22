package utils;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Scanner;

import model.CostMatrix;
import model.CustomerAdaptaded;


public class TSPReader {
	private Scanner tspFile;
	private CustomerAdaptaded[] customers;
	private CostMatrix distanceMatrix;
	
	public TSPReader(String fileDir){		
		try {
			tspFile = new Scanner(new FileReader(fileDir));									
		} catch (FileNotFoundException e) {		
			e.printStackTrace();
		}
	}
	
	public CostMatrix readDistances(){
		String sCurrentLine;
		//setting numbers of points		
		for (sCurrentLine = tspFile.nextLine(); tspFile.hasNext() && !sCurrentLine.contains("DIMENSION"); sCurrentLine = tspFile.nextLine());				
		int dimension = new Scanner(sCurrentLine).useDelimiter("[^0-9]+").nextInt();
		customers = new CustomerAdaptaded[dimension];
		for (int i = 1; i <= dimension; customers[i - 1] = new CustomerAdaptaded(i, "C"+i++));
		distanceMatrix = new CostMatrix (customers);
		//setting points
		for (sCurrentLine = tspFile.nextLine(); 
				tspFile.hasNext() && !sCurrentLine.equals("EOF"); 
				sCurrentLine = sCurrentLine.equals("EOF") ? null : tspFile.nextLine()){
			//Getting the distances
			if (sCurrentLine.contains("EDGE_WEIGHT_SECTION"))							
				for (int i = 0; 
						tspFile.hasNext() 
						&& !(sCurrentLine = tspFile.nextLine()).equals("EOF") 
						&& !sCurrentLine.equals("DISPLAY_DATA_SECTION"); 
						i++){
					Scanner distances = new Scanner(sCurrentLine).useDelimiter("[^0-9]+");
					for (int customerIndexSecundary = i + 1; distances.hasNextInt(); 
						distanceMatrix.addCost(customers[i], customers[customerIndexSecundary++], 
								Double.valueOf(distances.nextInt())));											
					/*for (int customerIndexSecundary = i + 1; distances.hasNextInt(); customerIndexSecundary++){
						double distance = distances.nextInt();
						System.out.println("Adding: C"+customers.get(i).getId()+" C"+customers.get(customerIndexSecundary).getId()+" "+distance);
						distanceMatrix.addDistance(customers.get(i), customers.get(customerIndexSecundary++), Double.valueOf(distance));
					}*/
				}						
			//In this case we got the coordenates, so we will insert then and after, if necessary caculate the distances
			if (sCurrentLine.contains("NODE_COORD_SECTION") || sCurrentLine.contains("DISPLAY_DATA_SECTION")){				
				for (int i = 0; tspFile.hasNext() && !(sCurrentLine = tspFile.nextLine()).equals("EOF"); i++){					
					Scanner coordenates = new Scanner(sCurrentLine).useDelimiter("[^\\d.]+|\\.(?!\\d)");									
					//reading coordenates
					customers[Integer.valueOf(coordenates.next()) - 1].setXY(Double.valueOf(coordenates.next()), 
							Double.valueOf(coordenates.next()));					
				}
				if (distanceMatrix.isEmpty()){
					//Calculating distances
					int customersSize = customers.length,
						i1 = customersSize - 1;
					for (int i = 0; i < i1; i++) 
						for (int j = i + 1; j < customersSize; j++) {
							CustomerAdaptaded a = customers[i], 
									b = customers[j];							
							distanceMatrix.addCost(a, b, DistanceCalculator.distance(a, b));							
						}				
				}
			}			
		}		
		return distanceMatrix;
	}

	/**
	 * @return the distanceMatrix
	 */
	public CostMatrix getDistanceMatrix() {
		return distanceMatrix;
	}

	/**
	 * @return the customers
	 */
	public CustomerAdaptaded[] getCustomers() {
		return customers;
	}		
	
	
}
