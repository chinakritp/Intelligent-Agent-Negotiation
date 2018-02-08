import java.util.ArrayList;

import negotiator.AgentID;
import negotiator.Bid;

import negotiator.issue.Issue;
import negotiator.issue.IssueDiscrete;
import negotiator.issue.Value;
import negotiator.issue.ValueDiscrete;

import negotiator.actions.Accept;
import negotiator.actions.Action;
import negotiator.actions.Offer;
import negotiator.parties.AbstractNegotiationParty;
import negotiator.parties.NegotiationInfo;
import negotiator.utility.AdditiveUtilitySpace;
import negotiator.utility.AbstractUtilitySpace;
import negotiator.utility.Evaluator;
import negotiator.utility.EvaluatorDiscrete;

import java.util.Arrays;
import java.util.List;

public class AgentAA extends AbstractNegotiationParty {
    private final String description = "AA Agent";

    private Bid lastReceivedOffer; // offer on the table
    private Bid myLastOffer;

    private List<issueWeight> weightArray = new ArrayList<issueWeight>();

//    private issueWeight[] weightArray = new issueWeight[];

    class issueWeight {
        public String name;
        public double weight;
        public int priority;

        public issueWeight(String n, double w, int p){
            this.name = n;
            this.weight = w;
            this.priority = p;
        }

        public String getName(){
            return name;
        }
        public double getWeight(){
            return weight;
        }
        public double getPriority(){
            return priority;
        }
    }

    @Override
    public void init(NegotiationInfo info) {
        super.init(info);

        AbstractUtilitySpace utilitySpace = info.getUtilitySpace();
        AdditiveUtilitySpace additiveUtilitySpace = (AdditiveUtilitySpace) utilitySpace;

        List<Issue> issues = additiveUtilitySpace.getDomain().getIssues();
//        ArrayList<Issue> issueList = (ArrayList<Issue>) info.getUtilitySpace().getDomain().getIssues();
        int nIssues = issues.size();

//        issueWeight[] weightArray = new issueWeight[nIssues];
//        weightArray = new issueWeight[nIssues];

        for(int i=0; i<issues.size();i++) {
            Issue issue = issues.get(i);

            String name = issue.getName();
            double weight = additiveUtilitySpace.getWeight(issue.getNumber());
            double avgWeight = (1 / (double)nIssues);
            System.out.println(nIssues);
            System.out.println(avgWeight);
            int priority = 0;
            if(weight >= avgWeight){
                priority = 1;
            }else if(weight >= (avgWeight/2)){
                priority = 2;
            }else{
                priority = 3;
            }



            weightArray.add(new issueWeight(name, weight, priority));

//            weightArray[i] = new issueWeight();
//            weightArray[i].name = issue.getName();
//            weightArray[i].weight = additiveUtilitySpace.getWeight(issue.getNumber());
            System.out.println(weightArray.get(i).getName() + " - weight : " + weightArray.get(i).getWeight() + " - weight : " + weightArray.get(i).getPriority());
        }
    }

    /**
     * When this function is called, it is expected that the Party chooses one of the actions from the possible
     * action list and returns an instance of the chosen action.
     *
     * @param list
     * @return
     */
    @Override
    public Action chooseAction(List<Class<? extends Action>> list) {// According to Stacked Alternating Offers Protocol list includes
        // Accept, Offer and EndNegotiation actions only.
        double time = getTimeLine().getTime(); // Gets the time, running from t = 0 (start) to t = 1 (deadline).
        // The time is normalized, so agents need not be
        // concerned with the actual internal clock.


        // First half of the negotiation offering the max utility (the best agreement possible) for Example Agent
        if (time < 0.5) {
            return new Offer(this.getPartyId(), this.getMaxUtilityBid());
        } else {

            // Accepts the bid on the table in this phase,
            // if the utility of the bid is higher than Example Agent's last bid.
            if (lastReceivedOffer != null
                    && myLastOffer != null
                    && this.utilitySpace.getUtility(lastReceivedOffer) > this.utilitySpace.getUtility(myLastOffer)) {

                return new Accept(this.getPartyId(), lastReceivedOffer);
            } else {
                // Offering a random bid
                myLastOffer = generateRandomBid();
                return new Offer(this.getPartyId(), myLastOffer);
            }
        }
    }

    /**
     * This method is called to inform the party that another NegotiationParty chose an Action.
     * @param sender
     * @param act
     */
    @Override
    public void receiveMessage(AgentID sender, Action act) {
        super.receiveMessage(sender, act);

        if (act instanceof Offer) { // sender is making an offer
            Offer offer = (Offer) act;

            // storing last received offer
            lastReceivedOffer = offer.getBid();
        }
    }

    /**
     * A human-readable description for this party.
     * @return
     */
    @Override
    public String getDescription() {
        return description;
    }

    private Bid getMaxUtilityBid() {
        try {
            return this.utilitySpace.getMaxUtilityBid();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}