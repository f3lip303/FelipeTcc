#java -cp lib/jade.jar:bin:lib/mail-1.4.7.jar:lib/commons-pool-1.6.jar:lib/commons-dbcp-1.4.jar:lib/slf4j-api-1.7.5.jar:lib/commons-math3-3.2.jar:lib/postgresql-9.2-1002.jdbc4.jar jade.Boot -services jade.core.messaging.TopicManagementService:jade.core.replication.AddressNotificationService -container "simulador01:logdyn.agents.Simulador(estatica, 50)"

java jade.Boot -container "SyncAgent:java.agents.SyncAgent" -services jade.core.messaging.TopicManagementService:jade.core.replication.AddressNotificationService 

java jade.Boot -services jade.core.messaging.TopicManagementService:jade.core.replication.AddressNotificationService -container "simulador01:agents.Simulador(estatica, 5)"

#------------------------------------------------------------------------------------
#Felipe
java -cp "lib/jade.jar:/home/felipelima/eclipse-workspace/felipetcc/target/classes" jade.Boot -container "SyncAgent:agents.SyncAgent" -services  jade.core.event.NotificationService:jade.core.messaging.TopicManagementService:jade.core.replication.AddressNotificationService 

#OK
#------------------------------------------------------------------------------------

#ta dando certo
java -cp "lib/jade.jar:/home/felipelima/eclipse-workspace/felipetcc/target/classes:lib/postgresql-9.2-1002.jdbc4.jar:lib/commons-pool-1.6-javadoc.jar:lib/commons-math3-3.2.jar:lib/jade/core/messaging" jade.Boot -container "simulador01:agents.Simulador(estatica, 5)" -services jade.core.messaging.TopicManagementService:jade.core.replication.AddressNotificationService


#Base
java -cp "lib/jade.jar:/home/felipelima/eclipse-workspace/felipetcc/target/classes:lib/postgresql-9.2-1002.jdbc4.jar:lib/commons-pool-1.6-javadoc.jar:lib/commons-math3-3.2.jar" jade.Boot -container "simulador01:agents.Simulador(dinamica_agentes_ult, 50)"



java -cp "lib/jade.jar:/home/felipelima/eclipse-workspace/felipetcc/target/classes" jade.Boot -container "simulador01:agents.Simulador(dinamica_agentes_ult, 50)"

