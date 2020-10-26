package utils;

import java.sql.SQLException;



public class RouteFactory {

	public static Route getRoute(NetworkRandom net, String tipoRota, int fxV, int fxH, double[] pontosX, double[] pontosY) throws SQLException {  
        if( tipoRota == null ) return null;  
        // Roteiro estático, sem tranferencia de tarefas e sem veículos auxiliares
        else if( tipoRota.equals("estatica") ) return new RouteRandomEstatico(net, fxV, fxH, pontosX, pontosY);  
        // Roteiro dinâmico, com tranferencia de tarefas dos veículos regulares para um único veículo auxiliar
        else if( tipoRota.equals("dinamica_centroMassa") ) return new RouteRandomCentroMassa(net, fxV, fxH, pontosX, pontosY);  
        // Roteiro dinâmico, com uso de agentes utilizando o critério de distancia mais proxima para tranferir as tarefas dos veículos regulares para outros regulares, sem uso de veiculos auxiliares
        else if( tipoRota.equals("dinamica_agentes_dist") ) return new RouteRandomAgentsOperadoresGoel(net, fxV, fxH, pontosX, pontosY);  
        // Roteiro dinâmico, com uso de agentes utilizando o critério de equilibrio de carga para tranferir as tarefas dos veículos regulares para outros regulares, sem uso de veiculos auxiliares
        else if( tipoRota.equals("dinamica_agentes_equil") ) return new RouteRandomAgentsOperadoresGoelEquilibrio(net, fxV, fxH, pontosX, pontosY);  
        // Roteiro dinâmico, com uso de agentes utilizando o critério de equilibrio de carga das tarefas últimas (restante) para tranferir as tarefas dos veículos regulares para outros regulares, sem uso de veiculos auxiliares
        else if( tipoRota.equals("dinamica_agentes_ult") ) return new RouteRandomAgentsOperadoresGoelUltimos(net, fxV, fxH, pontosX, pontosY);  
        // Roteiro dinâmico, com uso de agentes utilizando o critério de distancia mais proxima para tranferir as tarefas dos veículos regulares para outros regulares, com 1 veiculo auxiliar
        else if( tipoRota.equals("dinamica_agentes_ult_aux_1") ) return new RouteRandomAgentsOperadoresGoelUltimos(net, fxV, fxH, pontosX, pontosY);  
        // Roteiro dinâmico, com uso de agentes utilizando o critério de distancia mais proxima para tranferir as tarefas dos veículos regulares para outros regulares, com 2 veiculos auxiliares
        else if( tipoRota.equals("dinamica_agentes_ult_aux_2") ) return new RouteRandomAgentsOperadoresGoelUltimos(net, fxV, fxH, pontosX, pontosY);  
        // Roteiro dinâmico, com uso de agentes utilizando o critério de distancia mais proxima para tranferir as tarefas dos veículos regulares para outros regulares, com 3 veiculos auxiliares
        else if( tipoRota.equals("dinamica_agentes_ult_aux_3") ) return new RouteRandomAgentsOperadoresGoelUltimos(net, fxV, fxH, pontosX, pontosY);  
        // Roteiro dinâmico, com uso de agentes utilizando o critério de distancia mais proxima para tranferir as tarefas dos veículos regulares para outros regulares, com 4 veiculos auxiliares
        else if( tipoRota.equals("dinamica_agentes_ult_aux_4") ) return new RouteRandomAgentsOperadoresGoelUltimos(net, fxV, fxH, pontosX, pontosY);  
        // Roteiro dinâmico, com uso de agentes utilizando o critério de distancia mais proxima para tranferir as tarefas dos veículos regulares para outros regulares, com 5 veiculos auxiliares
        else if( tipoRota.equals("dinamica_agentes_ult_aux_5") ) return new RouteRandomAgentsOperadoresGoelUltimos(net, fxV, fxH, pontosX, pontosY);  
        // Roteiro dinâmico, com uso de agentes utilizando o critério de distancia mais proxima para tranferir as tarefas dos veículos regulares para outros regulares, com 6 veiculos auxiliares
        else if( tipoRota.equals("dinamica_agentes_ult_aux_6") ) return new RouteRandomAgentsOperadoresGoelUltimos(net, fxV, fxH, pontosX, pontosY);  
        // Roteiro dinâmico, com uso de agentes utilizando o critério de distancia mais proxima para tranferir as tarefas dos veículos regulares para outros regulares, com 7 veiculos auxiliares
        else if( tipoRota.equals("dinamica_agentes_ult_aux_7") ) return new RouteRandomAgentsOperadoresGoelUltimos(net, fxV, fxH, pontosX, pontosY);  
        // Roteiro dinâmico, com uso de agentes utilizando o critério de equilibrar as rotas dos veículos, com 1 veiculos auxiliares
        else if( tipoRota.equals("dinamica_agentes_equil_aux_1") ) return new RouteRandomAgentsOperadoresGoelEquilibrio(net, fxV, fxH, pontosX, pontosY);  
        // Roteiro dinâmico, com uso de agentes utilizando o critério de equilibrar as rotas dos veículos, com 2 veiculos auxiliares
        else if( tipoRota.equals("dinamica_agentes_equil_aux_2") ) return new RouteRandomAgentsOperadoresGoelEquilibrio(net, fxV, fxH, pontosX, pontosY);  
        // Roteiro dinâmico, com uso de agentes utilizando o critério de equilibrar as rotas dos veículos, com 3 veiculos auxiliares
        else if( tipoRota.equals("dinamica_agentes_equil_aux_3") ) return new RouteRandomAgentsOperadoresGoelEquilibrio(net, fxV, fxH, pontosX, pontosY);  
        // Roteiro dinâmico, com uso de agentes utilizando o critério de equilibrar as rotas dos veículos, com 4 veiculos auxiliares
        else if( tipoRota.equals("dinamica_agentes_equil_aux_4") ) return new RouteRandomAgentsOperadoresGoelEquilibrio(net, fxV, fxH, pontosX, pontosY);  
        // Roteiro dinâmico, com uso de agentes utilizando o critério de equilibrar as rotas dos veículos, com 5 veiculos auxiliares
        else if( tipoRota.equals("dinamica_agentes_equil_aux_5") ) return new RouteRandomAgentsOperadoresGoelEquilibrio(net, fxV, fxH, pontosX, pontosY);  
        // Roteiro dinâmico, com uso de agentes utilizando o critério de equilibrar as rotas dos veículos, com 6 veiculos auxiliares
        else if( tipoRota.equals("dinamica_agentes_equil_aux_6") ) return new RouteRandomAgentsOperadoresGoelEquilibrio(net, fxV, fxH, pontosX, pontosY);  
        // Roteiro dinâmico, com uso de agentes utilizando o critério de equilibrar as rotas dos veículos, com 7 veiculos auxiliares
        else if( tipoRota.equals("dinamica_agentes_equil_aux_7") ) return new RouteRandomAgentsOperadoresGoelEquilibrio(net, fxV, fxH, pontosX, pontosY);  
        else return null;  
    } 
	
