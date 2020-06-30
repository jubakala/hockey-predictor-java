package view;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;
import javax.swing.JSeparator;
import javax.swing.JComboBox;
import java.awt.Font;
import javax.swing.JButton;
import javax.swing.JTextArea;
import controller.MainController;

import model.Game;
import model.Counter;
import model.Wallet;

import java.util.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JScrollPane;
import javax.swing.JCheckBox;

/*
 * Tämä luokka huolehtii käyttöliittymäikkunaan liittyvistä toiminnoista.
 */
public class MainWindow 
{
	private JFrame frame;
	private final ButtonGroup buttonGroup = new ButtonGroup();
	private JComboBox<String> seasonComboBox = new JComboBox<String>();
	private Game game = new Game();
	private Vector<String> seasons = game.getSeasons();
	private JComboBox<Integer> latestGamesComboBox = new JComboBox<Integer>();
	private JComboBox<Double> walletComboBox = new JComboBox<Double>();
	private JComboBox<Integer> kellyComboBox = new JComboBox<Integer>();
	private JRadioButton poissonRB;
	private JRadioButton eloRB;
	private JRadioButton piHockeyRB;
	private JRadioButton aggregateRB;
	private JRadioButton goalBasedEloRB;
	private JCheckBox weightedEloCB;
	private JCheckBox homeAwayCB;
	private JButton simulateButton;
	private JTextArea resultsArea = new JTextArea();
	private JComboBox<String> bettingComboBox = new JComboBox<String>();
	
	/*
	 * Konstruktori, joka kutsuu ikkunan alustamismetodia.
	 */
	public MainWindow() 
	{
		initialize();
	}

	/* 
	 * Metodi käynnistää käyttöliittymäikkunan omaan säikeeseensä.
	 */
	public void launch() 
	{
		EventQueue.invokeLater(new Runnable() 
		{
			public void run() 
			{
				try 
				{
					MainWindow window = new MainWindow();
					// Näytetään ikkuna.
					window.frame.setVisible(true);
				}
				catch (Exception e) 
				{
					e.printStackTrace();
				}
			}
		});
	}

