import negotiator.AgentID;
import negotiator.Bid;
import negotiator.BidHistory;
import negotiator.actions.Accept;
import negotiator.actions.Action;
import negotiator.actions.Offer;
import negotiator.bidding.BidDetails;
import negotiator.issue.Issue;
import negotiator.parties.AbstractNegotiationParty;
import negotiator.parties.NegotiationInfo;
import negotiator.utility.AbstractUtilitySpace;
import negotiator.utility.AdditiveUtilitySpace;
import negotiator.utility.Evaluator;
import java.util.ArrayList;

import java.util.List;
import java.util.Random;


public class AgentCC extends AbstractNegotiationParty {
    private final String description = "AgentCC";

    private Bid lastReceivedOffer; // offer on the table
    private Bid myLastOffer;

    private Bid maxBid;
    private Bid minBid;

    private BidHistory bidHistory = new BidHistory();

    private List<offerReceive> offerArray = new ArrayList<offerReceive>();

    class offerReceive {
        public double offerUtility;

        public offerReceive(double offerUtility){
            this.offerUtility = offerUtility;
        }

        public double getOfferUtility(){
            return offerUtility;
        }
//        public int getSize(){
//            return this.getSize();
//        }
    }

    //**** Initial part of the Agent ****//
    @Override
    public void init(NegotiationInfo info) {
        super.init(info);

        //get UtilitySpace for getting issue weights in the next step
        AbstractUtilitySpace utilitySpace = info.getUtilitySpace();
        AdditiveUtilitySpace additiveUtilitySpace = (AdditiveUtilitySpace) utilitySpace;

        //get max & min utility Bid
        try {
            maxBid = additiveUtilitySpace.getMaxUtilityBid();
            minBid = additiveUtilitySpace.getMinUtilityBid();


        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("max:" + utilitySpace.getUtility(maxBid));
        System.out.println("min:" + utilitySpace.getUtility(minBid));
        System.out.println("max:" + maxBid + "| min:" + minBid);

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
        double time = getTimeLine().getTime();

        if (lastReceivedOffer != null) {

            double offerUtility = this.utilitySpace.getUtility(lastReceivedOffer);
//            System.out.println(offerUtility);

            offerArray.add(new offerReceive(offerUtility));

            BidDetails offerReceiveDetail = new BidDetails(lastReceivedOffer, 0, time);
            bidHistory.add(offerReceiveDetail);
//            BidDetails test = new BidDetails();
//            bidHistory.add();


//            System.out.println(offerArray.size());
//            if (myLastOffer != null) {
//                offerArray.add(new offerReceive(this.utilitySpace.getUtility(myLastOffer)));
//            }
        }

        if (time < 0.5) {
//            return new Offer(this.getPartyId(), this.getMaxUtilityBid());

            double utilityThreshold = ((utilitySpace.getUtility(maxBid) + 0.8) / 2);
            myLastOffer = generateRandomBidWithUtility(utilityThreshold);
            return new Offer(this.getPartyId(), myLastOffer);
        } else {

            // Accepts the bid on the table in this phase,
            // if the utility of the bid is higher than Example Agent's last bid.
            if (lastReceivedOffer != null
                    && myLastOffer != null
                    && this.utilitySpace.getUtility(lastReceivedOffer) > this.utilitySpace.getUtility(myLastOffer)) {

                return new Accept(this.getPartyId(), lastReceivedOffer);
            } else {
                // Offering a random bid
//                myLastOffer = generateRandomBid();

                double sumOfferReceive = 0;
                double avgOffetReceive;
                double utilityThreshold;

                for(int i=0; i<offerArray.size();i++) {
                    sumOfferReceive = sumOfferReceive + offerArray.get(i).getOfferUtility();
                }
                avgOffetReceive = sumOfferReceive / offerArray.size();
                utilityThreshold = ((utilitySpace.getUtility(maxBid) + avgOffetReceive) / 2) * time;
//                utilityThreshold = avgOffetReceive / (1 + time);
                if (utilityThreshold < 0.67) {
                    utilityThreshold = ((utilitySpace.getUtility(maxBid) + 0.8) / 2);
                }

                System.out.println(utilityThreshold);
                System.out.println(bidHistory.getAverageUtility());
                System.out.println(bidHistory.getBestBidDetails());
                System.out.println(bidHistory.getHistory());

                myLastOffer = generateRandomBidWithUtility(utilityThreshold);
//                System.out.println("mylastoffer: "+myLastOffer);
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

    public Bid generateRandomBidWithUtility(double utilityThreshold) {
        Bid randomBid;
        double utility;
        do {
            randomBid = generateRandomBid();
//            System.out.println(randomBid);

            try {
                utility = utilitySpace.getUtility(randomBid);
//                System.out.println(utility);
            } catch (Exception e)
            {
                utility = 0.0;
            }
        }
        while (utility < utilityThreshold);
        return randomBid;
    }

}