package home.bankonit.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

import ca.bankonit.exceptions.*;
import ca.bankonit.manager.*;
import ca.bankonit.models.*;

/**
 * Renders the account window.
 * @author Leila Nalivkina, Nick Hamnett
 * @version Nov 5, 2021
 */
@SuppressWarnings("rawtypes")
public class AccountWindow extends JFrame {
	
	/**
	 * constant
	 */
	private static final long serialVersionUID = 1L;
	//fields
	private Account account;
	private BankManager bankManager;
	
	
	//UI fields
	private JLabel cardNum;
	private JLabel balance;	
	private JList transact;
	private JScrollPane transactScrollPane;
	private JLabel typeTransact;
	private JRadioButton deposit;
	private JRadioButton withdraw;
	private JLabel amount;
	private JTextField sum;
	private JButton submit;
	private JButton signout;
	
	//Text font
	private Font font;
	private Font cardFont;
	
	private ButtonClickListener btnActionListener;
	
	/**
	 * Initializes the account window
	 * @param account Account to manage
	 */
	public AccountWindow(Account account) {
		super("Bank On It Account");
		
		// Store account as field.
		this.account = account;
		
		this.bankManager = BankManagerBroker.getInstance();
		
		// Set size to 600x500
		this.setSize(600, 500);
		
		// Cause process to exit when X is clicked.
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		//Create action listener
		this.btnActionListener = new ButtonClickListener();
		
		//Center login window in screen
		this.setLocationRelativeTo(null);	
		
		//Text font SensSerif, plain
		font = new Font("Tahoma", Font.PLAIN, 14);
		cardFont = new Font("Tahoma", Font.PLAIN, 20);
		
		this.add(createNorthPanel(), BorderLayout.NORTH);
		
		this.add(createCenterPanel(), BorderLayout.CENTER);
		
		this.add(createSouthPanel(), BorderLayout.SOUTH);
			
		this.populateTransactions();
	}
	
	//Create panel with card # and balance
	private JPanel createNorthPanel() {
		JPanel northPanel = new JPanel();
		northPanel.setLayout(new BorderLayout());
	
		//Get an account number
		long cNum = this.account.getCardNumber();
		String cardNumFormatted = String.format("Card #%d", cNum); 
		this.cardNum = new JLabel(cardNumFormatted);
		this.cardNum.setFont(cardFont);
		
		 //Display in the center of panel
		this.cardNum.setHorizontalAlignment(SwingConstants.CENTER); 
		northPanel.add(cardNum, BorderLayout.NORTH);
		
		this.balance = new JLabel();
		this.balance.setFont(font);
		
		this.balance.setHorizontalAlignment(SwingConstants.CENTER);
		northPanel.add(balance, BorderLayout.SOUTH);
		
		return northPanel;
	}
	
	//Create panel to display transactions
	private JPanel createCenterPanel() {
		JPanel centerPanel = new JPanel();
		
		this.transact = new JList();
		this.transactScrollPane = new JScrollPane(this.transact) {
		
			/**
			 * constant
			 */
			private static final long serialVersionUID = 1L;

			public Dimension getPreferredSize() {
				return new Dimension(586, 320);
			}
		};
				
		centerPanel.add(transactScrollPane, BorderLayout.CENTER);
		
		return centerPanel;
	}
	
	//Create panel where user can interact (deposit/withdraw)
	private JPanel createSouthPanel() {
		JPanel southPanel = new JPanel();
		southPanel.setLayout(new BorderLayout());
		
		southPanel.setBorder(BorderFactory.createEmptyBorder(3, 0, 4, 0));
		
		JPanel inputPanel = new JPanel();
		JPanel outButtonPanel = new JPanel();
		
		this.typeTransact = new JLabel("Type:");
		this.typeTransact.setFont(font);
		inputPanel.add(this.typeTransact);
		
		//Group up buttons so only 1 can be selected
		ButtonGroup buttons = new ButtonGroup();

		this.deposit = new JRadioButton("Deposit");
		this.deposit.setFont(font);
		buttons.add(this.deposit);
		inputPanel.add(this.deposit);
		
		this.withdraw = new JRadioButton("Withdraw");
		this.withdraw.setFont(font);
		buttons.add(this.withdraw);
		inputPanel.add(this.withdraw);
		
		this.amount = new JLabel("Amount:");
		this.amount.setFont(font);
		inputPanel.add(this.amount);
		
		this.sum = new JTextField(10);
		this.sum.setFont(font);
		inputPanel.add(this.sum);
		
		this.submit = new JButton ("Submit");
		this.submit.setFont(font);
		this.submit.addActionListener(this.btnActionListener);
		inputPanel.add(this.submit);
		
		southPanel.add(inputPanel, BorderLayout.NORTH);
		
		this.signout = new JButton ("Signout");
		this.signout.setFont(font);
		this.signout.addActionListener(this.btnActionListener);
		outButtonPanel.add(this.signout);
		
		southPanel.add(outButtonPanel, BorderLayout.SOUTH);
		
		return southPanel;
	}	
	
	/**
	 * Clears and re-populates transactions as well as updates balance.
	 */
	@SuppressWarnings("unchecked")
	private void populateTransactions() {
		try {
			double balance = 0;
			ArrayList<Transaction> transactions = this.bankManager.getTransactionsForAccount(account);
			String[] listTransactions = new String[transactions.size()];
						
			for(int i = 0; i < transactions.size(); i++) {
				Transaction transaction = transactions.get(i);
				listTransactions[i] = transaction.toString();
				
				if (transaction.getTransactionType() == Transaction.TYPE_WITHDRAW) {
					balance -= transaction.getAmount();
				} else {
					balance += transaction.getAmount();
				}
			}
			
			this.balance.setText(String.format("Balance: $%.2f", balance));
			this.transact.setListData(listTransactions);
			
		} catch (InvalidAccountException e) {
			e.printStackTrace();
		}
	}
	
	private class ButtonClickListener implements ActionListener {	
		
		@Override
		public void actionPerformed(ActionEvent e) {
			Object source = e.getSource();
			
			if (AccountWindow.this.submit.equals(source)) {
				try {
					String inputAmount = AccountWindow.this.sum.getText();
					double sumInput = Double.parseDouble(inputAmount);
					
					if (sumInput == 0.0) {
						throw new NumberFormatException();
					}
					
					if (AccountWindow.this.withdraw.isSelected()) {						
						AccountWindow.this.bankManager.withdraw(account, sumInput);
						
						AccountWindow.this.populateTransactions();
						AccountWindow.this.bankManager.persist();
						
						JOptionPane.showMessageDialog(AccountWindow.this, "Withdraw is completed");
						
					} else 
						if (AccountWindow.this.deposit.isSelected()) {
							AccountWindow.this.bankManager.deposit(account, sumInput);
							
							AccountWindow.this.populateTransactions();
							AccountWindow.this.bankManager.persist();
							
							JOptionPane.showMessageDialog(AccountWindow.this, "Deposit is completed");
						}
				} catch (NumberFormatException numEx) {
					JOptionPane.showMessageDialog(AccountWindow.this, "Invalid amount. Please try again");
				} catch (InvalidAccountException accEx) {
					JOptionPane.showMessageDialog(AccountWindow.this, "An error occurred. Please try again");
				}

			} else if (source == AccountWindow.this.signout) {
				AccountWindow.this.bankManager.persist();
				AccountWindow.this.setVisible(false);
				JOptionPane.showMessageDialog(AccountWindow.this, "You are successfully signout");
				System.exit(0);
			}
		}
	}	
}
