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


Quering Database
=================

In order to present the data, one could write a program that queries the database for Flow information and present the data.

Take note that for MySQL version >= 5.6, the IP Addresses are stored as  VARBINARY(16).

In order to translate binary strings to char strings, one would use the provided MySQL functions:

INET6_ATON() to convert string to binary string
INET6_NTOA() to convert binary string to string

You can read about the examples here: http://dev.mysql.com/doc/refman/5.6/en/miscellaneous-functions.html
