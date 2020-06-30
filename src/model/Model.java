package model;

import java.sql.Connection;
import java.sql.DriverManager;

/*
 * Luokka toimii yliluokkana kaikille Model-luokille.
 */
public class Model 
{
	// String server   = "den1.mysql6.gear.host";
	// String database = "nhl1";
	// String username = "nhl1";
	// String password = "Co8gk?js!je4";	
	String server = "localhost:3306";
	String database = "nhl";
	String username = "root";
	String password = "gu66EIwit";
	
	/*
	 * Metodi luo tietokantayhteyden. Myös Controller-luokassa on vastaava metodi.
	 * 
	 * @return		Luotu tietokantayhteys. Jos yhteyttä ei pystytty luomaan, palautetaan null-arvo.
	 */
	public Connection getConnection() 
	{
		// Luodaan tietokantayhteys.
        try 
        {
			Connection connection = DriverManager.getConnection("jdbc:mysql://" + server + "/" + database, username, password);

			// Jos tietokantayhteys onnistuttiin luomaan, se palautetaan kutsujalle.
            return connection;
        } 
        catch (Exception ex) 
        {
			// Jos tietokantayhteyden luominen ei onnistunut, palautetaan null.
			System.out.println(ex.getMessage());
        	return null;
        }
	}
}
