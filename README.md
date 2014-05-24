FlowCollector
=============

FlowCollector is a program designed to collect, and analyze NetFlow / sFlow traffic from routers/ switches.


Configuration
=============

The collector is designed to work with a backend MySQL database. As such, the database configuration must be saved in the file 'config.txt'. 
Refer to config.txt for a more detailed explanation.


Running
=============

To run the program, issue the following command in the terminal:

./FlowCollector

To run the program and specifying the SFlow/ NetFlow port to listen on:

./FlowCollector -s [SflowPort] -n [NetFlowPort]

To display the output of the packets

./FlowCollector -o
