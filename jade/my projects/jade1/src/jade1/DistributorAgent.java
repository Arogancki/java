package jade1;

import java.util.concurrent.TimeUnit;

import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.util.leap.ArrayList;
import jade.util.leap.HashMap;
import jade.util.leap.LinkedList;
import jade.util.leap.List;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class DistributorAgent extends Agent {
	private int ILOSC_POTRZEBNYCH_AGENTOW=10;
	
	
	private List agents= new LinkedList(); 
	private List matrix = new ArrayList();
	private List matrixResult = new ArrayList();
	private List rowTODO = new ArrayList();
	public DistributorAgent(){
		for (int i=0; i<20; i++) {
			List list2 = new ArrayList();
			for (int j=0; j<20; j++) {
				list2.add(j);
			}
			matrix.add(list2);
		}
		for (int i=0; i<ILOSC_POTRZEBNYCH_AGENTOW; i++) {
			rowTODO.add(i);
		}
	}
	private JSONObject toJSON(List list) {
		JSONObject obj = new JSONObject();
	    for (int i=0; i<list.size(); i++) {
	    	JSONObject obj2 =new JSONObject();
	    	List l =(List) list.get(i);
	    	for (int j=0; j<l.size(); j++) {
	    		obj2.put(""+j, l.get(j));
	    	}
	    	obj.put(""+i, obj2);
	    }
	    return obj;
	}
	private void log(String text) {
		System.out.println("DistributorAgent - "+getAID()+": "+text);
	}
	protected void setup() {
		log("startup");
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("matrix-provider");
		sd.setName("DistributorAgent");
		dfd.addServices(sd);
		try {
			DFService.register(this, dfd);
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}
		addBehaviour(new calculate());
	}
	
	private class calculate extends Behaviour {
		private static final long serialVersionUID = 1L;
		public void action() {
			if (rowTODO.size()>0) {
				MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
				ACLMessage msg = myAgent.receive(mt);
				if (msg != null) {
					log("got a Counting Agent "+msg.getSender());
					ACLMessage reply = msg.createReply();
					reply.setPerformative(ACLMessage.PROPOSE);
					int rowToCount =(int) rowTODO.get(0);
					rowTODO.remove(0);
					
					JSONObject obj = new JSONObject();
					obj.put("m1", toJSON(matrix));
					obj.put("m2", toJSON(matrix));
					obj.put("threads", ILOSC_POTRZEBNYCH_AGENTOW);
					obj.put("id", rowToCount);
					reply.setContent(obj.toJSONString());
					
					agents.add(rowToCount);
					reply.setReplyWith(""+System.currentTimeMillis());
					myAgent.send(reply);
				}
			}
			for (int c=0; c<agents.size(); c++) {
				MessageTemplate mt = MessageTemplate.MatchConversationId("matrix-calculation-done;"+agents.get(c));
				ACLMessage reply = myAgent.receive(mt);
				if (reply != null) {
					String content = reply.getContent();
					if (reply.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {
						matrixResult.add(content);
						log("got an answer ("+matrixResult.size()+"/"+ILOSC_POTRZEBNYCH_AGENTOW+")");
						agents.remove(c);
						if (matrixResult.size() == ILOSC_POTRZEBNYCH_AGENTOW) {
							//log("got everytnig I wanted. Im ready to die :)");
							 HashMap hmap = new HashMap();
							 try {
								for (int k=0; k<matrixResult.size(); k++) {
									JSONParser parser = new JSONParser();
									JSONObject myObject = (JSONObject) parser.parse((String) matrixResult.get(k));
									long id = (long) myObject.get("id");
									hmap.put(""+id, (JSONObject) myObject.get("result"));
								}
								String WholeMatrix = "";
								for (int h=0; h<hmap.size(); h++) {
									JSONObject f = (JSONObject) hmap.get(""+h);
									for (int jj = 0; jj < f.size(); jj++) {
										JSONObject fjj = (JSONObject) f.get(""+jj);
										String row = "";
										for (int jja = 0; jja < fjj.size(); jja++) {
											long q = (long) fjj.get(""+jja);
											row += q + "\t";	
										}
										WholeMatrix += row + "\n";
									}
								}
								log("\nWYNIK:\n"+WholeMatrix);
							} catch (ParseException e) {
								log("JSON ERROR");
								e.printStackTrace();
							}
							agents.clear();
							matrix.clear();
							matrixResult.clear();
							myAgent.doDelete();
						}
					}
					else {
						log("got Failure");
						rowTODO.add(agents.get(c));
						agents.remove(c);
						
					}
				}
			}
			block();
		}
		public boolean done() {
			if (matrix.size()==0)
				return true;
			return false;
		}
	}
	
	protected void takeDown() {
		try {
			DFService.deregister(this);
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}
		log("Terminated");
	}
}
