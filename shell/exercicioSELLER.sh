:: iniciar a plataforma
::java -cp lib\jade.jar jade.Boot -gui

:: iniciar um segundo conteiner na plataforma anterior
::java -cp lib\jade.jar;classes jade.Boot -container

:: iniciar a plataforma com a chamada de agentes
::java -cp lib\jade.jar;classes jade.Boot -gui -agents ping1:examples.PingAgent.PingAgent;ping2:examples.PingAgent.PingAgent 

:: iniciar um segundo conteiner na plataforma anterior com uma chamada a 2 novos agentes
java -cp lib\jade.jar;classes jade.Boot -container -agents seller01:logdyn.agents.Seller;seller02:logdyn.agents.Seller;seller03:logdyn.agents.Seller;

:: iniciar um segundo conteiner na plataforma anterior com uma chamada a 10 novos agentes
::java -cp lib\jade.jar;classes jade.Boot -container -agents seller01:logdyn.agents.Seller;seller02:logdyn.agents.Seller;seller03:logdyn.agents.Seller;seller04:logdyn.agents.Seller;seller05:logdyn.agents.Seller;seller06:logdyn.agents.Seller;seller07:logdyn.agents.Seller;seller08:logdyn.agents.Seller;seller09:logdyn.agents.Seller;seller10:logdyn.agents.Seller