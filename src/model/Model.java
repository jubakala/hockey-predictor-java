package model;

import java.sql.Connection;
import java.sql.DriverManager;

/*
 * Luokka toimii yliluokkana kaikille Model-luokille.
 */
public class Model 
{
	String server = "<server + port>";
	String database = "<database>";
	String username = "<username>";
	String password = "<password>";
	
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
