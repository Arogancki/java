package jade2;

import java.util.Random;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.util.leap.ArrayList;

public class FoodBow extends Agent {
	private static final long serialVersionUID = 1L;
	Random rand = new Random();
	private int initialBowSize = 10;
	private ArrayList food = new ArrayList();
	private String[] foodTypes = {
			"kebab", "hamburger", "nudle", "apple", "soup", 
			"beer", "cake", "eggs", "pork", "chicken", 
			"pie", "fish", "rise", "salad", "sandwich", "stew"};
	private void log(String text) {
		System.out.println("FoodBow - "+getName()+": "+text);
	}
	private String getRandomFood() {
		return foodTypes[rand.nextInt(foodTypes.length)];
	}
	public FoodBow(){
		for (int i=0; i<initialBowSize; i++) {
			food.add(getRandomFood());
		}
	}
	protected void setup() {
		log("startup");
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("foodBow");
		sd.setName("foodBow-"+getName());
		dfd.addServices(sd);
		try {
			DFService.register(this, dfd);
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}
		log("Food bow holds "+food.size()+" portions.");
		addBehaviour(new CyclicBehaviour(this) {
			private static final long serialVersionUID = 1L;
			public void action() {
				ACLMessage msg = myAgent.receive(MessageTemplate.MatchPerformative(ACLMessage.CFP));
				if (msg != null) {
					ACLMessage reply = msg.createReply();
					if (msg.getContent()!=null) {
						if (!food.isEmpty()) {
							reply.setPerformative(ACLMessage.CONFIRM);	
						}
						else {
							reply.setPerformative(ACLMessage.DISCONFIRM);
						}
					}
					else {
						if (!food.isEmpty()) {
							reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
							reply.setContent((String) food.get(0));
							food.remove(0);
							log("Food bow now holds "+food.size()+" portions.");
						}
						else {
							reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
							log("Food bow is already empty.");
						}	
					}
					reply.setConversationId(msg.getConversationId());
					myAgent.send(reply);
				}
				block();
			}
		});
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
