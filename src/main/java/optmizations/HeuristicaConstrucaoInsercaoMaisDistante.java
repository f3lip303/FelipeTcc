package optmizations;

import java.util.Arrays;

import utils.Utilidades;

public class HeuristicaConstrucaoInsercaoMaisDistante {
	/**
     * Efetua a contrução da rota. Utiliza o método da inserção do mais distante
     * como a Heurística construtiva.s
     * @param matrizDistancia
     * @param numeroClientes
     * @return
     */
	public Integer[] solve(double[][] matrizDistancia){
		final int numeroVisitas = matrizDistancia.length;   // incluindo o deposito de volta
		Integer[] rota  = new Integer[numeroVisitas+1];
        double[] distancias = new double[numeroVisitas];
        double distanciaMaxima, custo, menorCusto;
        int tamCiclo,indice,farthest,melhorPosicao;
        
        // rota inicia-se no depósito
        rota[0] = 0;
        rota = Utilidades.inicializaArray(rota,0);
        tamCiclo = 1;
        // até o fim da roteirização de todas as visitas
        while (tamCiclo<numeroVisitas){
        	
        	// verifica a maior distancia de todos os pontos a alocar aos pontos já alocados em rota
        	indice=0;
        	Arrays.fill(distancias, -1);
        	while ( indice < tamCiclo){   // para cada ponto alocado
        		for (int i=0 ; i<numeroVisitas ; i++){    // para todas as visitas 
        			if (matrizDistancia[rota[indice]][i] > distancias[i]) { distancias[i] = matrizDistancia[rota[indice]][i]; }
        		}
        		indice++;
        	}
        	
        	// verifica a distancia de todos os pontos a alocar às retas já existentes ???
        	
        	// identifica o nó mais distante dos outros já adicionados
        	farthest = 0;  // indice do ponto mais distante
        	distanciaMaxima = 0; // valor do ponto mais distante  
        	for (int i=0 ; i<numeroVisitas ; i++){    // para todas as visitas 
        		if (Utilidades.inArray(rota,i)<0){ // se o ponto não está na rota
        			if (distancias[i]>distanciaMaxima){
        				distanciaMaxima = distancias[i];
        				farthest = i;
        			}
        		}
        	}
        	
        	
        	// escolher o local (melhorPosicao) para inserir o nó mais distante (farthest) numa posição que alcançe (menorCusto)
        	indice = 1;   // começar tentativas na posição 1, pois a 0 é fixa no armazem, até o final do array
        	melhorPosicao = -1;
        	menorCusto = 999999;        	
        	Integer[] rotaAux = new Integer[numeroVisitas];
        	rotaAux = Utilidades.inicializaArray(rotaAux,0);
        	for (int i=1;i<=tamCiclo;i++){   // laço que verifica todas as posições para inserir farthest
        		custo = 0;
        		Arrays.fill(rotaAux, 0);
        		
            	System.arraycopy(rota,0,rotaAux,0,i);  	// elementos antes de farthest permanecem, então copiar apenas elementos após farthest
            	rotaAux[i] = farthest;   															// inclusão de farthest
            	System.arraycopy(rota,i,rotaAux,i+1,numeroVisitas-i-1);	// colando os elementos após farthest        	
            	for (int u=0; u<(rotaAux.length-1); u++){
            		custo += matrizDistancia[rotaAux[u]][rotaAux[u+1]];
            	}
        		
        		if (custo<menorCusto){
        			menorCusto = custo;
        			melhorPosicao = indice;
        		}
        		indice++;
        	}
        	
        	// atualizar a rota com a entrada de (farthest) na posição que reduza o custo (melhorPosicao) 
        	tamCiclo++;
        	Arrays.fill(rotaAux, 0);
        	System.arraycopy(rota,melhorPosicao,rotaAux,melhorPosicao,numeroVisitas-melhorPosicao);  	// elementos antes de farthest permanecem, então copiar apenas elementos após farthest
        	rota[melhorPosicao] = farthest;   															// inclusão de farthest
        	System.arraycopy(rotaAux,melhorPosicao,rota,melhorPosicao+1,numeroVisitas-melhorPosicao-1);	// colando os elementos após farthest        	
        }
        rota[tamCiclo] = 0;
        return rota;
	}
	
}
