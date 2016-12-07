package it.uniroma1.lcl;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.Set;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.jgrapht.alg.util.Pair;

public class SemanticIndexing extends JFrame						
{
	private static final long serialVersionUID = 1L;
	private JTextField entry;
    private JLabel jLabel1;
    private JScrollPane jScrollPane1;
    private JEditorPane textArea;
    
    private JButton pButton;
    private JButton pButtonGraph;
    
    private static final String messageSearch = "Enter a BabelNet ID to search";
    private static final String textButton = "Search";
    private static final String textButtonGraph = "Show Graph";
    private static final String titleApplet = "Semantic Indexing";
    private XMLprocessor xml = null;
    
    public SemanticIndexing() 
    {
        initComponents();    
    }
    
    private void addMenuItem(JMenu menu, String text, String description, ActionListener al)
    {
    	JMenuItem menuItem = new JMenuItem(text, KeyEvent.VK_T);
		menuItem.getAccessibleContext().setAccessibleDescription(description);
		menuItem.addActionListener(al);
		menu.add(menuItem);
    }
    
    private void initComponents() 
    {
    	
    	JMenuBar menuBar =  new JMenuBar();
    	
    	//Build the first menu.
    	JMenu menu = new JMenu("Menu");
    	menu.setMnemonic(KeyEvent.VK_A);
    	menuBar.add(menu);
    	
    	addMenuItem(menu, "open folder", "open a folder", new OpenFolderL());
    	
    	addMenuItem(menu, "open file", "open an xml file", new OpenFileL());

    	addMenuItem(menu, "Exit", "exit", new ExitListener());
    	
    	this.setJMenuBar(menuBar);
    	
        entry = new JTextField();
        //textArea = new JTextArea();
        textArea = new JEditorPane("text/html", "");

        jLabel1 = new JLabel();
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle(titleApplet);

//        textArea.setColumns(20);
//        textArea.setLineWrap(true);
//        textArea.setRows(5);
//        textArea.setWrapStyleWord(true);
        textArea.setEditable(false);
        jScrollPane1 = new JScrollPane(textArea);

        jLabel1.setText(messageSearch);

        pButton = new JButton(textButton);
        pButton.addActionListener(new SearchInMap());
        
        pButtonGraph  = new JButton(textButtonGraph);
        pButtonGraph.addActionListener(new ShowGraph());
        
        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        
		//Create a parallel group for the horizontal axis
		ParallelGroup hGroup = layout.createParallelGroup(GroupLayout.Alignment.LEADING);
		
		//Create a sequential and a parallel groups
		SequentialGroup h1 = layout.createSequentialGroup();
		ParallelGroup h2 = layout.createParallelGroup(GroupLayout.Alignment.TRAILING);

		//Add a container gap to the sequential group h1
		h1.addContainerGap();
		
		//Add a scroll pane and a label to the parallel group h2
		h2.addComponent(jScrollPane1, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 450, Short.MAX_VALUE);

		//Create a sequential group h3
		SequentialGroup h3 = layout.createSequentialGroup();
		h3.addComponent(jLabel1);
		h3.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED);
		h3.addComponent(entry, GroupLayout.DEFAULT_SIZE, 321, Short.MAX_VALUE);
		h3.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED);
		h3.addComponent(pButton, GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE);
		h3.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED);
		h3.addComponent(pButtonGraph, GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE);
		
		//Add the group h3 to the group h2
		h2.addGroup(h3);
		//Add the group h2 to the group h1
		h1.addGroup(h2);
	
		h1.addContainerGap();
		
		//Add the group h1 to the hGroup
		hGroup.addGroup(GroupLayout.Alignment.TRAILING, h1);
		//Create the horizontal group
		layout.setHorizontalGroup(hGroup);
		
	        
		//Create a parallel group for the vertical axis
		ParallelGroup vGroup = layout.createParallelGroup(GroupLayout.Alignment.LEADING);
		//Create a sequential group v1
		SequentialGroup v1 = layout.createSequentialGroup();
		//Add a container gap to the sequential group v1
		v1.addContainerGap();
		
		
		//Create a parallel group v2
		ParallelGroup v2 = layout.createParallelGroup(GroupLayout.Alignment.BASELINE);
		v2.addComponent(jLabel1);
		
		v2.addComponent(entry, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE);
		
		v2.addComponent(pButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE);
		
		v2.addComponent(pButtonGraph, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE);

		//Add the group v2 tp the group v1
		v1.addGroup(v2);
		v1.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED);
		v1.addComponent(jScrollPane1, GroupLayout.DEFAULT_SIZE, 233, Short.MAX_VALUE);
		v1.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED);
		v1.addContainerGap();
		
		//Add the group v1 to the group vGroup
		vGroup.addGroup(v1);
		//Create the vertical group
		layout.setVerticalGroup(vGroup);
		pack();
		
    }
    
    class OpenFolderL implements ActionListener {
        public void actionPerformed(ActionEvent e) {
          JFileChooser c = new JFileChooser();
          c.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
          // 
          int rVal = c.showOpenDialog(SemanticIndexing.this);
          if (rVal == JFileChooser.APPROVE_OPTION) {
        	 File folder = c.getSelectedFile();
        	 try {
        		textArea.setText("Loading corpora..."); 
        		
        		xml = new XMLprocessor(folder, null);
        		
        		textArea.setText("Corpora loaded<br>Please type your BabelNet ID");
        		
 			} catch (Exception e1) {
 				e1.printStackTrace();
 			}
        	 
          }
          if (rVal == JFileChooser.CANCEL_OPTION) {
        	 System.out.println("You pressed cancel");
            
          }
        }
      }
    
    class OpenFileL implements ActionListener {
        public void actionPerformed(ActionEvent e) {
          JFileChooser c = new JFileChooser();

          int rVal = c.showOpenDialog(SemanticIndexing.this);
          if (rVal == JFileChooser.APPROVE_OPTION){
        	 File file = c.getSelectedFile();
        	 try {
        		textArea.setText("Loading corpus..."); 
				xml = new XMLprocessor(null, file);

				textArea.setText("Corpus loaded<br>Please type your BabelNet ID");
			} catch (Exception e1) {
				e1.printStackTrace();
			}
        	 
          }
          if (rVal == JFileChooser.CANCEL_OPTION) {
        	 System.out.println("You pressed cancel");
            
          }
        }
      }
    
    
    class SearchInMap implements ActionListener 
    {
    	public void actionPerformed(ActionEvent e) 
    	{
    		StringBuilder allSentences = new StringBuilder();
        	if(xml!=null){
        		
        		if(xml.getMap_bn2IDsentence()!=null){

        	        String search = entry.getText();
        	        if(search.matches("bn:\\d+[nvar]")){
            	       
        	        	 String sents = findSents(search);
        	        	 allSentences.append(sents);
        	        	 
        	        }else{
        	        	
//        	        	Set<String> bnIDS = xml.getMap_lexs2BNs().get(search);
//        	        	if(bnIDS!=null){
//        	        		for(String bn : bnIDS){
//            	        		String sents = findSents(bn);
//            	        		allSentences.append(sents);
//            	        	}	
//        	        	}
        	        	
        	        }
        	     
        		}
        	}
        
        	String sents = allSentences.toString().trim();
        	if(sents!= null && !sents.isEmpty()){
        		textArea.setText(sents);
        	}else{
        		textArea.setText("BabelNet ID not found. Try with another one!");
        	}
        	
    	}
        
      }
    
    class ShowGraph implements ActionListener 
    {
    	public void actionPerformed(ActionEvent e) 
    	{
    		if(xml!=null){
    			  String search = entry.getText();
      	        if(search.matches("bn:\\d+[nvar]")){
      	        		xml.showGraph(search);      	        	
      	        }
    			
    		}
    		
    	}
        
     }

    private String findSents(String bnID)
    {
    	StringBuilder allSentences = new StringBuilder();
    	Set<Pair<Integer, String>> idsSentences = xml.getMap_bn2IDsentence().get(bnID);
    	
		if(idsSentences!=null){
			
			for(Pair<Integer, String> idAnchor : idsSentences){
				
				Integer id = idAnchor.first;
				String anchor = idAnchor.second;
				String sent = xml.getMap_IDsentence2sentence().get(id);
				if(sent!=null){
					allSentences.append(sent.replace(anchor, "<b>"+anchor+"</b>"));
					allSentences.append("<br>");	
				}
				
			}
		}   	
		
		return allSentences.toString().trim();	
			
    }
    

    private class ExitListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            int confirm = JOptionPane.showOptionDialog(SemanticIndexing.this,
                    "Are You Sure to Close this Application?",
                    "Exit Confirmation", JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE, null, null, null);
            if (confirm == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        }
    }
    
    public static void main(String args[]) {
    	
    	SwingUtilities.invokeLater(new Runnable() {
            public void run() {
             
                //UIManager.put("swing.boldMetal", Boolean.FALSE);
                new SemanticIndexing().setVisible(true);
            }
        });
    }

}
