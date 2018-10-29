package jade2;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
import jade.util.leap.LinkedList;

public class Philosopher extends Agent {
	private static final long serialVersionUID = 1L;
	Random rand = new Random();
	private String id = "";
	private int reactionTime = rand.nextInt(1000) + 500;
	private ArrayList forks = new ArrayList();
	private LinkedList myForks = new LinkedList();
	private LinkedList myForksID = new LinkedList();
	private AID foodBow = null;
	private ArrayList foodEaten = new ArrayList();
	private FoodBowStatus foodBowStatus = FoodBowStatus.UNKNOWN;
	private	enum FoodBowStatus {EMPTY, UNKNOWN, NOT_EMPTY};
	private ActionStatus actionStatus = ActionStatus.WORKING; 
	private	enum ActionStatus {WORKING, WAITING};
	private void log(String text) {
		System.out.println("Philosopher - "+getName()+": "+text);
	}
	protected void setup() {
		log("startup");
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("philosopher");
		sd.setName("philosopher-"+getName());
		dfd.addServices(sd);
		try {
			DFService.register(this, dfd);
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}
		addBehaviour(new TickerBehaviour(this, 2000) {
			private static final long serialVersionUID = 1L;
			protected void onTick() {
				if (foodBow==null) {
					log("checking for a food bow");
					DFAgentDescription template = new DFAgentDescription();
					ServiceDescription sd = new ServiceDescription();
					sd.setType("foodBow");
					template.addServices(sd);
					try {
						DFAgentDescription[] result = DFService.search(myAgent, template); 
						if (result.length != 0) {
							log("Sees a food bow");
							foodBow=result[0].getName();
							myAgent.addBehaviour(new Feast());
						}
					}
					catch (FIPAException fe) {
						fe.printStackTrace();
					}
				}
			}
		});
	}
	private class Feast extends CyclicBehaviour {
		private static final long serialVersionUID = 1L;			
		public void action() {
			// Update the list of forks
			DFAgentDescription template = new DFAgentDescription();
			ServiceDescription sd = new ServiceDescription();
			sd.setType("fork");
			template.addServices(sd);
			try {
				DFAgentDescription[] results = DFService.search(myAgent, template);
				int forksBefore=forks.size();
				for (DFAgentDescription r : results)
					if (!forks.contains(r.getName()))
						forks.add(r.getName());
				int forksAfter = forks.size() - forksBefore;
				if (forksAfter>0) {
					log("Sees new forks ("+forksAfter+").");
				}
			}
			catch (FIPAException fe) {
				fe.printStackTrace();
			}
			
			// let reaction time pass
			if (actionStatus==ActionStatus.WORKING) {
				try {
					TimeUnit.MILLISECONDS.sleep(reactionTime);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			// main logic
			if (myForksID.size() == 2) {
				// has 2 forks - eat
				if (actionStatus==ActionStatus.WORKING) {
					log("getting something to eat...");
					ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
					cfp.addReceiver(foodBow);
					id = ""+getAID()+System.currentTimeMillis();
					cfp.setConversationId(id);
					myAgent.send(cfp);
					actionStatus = ActionStatus.WAITING;
				}
				else {
					ACLMessage respond = myAgent.receive(MessageTemplate.MatchConversationId(id));
					if (respond!=null) {
						if (respond.getPerformative()==ACLMessage.ACCEPT_PROPOSAL) {
							String food = respond.getContent();
							foodEaten.add(food);
							log("is eating: "+food);
							foodBowStatus = FoodBowStatus.UNKNOWN;
						}
						else if (respond.getPerformative()==ACLMessage.REJECT_PROPOSAL) {
							foodBowStatus = FoodBowStatus.EMPTY;
						}
						releaseAllForks();
						actionStatus = ActionStatus.WORKING;
					}	
				}
			}
			else {
				// has not enough forks - try to get more
				if (foodBowStatus==FoodBowStatus.UNKNOWN) {
					// first get to know if there's still food
					if (actionStatus==ActionStatus.WORKING) {
						ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
						cfp.addReceiver(foodBow);
						cfp.setContent("checkup");
						id = ""+getAID()+System.currentTimeMillis();
						cfp.setConversationId(id);
						myAgent.send(cfp);
						actionStatus = ActionStatus.WAITING;
					}
					else {
						ACLMessage respond = myAgent.receive(MessageTemplate.MatchConversationId(id));
						if (respond!=null) {
							if (respond.getPerformative()==ACLMessage.CONFIRM) {
								foodBowStatus = FoodBowStatus.NOT_EMPTY;
							}
							else if (respond.getPerformative()==ACLMessage.DISCONFIRM) {
								foodBowStatus = FoodBowStatus.EMPTY;
							}
							actionStatus = ActionStatus.WORKING;
						}	
					}
				}
				if (foodBowStatus==FoodBowStatus.NOT_EMPTY) {
					// there's still food - try to get a fork
					if (!forks.isEmpty()) {
						if (actionStatus==ActionStatus.WORKING) {
							LinkedList freeForks = new LinkedList();
							for (int i=0; i<forks.size(); i++) {
								AID fork = (AID) forks.get(i);
								if (!myForks.contains(fork)) {
									freeForks.add(fork);
								}
							}
							if (!freeForks.isEmpty()) {
								AID fork = (AID) freeForks.get(rand.nextInt(freeForks.size()));
								log("Trying to get fork: "+fork+"...");
								ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
								cfp.addReceiver(fork);
								myForks.add(fork);
								id = ""+getAID()+System.currentTimeMillis();
								cfp.setConversationId(id);
								myAgent.send(cfp);
								actionStatus = ActionStatus.WAITING;
							}
							else {
								log("I already got all forks. Got to wait for some more!");
							}
						}
						else {
							ACLMessage respond = myAgent.receive(MessageTemplate.MatchConversationId(id));
							if (respond!=null) {
								if (respond.getPerformative()==ACLMessage.ACCEPT_PROPOSAL) {
									log("Got a new fork.");
									myForksID.add(respond.getConversationId());
								}
								else if (respond.getPerformative()==ACLMessage.REJECT_PROPOSAL) {
									myForks.remove(myForks.size()-1);
									log("This fork is already taken.");
									releaseAllForks();
								}
								actionStatus = ActionStatus.WORKING;
								foodBowStatus=FoodBowStatus.UNKNOWN;
							}
						}
					}
				}
			}
			
			if (foodBowStatus==FoodBowStatus.EMPTY) {
				log("has just realized all food is gone.");
				myAgent.doDelete();
			}
		}
		private void releaseAllForks() {
			for (int i=0; i<myForks.size(); i++) {
				ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
				cfp.addReceiver((AID) myForks.get(i));
				cfp.setConversationId((String) myForksID.get(i));
				myAgent.send(cfp);
			}
			myForks.clear();
			myForksID.clear();
			log("has put all forks down");
		}
	}
	protected void takeDown() {
		try {
			DFService.deregister(this);
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}
		log(" with reaction time "+ reactionTime +" ate "+foodEaten.size()+" things: " + String.join(", ", foodEaten.toList()));
	}
}
