package it.uniroma1.lcl;

import java.io.BufferedReader;
import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.view.Viewer;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

public class XMLprocessor 
{
	//lexicalizations -> bn
	private HashMap<String, Set<String>> map_lexs2BN = null;

	//bnID  -> set <id sentence>
	private HashMap<String, Set<Pair<Integer, String>>> map_bn2IDsentence = null;
	
	//id sentence  -> sentence
	private HashMap<Integer, String> map_IDsentence2sentence= null;
	
	//sentence -> id sentence
	private HashMap<String, Integer> map_sentence2IDsentence = null;
	
	private Graph graph = null;
	UndirectedGraph<String, DefaultEdge> graphT = null; 

	
	public HashMap<String, Set<String>> getMap_lexs2BNs() {
		return map_lexs2BN;
	}

	public HashMap<String, Set<Pair<Integer, String>>> getMap_bn2IDsentence() {
		return map_bn2IDsentence;
	}

	public HashMap<Integer, String> getMap_IDsentence2sentence() {
		return map_IDsentence2sentence;
	}

	public HashMap<String, Integer> getMap_sentence2IDsentence() {
		return map_sentence2IDsentence;
	}

	private int IDsent = 0;
	
	private String styleSheet =
			     "node { text-alignment: at-right; "
		         + "text-color: #222; "
		         + "text-background-mode: plain; "
		         + "text-background-color: white; "
		         + "text-style: bold;"
		         + "text-size: 16; }"
		         ;
	
	
	
	XMLprocessor(File folder, File file) throws Exception
	{
		if(folder!=null){
			
			init();
			Files.walk(folder.toPath())
			.filter(s->{ if(s.getFileName().toFile().getName().endsWith(".xml")) return true; else return false;})
			.forEach(xml -> {
				try {
					loadAnnotation(xml.toAbsolutePath().toFile());
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
			  
	    
			
		}else if(file!=null){
			
			if(file.getName().endsWith(".xml")){
				init();
				loadAnnotation(file);
				
			}
			
			   
	        
			
		}
		
	}
	
	private void init()
	{
		graphT = new SimpleGraph<>(DefaultEdge.class);
  
		map_lexs2BN = new HashMap<>();
		map_bn2IDsentence = new HashMap<>();
		map_IDsentence2sentence = new HashMap<>();
		map_sentence2IDsentence = new HashMap<>();
	}
	
	
	public void showGraph(String bnID)
	{
		Set<DefaultEdge> vicini = graphT.edgesOf(bnID);
		if(vicini!=null)
		{
			graph = new SingleGraph("Graph View");
			graph.addAttribute("ui.quality");
			graph.addAttribute("ui.antialias");
 			graph.addAttribute("ui.stylesheet", styleSheet);
 			graph.setAutoCreate(true);
 	        graph.setStrict(false);
 	        
 	      for(DefaultEdge de : vicini){
				String source = graphT.getEdgeSource(de);
				String target = graphT.getEdgeTarget(de);
 	        	graph.addEdge(source+target, source, target);
 	      }
 	        
		  for (Node node : graph) {
		        node.addAttribute("ui.label", node.getId());
		  }
			
 	      Viewer viewer =graph.display();
 	      viewer.setCloseFramePolicy(Viewer.CloseFramePolicy.HIDE_ONLY);

	        
		}
		
	}
	
	private void loadAnnotation(File xml)throws Exception
	{
		//System.out.println(xml.getAbsolutePath());
			
		BufferedReader in = Files.newBufferedReader(xml.toPath(), Charset.forName("UTF-8"));
		String line = null;
		
		boolean readText = false;
		ArrayList<String> sentences = new ArrayList<>();
		HashMap<String, Set<String>> map_anchor2BNs = new HashMap<>();
        
		HashSet<String> setBN = new HashSet<>();
        
		while ((line = in.readLine()) != null)
		{
			line = line.trim();
			if(line.equals("<text>")){
				readText = true;
				continue;
			}
			if(line.equals("</text>")){
				readText = false;
				continue;
			}
			
			if(readText){
				String cleanedSent = StringEscapeUtils.unescapeXml(line);
				sentences.add(cleanedSent);
				
				if(map_sentence2IDsentence.get(cleanedSent)==null){
					map_IDsentence2sentence.put(IDsent, cleanedSent);
					map_sentence2IDsentence.put(cleanedSent, IDsent);
					IDsent++;
				}
				
			}

			if(line.startsWith("<annotation source=")){
				String anchor = line.substring(line.indexOf("anchor=\"")+8, line.indexOf("\" bfScore="));
				String bnID = line.substring(line.lastIndexOf("\">")+2, line.indexOf("</annotation>"));
				setBN.add(bnID);
				
				Set<String> bns = map_lexs2BN.getOrDefault(anchor, new HashSet<String>());
				bns.add(bnID);
				map_lexs2BN.put(anchor, bns);
				
				Set<String> bnIds = map_anchor2BNs.getOrDefault(anchor, new HashSet<>());
				bnIds.add(bnID);
				map_anchor2BNs.put(anchor, bnIds);
				
			}
			
			if(line.equals("</annotations>"))
			{
				
				for(String sent : sentences){
					for(String anchor : map_anchor2BNs.keySet()){

						if(sent.matches(".*(\\s|^)("+Pattern.quote(anchor)+")(\\s|$).*")){
							Integer id = map_sentence2IDsentence.get(sent);
							
							Set<String> bnIds = map_anchor2BNs.get(anchor);
							for(String bn : bnIds){
								Set<Pair<Integer, String>> ids = map_bn2IDsentence.getOrDefault(bn, new HashSet<Pair<Integer, String>>());
								Pair<Integer, String> pair = new Pair<Integer, String>(id, anchor);
								ids.add(pair);
								
								map_bn2IDsentence.put(bn, ids);
							}
							
						}
						
					}
					
				}
				
				
			
				for(String bn1 : setBN){
					graphT.addVertex(bn1);
					for(String bn2 : setBN){
						if(!bn1.equals(bn2)){
							graphT.addVertex(bn2);
							graphT.addEdge(bn1, bn2);	
						}
					}
				}			
				setBN.clear();
				
				sentences.clear();
				map_anchor2BNs.clear();
			}
			
			
		}
		in.close();
		
	}
	
	
	
	
}
