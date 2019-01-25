package jade1;

import java.util.Hashtable;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.util.leap.ArrayList;
import jade.util.leap.List;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class CountingAgent extends Agent {
	private int ERROR_PROP = 20;
	
	private static final long serialVersionUID = 2973508404347326141L;
	Random rand = new Random();
	int waitTime = rand.nextInt(1000) + 500;
	private int step = 0;
	private AID matrixProvider;
	public MessageTemplate mt;
	private boolean busy=false;
	public CountingAgent(){
	}
	private void log(String text) {
		System.out.println("CountingAgent - "+getAID()+": "+text);
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
	
	protected void setup(){
		log("startup");
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("matrix-cal");
		sd.setName("CountingAgent");
		dfd.addServices(sd);
		try {
			DFService.register(this, dfd);
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}
		addBehaviour(new TickerBehaviour(this, 5000) {
			private static final long serialVersionUID = 1L;
			protected void onTick() {
				log("checking for providers");
				// Update the list of seller agents
				DFAgentDescription template = new DFAgentDescription();
				ServiceDescription sd = new ServiceDescription();
				sd.setType("matrix-provider");
				template.addServices(sd);
				try {
					DFAgentDescription[] result = DFService.search(myAgent, template); 
					if (result.length != 0) {
						matrixProvider = result[0].getName();
						log("Found provider "+matrixProvider);
						busy=true;
						myAgent.addBehaviour(new calculate());
					}
				}
				catch (FIPAException fe) {
					fe.printStackTrace();
				}
			}
		});
	}
	
	private class calculate extends Behaviour {
		private static final long serialVersionUID = 1L;
		public void action() {
			switch (step) {
			case 0:
				ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
				cfp.addReceiver(matrixProvider);
				cfp.setConversationId("matrix-calculation");
				cfp.setReplyWith("cfp"+System.currentTimeMillis()); // Unique value
				myAgent.send(cfp);
				mt = MessageTemplate.and(MessageTemplate.MatchConversationId("matrix-calculation"),
						MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
				step = 1;
				break;
			case 1:
				// Receive all proposals/refusals from matrix providers agents
				ACLMessage msg = myAgent.receive(mt);
				if (msg != null) {
					if (msg.getPerformative() == ACLMessage.PROPOSE) {
						log("doing calculation");
						ACLMessage reply = msg.createReply();
						
						// przeparsowac dane
						String content = msg.getContent();
						try {
							// calculation
							int errorChance = rand.nextInt(99) + 1;
							if (errorChance<ERROR_PROP) {
								throw new Error("");
							}
							
							
							JSONParser parser = new JSONParser();
							JSONObject myObject = (JSONObject) parser.parse((String) content);
							long id = (long) myObject.get("id");
							long threads = (long) myObject.get("threads");
							JSONObject m1 = (JSONObject) myObject.get("m1");
							JSONObject m2 = (JSONObject) myObject.get("m2");
							
							reply.setConversationId("matrix-calculation-done;"+id);
							
							List matrix1 = new ArrayList();
							for (int i=0; i<m1.size(); i++) {
								JSONObject matrixI = (JSONObject) m1.get(""+i);
								List list2 = new ArrayList();
								for (int j=0; j<matrixI.size(); j++) {
									list2.add(matrixI.get(""+j));
								}
								matrix1.add(list2);
							}
							List matrix2 = new ArrayList();
							for (int i=0; i<m2.size(); i++) {
								JSONObject matrixI = (JSONObject) m2.get(""+i);
								List list2 = new ArrayList();
								for (int j=0; j<matrixI.size(); j++) {
									list2.add(matrixI.get(""+j));
								}
								matrix2.add(list2);
							}
							
							if (((ArrayList)matrix1.get(0)).size() != matrix2.size()) {
								throw new Error("");
							}
							
							double chunk = ((double)matrix1.size())/((double)threads);
							double decimal = chunk%1;
							chunk = Math.floor(chunk);
							long extraWork = (long) Math.round(decimal*((double)threads));
							long extraWorkStart = id==0 ? 0 : (
									id<extraWork ? id : extraWork
							);
							long start = (id*((long)chunk))+extraWorkStart;
							long end = start+((long)chunk)+ (id<extraWork?1:0);
							
							List wynik = new ArrayList();
							for (long count=(int) start; count<end; count++) {
								List list2 = new ArrayList();
								for (long count2=0; count2<((List)matrix2.get(0)).size(); count2++) {
									long suma=0;
									for (long count3=0; count3<((List)matrix1.get(0)).size(); count3++) {
										suma+=((long)((List)matrix1.get((int)count)).get((int)count3)) * ((long)((List)matrix2.get((int)count3)).get((int)count2)); 
									}
									list2.add(suma);
								}
								wynik.add(list2);
							}
							
							//calculation end
							
							JSONObject obj = new JSONObject();
							obj.put("result", toJSON(wynik));
							obj.put("id", id);
							
							reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
							reply.setContent(obj.toJSONString());
							log("Prepering to send data...");
							try {
								TimeUnit.MILLISECONDS.sleep(waitTime);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							log("Sent");
							myAgent.send(reply);
						}
						catch (Error | ParseException e) {
							reply.setPerformative(ACLMessage.FAILURE);
							JSONParser parser = new JSONParser();
							log("FAILURE!!");
							try {
								JSONObject myObject = (JSONObject) parser.parse((String) content);
								long id = (long) myObject.get("id");
								reply.setConversationId("matrix-calculation-done;"+id);
								log("Sending FAILURE");
								myAgent.send(reply);
							} catch (ParseException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
							try {
								TimeUnit.MILLISECONDS.sleep(2000);
							} catch (InterruptedException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
						}
						step=0;
					}
					else {
						log("Propose not accepted");
						busy=false;
						step=0;
					}
				}
				else {
					block();
				}
				break;
			}
		}
		public boolean done() {
			if (!busy)
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
