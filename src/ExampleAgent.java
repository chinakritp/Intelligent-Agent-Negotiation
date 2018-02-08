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

/**
 * ExampleAgent returns the bid that maximizes its own utility for half of the negotiation session.
 * In the second half, it offers a random bid. It only accepts the bid on the table in this phase,
 * if the utility of the bid is higher than Example Agent's last bid.
 */
public class ExampleAgent extends AbstractNegotiationParty {
    private final String description = "Example Agent";

    private Bid lastReceivedOffer; // offer on the table
    private Bid myLastOffer;

    private class issueWeight {
        String name;
        double weight;
    }

//    public class getIssueWeight {
//        public void main(String args[]) {
//            IssueWeight[] weightArray = new IssueWeight[3];
//            weightArray[1] = new IssueWeight();
//            weightArray[1].name = "test";
//            weightArray[1].weight = 3;
//            System.out.println(weightArray[1].name);
////            IssueWeightArray =  weightArray;
//        }
//    }

    @Override
    public void init(NegotiationInfo info) {
//        System.out.println(info.getUtilitySpace());
//        System.out.println(info);
//
//        System.out.println(info.getUtilitySpace().getDomain().getIssues());
////        List<ValueDiscrete> values = ((IssueDiscrete) issues.get(index)).getValues();
////        private eval = EvaluatorDiscrete;
////        System.out.println(EvaluatorDiscrete.get);
//
////        List values = ((IssueDiscrete) issues.get(index)).getValues();
////        System.out.println(values);
//
//        ArrayList<Issue> issueList = (ArrayList<Issue>) info.getUtilitySpace().getDomain().getIssues();
//        ArrayList<ArrayList<ValueDiscrete>> listValueList= new ArrayList<ArrayList<ValueDiscrete>>();
//        ArrayList<Bid> bidList = new ArrayList<Bid>();
//        int bidListsize=1;
//        int nIssues = issueList.size();
//        int[] nValues = new int[nIssues];
//        for(int i=0; i<issueList.size();i++) {
//            listValueList.add((ArrayList<ValueDiscrete>) ((IssueDiscrete) issueList.get(i)).getValues());
//            nValues[i]=listValueList.get(i).size();
//            bidListsize=bidListsize*nValues[i];
//        }
//        System.out.println(listValueList);
//
//        System.out.println("weight");
        AbstractUtilitySpace utilitySpace = info.getUtilitySpace();
        AdditiveUtilitySpace additiveUtilitySpace = (AdditiveUtilitySpace) utilitySpace;

        List<Issue> issues = additiveUtilitySpace.getDomain().getIssues();
//
//        for (Issue issue : issues) {
//            System.out.println(additiveUtilitySpace.getWeight(issue.getNumber()));
//        }
//        System.out.println(((AdditiveUtilitySpace) info.getUtilitySpace()).getWeight((((AdditiveUtilitySpace) utilitySpace).getDomain().getIssues()).size()));
        super.init(info);

        System.out.println("Discount Factor is " + info.getUtilitySpace().getDiscountFactor());
        System.out.println("Reservation Value is " + info.getUtilitySpace().getReservationValueUndiscounted());

        ArrayList<Issue> issueList = (ArrayList<Issue>) info.getUtilitySpace().getDomain().getIssues();
        int nIssues = issues.size();

        issueWeight[] weightArray = new issueWeight[nIssues];

        for(int i=0; i<issues.size();i++) {
            Issue issue = (Issue) issues.get(i);

            weightArray[i] = new issueWeight();
            weightArray[i].name = issue.getName();
            weightArray[i].weight = additiveUtilitySpace.getWeight(issue.getNumber());

//            System.out.println(weightArray[i].name + " - weight : " + weightArray[i].weight);

//            listValueList.add((ArrayList<ValueDiscrete>) ((IssueDiscrete) issues.get(i)).getValues());
//            nValues[i]=listValueList.get(i).size();
//            bidListsize=bidListsize*nValues[i];
//            System.out.println(issueList.get(i));
//            System.out.println(nValues[i]);

        }
//        System.out.println(listValueList);

//        System.out.println("weight");

//        issueWeight[] weightArray = new issueWeight[nIssues];
//
//        for(int i=0;i<7;i++)
//        {
//            weightArray[i] = new issueWeight();  // create each actual Person
//        }
//
//        weightArray[1] = new issueWeight();
//        weightArray[1].name = "test";
//        weightArray[1].weight = 3;
//
//        System.out.println(weightArray);

    }

    /**
     * When this function is called, it is expected that the Party chooses one of the actions from the possible
     * action list and returns an instance of the chosen action.
     *
     * @param list
     * @return
     */
    @Override
    public Action chooseAction(List<Class<? extends Action>> list) {
        // According to Stacked Alternating Offers Protocol list includes
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