	/*
	 * Metodi alustaa ja asettelee oikeille paikoilleen kaikki tarvittavat käyttöliittymäkomponentit.
	 */
	private void initialize() 
	{
		frame = new JFrame();
		frame.setBounds(100, 100, 737, 598);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		// Ikkunan kokoa ei haluta muutettavan.
		frame.setResizable(false);
		// Ikkunan otsikko.
		frame.setTitle("Jääkiekko-otteluiden simulointisovellus");
		frame.setLocationRelativeTo(null);
		
		JLabel titleLabel = new JLabel("Valitse:");
		titleLabel.setFont(new Font("Tahoma", Font.PLAIN, 14));
		titleLabel.setBounds(10, 27, 46, 14);
		frame.getContentPane().add(titleLabel);
		
		JLabel modelLabel = new JLabel("Malli");
		modelLabel.setBounds(14, 52, 46, 14);
		frame.getContentPane().add(modelLabel);
		
		poissonRB = new JRadioButton("Poisson");
		
		poissonRB.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent event) 
			{
				updateSeasons(seasons, seasonComboBox, "poisson");
				latestGamesComboBox.setEnabled(true);
				simulateButton.setEnabled(true);
				
				weightedEloCB.setEnabled(false);
				homeAwayCB.setEnabled(false);
			}
		});
		
		buttonGroup.add(poissonRB);
		poissonRB.setBounds(10, 73, 109, 23);
		frame.getContentPane().add(poissonRB);
		
		eloRB = new JRadioButton("Elo-malli - perus");
		
		eloRB.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent event) 
			{
				updateSeasons(seasons, seasonComboBox, "elo");
				latestGamesComboBox.setEnabled(false);
				simulateButton.setEnabled(true);
				
				homeAwayCB.setEnabled(true);
				weightedEloCB.setEnabled(true);
			}
		});
		
		buttonGroup.add(eloRB);
		eloRB.setBounds(140, 99, 149, 23);
		frame.getContentPane().add(eloRB);
		
		goalBasedEloRB = new JRadioButton("Elo-malli - maaliperustainen");
		goalBasedEloRB.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent event) 
			{
				updateSeasons(seasons, seasonComboBox, "elo");
				latestGamesComboBox.setEnabled(false);
				simulateButton.setEnabled(true);
				
				homeAwayCB.setEnabled(true);
				weightedEloCB.setEnabled(true);
			}
		});
		
		buttonGroup.add(goalBasedEloRB);
		goalBasedEloRB.setBounds(10, 125, 239, 23);
		frame.getContentPane().add(goalBasedEloRB);
		
		piHockeyRB = new JRadioButton("Pi-Hockey");
		piHockeyRB.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent event) 
			{
				updateSeasons(seasons, seasonComboBox, "pi-hockey");
				latestGamesComboBox.setEnabled(false);
				simulateButton.setEnabled(true);
				
				homeAwayCB.setEnabled(false);
				weightedEloCB.setEnabled(false);
			}
		});
		
		buttonGroup.add(piHockeyRB);
		piHockeyRB.setBounds(140, 73, 109, 23);
		frame.getContentPane().add(piHockeyRB);
		
		aggregateRB = new JRadioButton("Agregaatti-malli");
		
		aggregateRB.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent event) 
			{
				updateSeasons(seasons, seasonComboBox, "aggregate");
				latestGamesComboBox.setEnabled(false);
				simulateButton.setEnabled(true);
				
				homeAwayCB.setEnabled(false);
				weightedEloCB.setEnabled(false);
			}
		});
		
		buttonGroup.add(aggregateRB);
		aggregateRB.setBounds(10, 99, 116, 23);
		frame.getContentPane().add(aggregateRB);
		
		JLabel seasonLabel = new JLabel("Kausi");
		seasonLabel.setBounds(10, 168, 46, 14);
		frame.getContentPane().add(seasonLabel);
		
		seasonComboBox.setBounds(10, 193, 109, 23);
		frame.getContentPane().add(seasonComboBox);
		
		updateSeasons(seasons, seasonComboBox, "poisson");
		
		JLabel kellyLabel = new JLabel("Kellyn jakaja");
		kellyLabel.setBounds(140, 168, 102, 14);
		frame.getContentPane().add(kellyLabel);
		
		// Lisätään Kellyn jakajan mahdolliset arvot valikkoon.
		for (int jj = 1; jj <= 10; jj++) { kellyComboBox.addItem(jj); }
		
		kellyComboBox.setBounds(140, 193, 123, 22);
		// Valitaan oletuksena Kelly jakajaksi aina 5.
		kellyComboBox.setSelectedIndex(4);
		frame.getContentPane().add(kellyComboBox);
		
		JLabel walletLabel = new JLabel("Pelikassa");
		walletLabel.setBounds(10, 227, 91, 14);
		frame.getContentPane().add(walletLabel);
		
		walletComboBox.setBounds(10, 252, 109, 22);
		
		// Asetetaan pelikassan mahdolliset alkusaldot.
		walletComboBox.addItem((double)100);
		walletComboBox.addItem((double)500);
		walletComboBox.addItem((double)1000);
		walletComboBox.addItem((double)1500);
		walletComboBox.addItem((double)2000);
		walletComboBox.addItem((double)2500);
		
		walletComboBox.setSelectedIndex(2);
		
		frame.getContentPane().add(walletComboBox);
		
		JSeparator upperSeparator = new JSeparator();
		upperSeparator.setBounds(10, 155, 289, 2);
		frame.getContentPane().add(upperSeparator);
		
		JSeparator middleSeparator = new JSeparator();
		middleSeparator.setBounds(10, 285, 289, 3);
		frame.getContentPane().add(middleSeparator);
		
		JLabel gameAmountLabel = new JLabel("Huomioitavien viimeotteluiden m\u00E4\u00E4r\u00E4 \n");
		gameAmountLabel.setBounds(10, 299, 281, 23);
		frame.getContentPane().add(gameAmountLabel);
		
		JLabel gameAmountInstructionsLabel = new JLabel("(vain Poisson, 0 = kaikki viime ottelut)");
		gameAmountInstructionsLabel.setBounds(10, 321, 256, 14);
		frame.getContentPane().add(gameAmountInstructionsLabel);
		
		latestGamesComboBox.setBounds(10, 346, 109, 22);
		
		// Asetetaan mahdollisten viimeisten laskettavien pelien määrät.
		for (int kk = 0; kk <= 6; kk++) { latestGamesComboBox.addItem(kk); }
		
		latestGamesComboBox.setEnabled(false);
		frame.getContentPane().add(latestGamesComboBox);
		
		JSeparator lowerSeparator = new JSeparator();
		lowerSeparator.setBounds(10, 380, 289, 2);
		frame.getContentPane().add(lowerSeparator);
		
		simulateButton = new JButton("Simuloi");
		simulateButton.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent e) 
			{
				simulate();
			}
		});
		
		simulateButton.setBounds(208, 483, 91, 23);
		frame.getContentPane().add(simulateButton);
		
		simulateButton.setEnabled(false);
		
		JButton exitButton = new JButton("Lopeta");
		exitButton.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent event) 
			{
				// Suljetaan sovellus.
				System.exit(0);
			}
		});
		
		exitButton.setBounds(107, 483, 91, 23);
		frame.getContentPane().add(exitButton);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(313, 11, 404, 498);
		frame.getContentPane().add(scrollPane);
		scrollPane.setViewportView(resultsArea);
		
		JLabel bettingLabel = new JLabel("Ly\u00F6d\u00E4\u00E4n vetoa");
		bettingLabel.setBounds(140, 226, 116, 14);
		frame.getContentPane().add(bettingLabel);
		
		bettingComboBox.addItem("Kaikista otteluista");
		bettingComboBox.addItem("Vain kotivoitoista");
		bettingComboBox.addItem("Vain vierasvoitoista");
		
		bettingComboBox.setBounds(140, 252, 123, 22);
		frame.getContentPane().add(bettingComboBox);
		
		JLabel eloModelsLabel = new JLabel("Vain elo-mallit");
		eloModelsLabel.setBounds(10, 393, 116, 14);
		frame.getContentPane().add(eloModelsLabel);
		
		weightedEloCB = new JCheckBox("Painotettu - edellisen kauden elo-luvut mukana");
		weightedEloCB.setBounds(10, 440, 279, 23);
		frame.getContentPane().add(weightedEloCB);
		
		homeAwayCB = new JCheckBox("Koti- ja vieras-elo-luvut erikseen");
		homeAwayCB.setBounds(10, 414, 279, 23);
		frame.getContentPane().add(homeAwayCB);
		
		weightedEloCB.setEnabled(false);
		homeAwayCB.setEnabled(false);
		
		JSeparator separator = new JSeparator();
		separator.setBounds(10, 470, 289, 2);
		frame.getContentPane().add(separator);
		
		JButton cleanTextAreaButton = new JButton("Tyhjenn\u00E4 tekstikentt\u00E4");
		// Tyhjennetään tekstikenttä.
		cleanTextAreaButton.addActionListener(new ActionListener() 
		{
			// 
			public void actionPerformed(ActionEvent e) 
			{
				resultsArea.setText("");
			}
		});
		
		cleanTextAreaButton.setBounds(551, 522, 166, 23);
		frame.getContentPane().add(cleanTextAreaButton);
	}
	
	/*
	 * Metodi käynnistää varsinaisen simuloinnin käyttäjän valintojen perusteella. Simulointinappia ei voi painaa, jos jokin malli ei ole valittuna. 
	 */
	public void simulate()
	{
		Counter counter;
		MainController mC 		= MainController.getInstance(); 
		Wallet wallet 			= Wallet.getInstance();
		
		double walletBalance 	= (double)walletComboBox.getSelectedItem();
		int kellyDivider  		= (int)kellyComboBox.getSelectedItem();
		String season 			= (String)seasonComboBox.getSelectedItem();
		String whatToPlayStr	= (String)bettingComboBox.getSelectedItem();
		int whatToPlay			= 0;
		
		if (whatToPlayStr.equals("Kaikista otteluista"))  { whatToPlay = 0; }
		if (whatToPlayStr.equals("Vain kotivoitoista"))   { whatToPlay = 1; }
		if (whatToPlayStr.equals("Vain vierasvoitoista")) { whatToPlay = 2; }
		
		wallet.setBalance(walletBalance);	
		wallet.setKellyDivider(kellyDivider);
		wallet.setCountersZero();
		
		// Jos simulointiin halutaan käyttää Poisson-mallia.
		if (poissonRB.isSelected()) 
		{ 
			int latestGamesCount = (int)latestGamesComboBox.getSelectedItem();
			
			if (latestGamesCount == 0) { latestGamesCount = -1; }
			
			counter = mC.poisson(season, latestGamesCount, whatToPlay, wallet);
			String resultString = "Poisson-malli:\n" + counter.print(season, wallet);
			resultsArea.append(resultString);
		}
		
		// Jos simulointiin halutaan käyttää Elo-mallia. 		
		if (eloRB.isSelected() || goalBasedEloRB.isSelected()) 
		{ 
			boolean homeAway  = false;
			boolean goalBased = false;
			String title	  = "Perus-elo-malli:\n";
			
			if (goalBasedEloRB.isSelected())
			{
				goalBased = true;
				title 	  = "Maaliperusteinen elo-malli:\n";
			}
			
			boolean weighted  = false;
			
			if (homeAwayCB.isSelected()) 	{ homeAway = true;  }
			if (weightedEloCB.isSelected()) { weighted = true; title = "Painotettu " + title; }
			
			counter = mC.elo(season, whatToPlay, wallet, homeAway, goalBased, weighted);
			String resultString = title + counter.print(season, wallet);
			resultsArea.append(resultString);
		}

		// Jos simulointiin halutaan käyttää pi-hockey -mallia.
		if (piHockeyRB.isSelected()) 
		{ 
			counter = mC.piHockey(season, whatToPlay, wallet);
			String resultString = "Pi-Hockey:\n" + counter.print(season, wallet);
			resultsArea.append(resultString);
		}
		
		// Jos simulointiin halutaan käyttää aggregaatti-mallia.
		if (aggregateRB.isSelected()) 
		{ 
			counter = mC.aggregateModel(season, whatToPlay, wallet);
			String resultString = "Aggregaatti-malli:\n" + counter.print(season, wallet);
			resultsArea.append(resultString);
		}
	}
	
	/*
	 * Metodi päivittää kausivalikkoa. Vain Poisson-mallilla voidan simuloida kausia 2002-2008.
	 */
	public void updateSeasons(Vector<String> seasons, JComboBox<String> seasonCB, String caller)
	{
		seasonCB.removeAllItems();
		
		// Vain Poissonia varten tarvitaan kaudet 2002-2008.
		if (caller.equals("poisson"))
		{
			for (int ii = 0; ii < seasons.size(); ii++)
			{	
				seasonCB.addItem(seasons.get(ii));
			}
		}
		else
		{
			for (int ii = 5; ii < seasons.size(); ii++)
			{
				seasonCB.addItem(seasons.get(ii));
			}
		}
	}
}
