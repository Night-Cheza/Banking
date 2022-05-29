package home.bankonit.gui;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

import javax.swing.*;

import ca.bankonit.exceptions.InvalidCardNumberException;
import ca.bankonit.manager.*;
import ca.bankonit.models.*;


/**
 * Renders the login window
 * @author Leila Nalivkina, Nick Hamnett
 * @version Nov 5, 2021
 */
public class LoginWindow extends JFrame {
	
	/**
	 * constant
	 */
	private static final long serialVersionUID = 1L;
	
	//fields
	private Account accounts;
	private BankManager bankManager;
	
	//UI fields
	private JTextField cardNum;
	private JTextField pin;
	private JButton login;
	
	//Text font
	private Font font;
	private Font headFont;
	
	//event listener
	private LoginClickListener loginButton;
		
	/**
	 * Initializes the login window
	 */
	public LoginWindow() {
		super("Bank On It Login");
		
		// Set window size to 400x150
		this.setSize(400, 150);
		
		this.bankManager = BankManagerBroker.getInstance();	
		
		// Cause process to exit when X is clicked.
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		//create action listener
		this.loginButton = new LoginClickListener();
		
		// Center login window in screen
		this.setLocationRelativeTo(null);			
		
		//text font SensSerif, plain
		font = new Font("Tahoma", Font.PLAIN, 14);
		headFont = new Font("Tahoma", Font.PLAIN, 20);

		// Create panel
		JPanel panel = this.createPanel();
		
		// Add JPanel to the JFrame
		super.add(panel);
	}
	

	private JPanel createPanel() {
		JPanel p = new JPanel();
		
		//set padding
		p.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 10));
		
		//to set layout of a panel
		BorderLayout layoutSetting = new BorderLayout();
		p.setLayout(layoutSetting);
		
		p.add(createNorthPanel(), BorderLayout.NORTH);
		
		p.add(createInputPanel(), BorderLayout.CENTER);
		
		p.add(createLoginButtonPanel(), BorderLayout.SOUTH);
		
		return p;	

	}
	
	private JPanel createLoginButtonPanel() {
		JPanel loginButtonPanel = new JPanel();
		loginButtonPanel.setLayout(new FlowLayout());
		
		//Create button Login
		this.login = new JButton("Login");
		this.login.setFont(font);
		this.login.addActionListener(this.loginButton);
				
		loginButtonPanel.add(this.login);		

		return loginButtonPanel;
	}
	
	//Create center panel for inputs
	private JPanel createInputPanel() {
		JPanel cardNumPanel = new JPanel();
		
		//Create panel for card number
		JLabel cardLabel = new JLabel("Card Number:");
		cardLabel.setFont(font);
		cardNumPanel.add(cardLabel);
		
		this.cardNum = new JTextField(14);
		cardNumPanel.add(this.cardNum, BorderLayout.WEST);
		
		//Create panel for pin
		JPanel pinPanel = new JPanel();
		
		JLabel pinLabel = new JLabel("PIN:");
		pinLabel.setFont(font);
		pinPanel.add(pinLabel);
		
		pin = new JPasswordField(6);
		pinPanel.add(pin);
		
		cardNumPanel.add(pinPanel, BorderLayout.EAST);
		
		return cardNumPanel;
	}
	
	//Create panel to display name of the bank
	private JPanel createNorthPanel() {
		JPanel headPanel = new JPanel();
		
		JLabel bankName = new JLabel("Bank On It Login");
		
		bankName.setFont(headFont);		
		headPanel.add(bankName);
		
		return headPanel;
	}
		
	/**
	 * Load accounts from csv file 
	 */
	private void LoadAccounts() {
			
		File file = new File(BankManager.ACCOUNTS_TEXT);
		ArrayList<Account> ac = new ArrayList<Account>();
		
		try {
			Scanner in = new Scanner(file);
			
			while (in.hasNext()) {
				String accParts = in.next();
				String [] parts = accParts.split(";");

				long accNum = Long.parseLong(parts[0]);
				short pNum = Short.parseShort(parts[1]);
				this.accounts  = new Account (accNum, pNum); 
				ac.add(accounts);
			}

 			in.close();
		} catch (InvalidCardNumberException cardEx) {
			cardEx.printStackTrace();
		} catch (FileNotFoundException fileEx) {
			fileEx.printStackTrace();
		}		
	}		

	private class LoginClickListener implements ActionListener {
		
		@Override
		public void actionPerformed(ActionEvent e) {
			Object acc = e.getSource();			
			
			if (LoginWindow.this.login.equals(acc)) {
				String cardStr = LoginWindow.this.cardNum.getText();
				long cNum = Long.parseLong(cardStr);
				
				String pinStr = LoginWindow.this.pin.getText();
				short pinNum = Short.parseShort(pinStr);
				
				if (cNum == 0 || pinNum == 0) {
					throw new NumberFormatException();
				}
				
				try {
					Account account = new Account(cNum, pinNum);
									
				if (account != null) {
					LoginWindow.this.LoadAccounts();
					account = LoginWindow.this.bankManager.login(cNum,  pinNum);
					AccountWindow accountWindow = new AccountWindow(account);
					accountWindow.setVisible(true);
					LoginWindow.this.login.setEnabled(false);
					LoginWindow.this.setVisible(false); //hide login window
				} 
			} catch (InvalidCardNumberException cardEx) {				
				JOptionPane.showMessageDialog(null, "Invalid card number/pin. Please try again");
			}
			}
		}
	}
}
			

