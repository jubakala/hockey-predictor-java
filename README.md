This is the Java code of my master's thesis. The code is from year 2013. I only made small fixes to make sure it is still working. The theme of my thesis was to find out if it is possible to predict (NHL) hockey match outcomes (home win, away win, draw) programatically. Only statistical data which was used was the result of the game, ie. scored goals by both team. Also betting odds from several bookies were used to test, if the predictions the program made would be profitable as well.

In order to run the code, a MySQL server is required. The database SQL-file is provided with in the code folder (/database/nhl.sql). Also, Java MySQL connector is required for the code to work.

Intallation:

After you have a working database-server and the provided database loaded:

update files src/controller/Controller.java and src/model/Model.java with your MySQL configuration data (server, port, database name, username and password).
set the path to you Java MySQL connector in the /.classpath -file.
Unfortunately the thesis as well as the comments in the code are in Finnish.
