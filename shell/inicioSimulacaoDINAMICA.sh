java jade.Boot -container "simulador01:logdyn.agents.Simulador(dinamica_agentes_ult, 50)" 

java jade.Boot -container "simulador01:logdyn.agents.Simulador(dinamica_agentes_dist, 50)" 

java jade.Boot -container "simulador01:logdyn.agents.Simulador(dinamica_agentes_ult, 50)" 

java jade.Boot -container "simulador01:logdyn.agents.Simulador(dinamica_agentes_equil, 50)" 

java jade.Boot -container "simulador01:logdyn.agents.Simulador(dinamica_agentes_ult_aux_2, 50)" 

java jade.Boot -services jade.core.messaging.TopicManagementService:jade.core.replication.AddressNotificationService -container "simulador01:logdyn.agents.Simulador(dinamica_agentes_ult_aux_4, 500)"

java -cp "lib/jade.jar:/home/felipelima/eclipse-workspace/felipetcc/target/classes" jade.Boot -container "simulador01:agents.Simulador(dinamica_agentes_ult, 50)"

java -cp "lib/jade.jar:/home/felipelima/eclipse-workspace/felipetcc/target/classes" jade.Boot -container "simulador01:agents.Simulador(dinamica_agentes_ult_aux_2, 50)" 

java -cp "lib/jade.jar:/home/felipelima/eclipse-workspace/felipetcc/target/classes:/home/felipelima/eclipse-workspace/felipetcc/lib" jade.Boot -container "simulador01:agents.Simulador(dinamica_agentes_ult_aux_2, 50)"


#ta dando certo
java -cp "lib/jade.jar:/home/felipelima/eclipse-workspace/felipetcc/target/classes:lib/postgresql-9.2-1002.jdbc4.jar:lib/commons-pool-1.6-javadoc.jar:lib/commons-math3-3.2.jar" jade.Boot -services jade.core.event.NotificationService:jade.core.messaging.TopicManagementService -container "simulador01:agents.Simulador(dinamica_agentes_ult_aux_4, 100)" 