	public static boolean isValid( String tipoRota){
        if( tipoRota.equals("estatica") || 
        	tipoRota.equals("dinamica_centroMassa") ||
        	tipoRota.equals("dinamica_agentes_dist")  ||
        	tipoRota.equals("dinamica_agentes_equil")||
        	tipoRota.equals("dinamica_agentes_ult")||
        	tipoRota.equals("dinamica_agentes_ult_aux_1")||
        	tipoRota.equals("dinamica_agentes_ult_aux_2")||
        	tipoRota.equals("dinamica_agentes_ult_aux_3")||
        	tipoRota.equals("dinamica_agentes_ult_aux_4")||
        	tipoRota.equals("dinamica_agentes_ult_aux_5")||
        	tipoRota.equals("dinamica_agentes_ult_aux_6")||
        	tipoRota.equals("dinamica_agentes_ult_aux_7")||
        	tipoRota.equals("dinamica_agentes_equil_aux_1")||
        	tipoRota.equals("dinamica_agentes_equil_aux_2")||
        	tipoRota.equals("dinamica_agentes_equil_aux_3")||
        	tipoRota.equals("dinamica_agentes_equil_aux_4")||
        	tipoRota.equals("dinamica_agentes_equil_aux_5")||
        	tipoRota.equals("dinamica_agentes_equil_aux_6")||
        	tipoRota.equals("dinamica_agentes_equil_aux_7"))

        		return true;  
        else 
        		return false;  
	}
}
