package jade2;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class Fork extends Agent{
	private static final long serialVersionUID = 1L;
	private boolean isTaken = false;
	private String owner = "";
	private void log(String text) {
		System.out.println("Fork - "+getName()+": "+text);
	}
	protected void setup(){
		log("startup");
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("fork");
		sd.setName("fork-"+getName());
		dfd.addServices(sd);
		try {
			DFService.register(this, dfd);
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}
		addBehaviour(new CyclicBehaviour(this) {
			private static final long serialVersionUID = 1L;
			public void action() {
				ACLMessage msg = myAgent.receive(MessageTemplate.MatchPerformative(ACLMessage.CFP));
				if (msg != null) {
					if (!owner.equals("") && owner.equals(msg.getConversationId())) {
						isTaken = false;
						log("now's free");	
					}
					else {
						ACLMessage reply = msg.createReply();
						if (!isTaken) {
							isTaken = true;
							log("now's taken");
							reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
							owner = msg.getConversationId();
						}
						else {
							reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
						}
						reply.setConversationId(msg.getConversationId());
						myAgent.send(reply);
					}
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
