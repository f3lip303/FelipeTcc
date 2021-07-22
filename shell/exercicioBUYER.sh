:: iniciar a plataforma
::java -cp lib\jade.jar jade.Boot -gui

:: iniciar um segundo conteiner na plataforma anterior
::java -cp lib\jade.jar;classes jade.Boot -container

:: iniciar a plataforma com a chamada de agentes
::java -cp lib\jade.jar;classes jade.Boot -gui -agents ping1:examples.PingAgent.PingAgent;ping2:examples.PingAgent.PingAgent 

:: iniciar um segundo conteiner na plataforma anterior com uma chamada a um novo agente
java -cp lib/jade.jar;bin jade.Boot -container -agents buyer01:logdyn.agents.Buyer;buyer02:logdyn.agents.Buyer;
java -cp lib/jade.jar jade.Boot -container -agents buyer01:logdyn.agents.Buyer;buyer02:logdyn.agents.Buyer;

:: iniciar um segundo conteiner na plataforma anterior com uma chamada a 5 novos agentes
::java -cp lib\jade.jar;bin jade.Boot -container -agents buyer01:logdyn.agents.Buyer;buyer02:logdyn.agents.Buyer;buyer03:logdyn.agents.Buyer;buyer04:logdyn.agents.Buyer;buyer05:logdyn.agents.Buyer
