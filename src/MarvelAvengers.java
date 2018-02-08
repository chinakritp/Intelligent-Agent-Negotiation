

import negotiator.AgentID;
import negotiator.Bid;
import negotiator.actions.Accept;
import negotiator.actions.Action;
import negotiator.actions.Offer;
import negotiator.issue.Issue;
import negotiator.parties.AbstractNegotiationParty;
import negotiator.parties.NegotiationInfo;
import negotiator.utility.AbstractUtilitySpace;
import negotiator.utility.AdditiveUtilitySpace;
import negotiator.utility.Evaluator;

import java.util.List;
import java.util.Random;


public class MarvelAvengers extends AbstractNegotiationParty {
        private final String description = "YourAgentsNameHere : Marvel Avengers ";

    private Bid myLastOffer;
    private Bid lastReceivedOffer; // offer on the table
    private Bid prevReceivedOffer; // offer from the previous turn
    private Bid BestReceivedOffer;
    private Bid WorstReceivedOffer;

    private List<Issue> issues;
    private int sizeIssues = 0;
    private my_issue[] myIssueArray;

    private Bid newBid;
    private Bid randBid;  //store of a random Bid
    private Bid maxBid;
    private Bid minBid;
    private Bid putvalueBid;

    private AdditiveUtilitySpace adtUtilSpace;
    private double LeastAcceptableBidUtility;
    private int play_mode;
    private int GoodFriend_score;
    private int BadFriend_score;

    private class my_issue {
        int issue_id;
        String issue_name;
        double issue_weight;
        int important_label;

        public void prioritize_issue(int sizeIssues, double sumIssueWeight)
        {
            //compare this issue weight to the average value of issues.
            double avgWeight = (sumIssueWeight/sizeIssues);

            if(issue_weight >= avgWeight) {important_label = 1;}
            else if (issue_weight < avgWeight*0.5) {important_label = 3;}
            else {important_label = 2;}
        }
    }

    public void Calculate_LeastAcceptableBidUtility(double mid_size)
    {
        // sizeIssues should already has value before use this function.
        if (sizeIssues > 0)
        {
            double time = getTimeLine().getTime();
            double slope_parameter = mid_size*(0.1+time);

            // set input for Sigmoid function
            double xx = (sizeIssues - slope_parameter)*6*(1/(mid_size-1));

            // use Sigmoid function to initialize the value
            double sigmoidValue = Math.exp(xx) / (Math.exp(xx) + 1);

            // The acceptable bid must has utility equal to this Least Utility value.
            LeastAcceptableBidUtility = 0.49 + (0.375*sigmoidValue) ;
            //System.out.println(" Least Acceptable Bid Utility: "+" "+LeastAcceptableBidUtility);
        }
    }

    public void Calculate_Tough_LeastAcceptableBidUtility(double mid_size)
    {
        // sizeIssues should already has value before use this function.
        if (sizeIssues > 0)
        {
            double time = getTimeLine().getTime();
            double slope_parameter = mid_size*(0.1+time);

            // set input for Sigmoid function
            double xx = (sizeIssues - slope_parameter)*6*(1/(mid_size-1));

            // use Sigmoid function to initialize the value
            double sigmoidValue = Math.exp(xx) / (Math.exp(xx) + 1);

            // The acceptable bid must has utility equal to this Least Utility value.
            LeastAcceptableBidUtility = 0.69 + (0.20*sigmoidValue) ;

            if (LeastAcceptableBidUtility > utilitySpace.getUtility(maxBid))
            {    LeastAcceptableBidUtility = utilitySpace.getUtility(maxBid);   }

            //System.out.println(" Least Acceptable Bid Utility: "+" "+LeastAcceptableBidUtility);
        }
    }

    public void Calculate_LeastAcceptableBidUtility_2(double mid_size)
    {
        // sizeIssues should already has value before use this function.
        if (sizeIssues > 0)
        {
            double time = getTimeLine().getTime();
            double slope_parameter = mid_size*(0.1+time);

            // set input for Sigmoid function
            double xx = (sizeIssues - slope_parameter)*6*(1/(mid_size-1));

            // use Sigmoid function to initialize the value
            double sigmoidValue = Math.exp(xx) / (Math.exp(xx) + 1);

            // The acceptable bid must has utility equal to this Least Utility value.
            LeastAcceptableBidUtility = 0.49 + (0.4*sigmoidValue) ;
            //System.out.println(" Least Acceptable Bid Utility: "+" "+LeastAcceptableBidUtility);
        }
    }

    public void create_new_bid_01() {
        int a_issue_id;
        // a_issue_name;
        //double a_issue_weight;
        int a_important_label;

        newBid = null;

        // Base Bid for putValue method.
        putvalueBid = randBid;

        for (int i = 0; i < sizeIssues; i++) {
            //Generate a random number
            Random rnd = new Random();
            double randnum = rnd.nextDouble();

            a_issue_id = myIssueArray[i].issue_id;
            //a_issue_name = myIssueArray[i].issue_name;
            //a_issue_weight = myIssueArray[i].issue_weight;
            a_important_label = myIssueArray[i].important_label;

            //Assign probability score to each range of utility
            int sum_density = 0;
            int prob_interval_number = 3;
            int[] prob = new int[prob_interval_number];
            prob[0] = 2;
            prob[1] = 3;
            prob[2] = 4;

            //Sum all probability score to use in normalizing calculation.
            for (int kk = 0; kk < prob_interval_number; kk++) {
                sum_density = sum_density + prob[kk];
            }

            //Select range of utility by probability score as accumulated probability density.
            double accum_density = 0;
            int select_interval = 0;
            int selected_done = 0;
            for (int cnt = 0; cnt < prob_interval_number; cnt++) {
                accum_density = accum_density + prob[cnt];
                if (accum_density >= (randnum * sum_density) && selected_done == 0) {
                    select_interval = cnt + 1;
                    selected_done = 1; // done with interval selection.
                }
            }

            //Generate bid corresponds to range of utility that was chosen from previous step.
            double utilFloor;
            double utilCeil;
            switch (select_interval) {
                case 1:
                    switch (a_important_label) {
                        case 1:
                            utilFloor = LeastAcceptableBidUtility ;
                            utilCeil =  LeastAcceptableBidUtility + 0.34*(1 - LeastAcceptableBidUtility) ;
                            generateRandomBidWithIssueUtilityBand(utilFloor, utilCeil,a_issue_id);
                            break;
                        case 2:
                            utilFloor = 0.25 ;
                            utilCeil = 0.25 + 0.34*(1 - 0.25) ;
                            generateRandomBidWithIssueUtilityBand(utilFloor, utilCeil,a_issue_id);
                            break;
                        case 3:
                            utilFloor = 0;
                            utilCeil = 0.34*(0.5);
                            generateRandomBidWithIssueUtilityBand(utilFloor, utilCeil,a_issue_id);
                            break;
                    }
                    break;

                case 2:
                    switch (a_important_label) {
                        case 1:
                            utilFloor = LeastAcceptableBidUtility + 0.34*(1 - LeastAcceptableBidUtility);
                            utilCeil = LeastAcceptableBidUtility + 0.67*(1 - LeastAcceptableBidUtility) ;
                            generateRandomBidWithIssueUtilityBand(utilFloor, utilCeil,a_issue_id);
                            break;
                        case 2:
                            utilFloor = 0.25 + 0.34*(1 - 0.25);
                            utilCeil = 0.25 + 0.67*(1 - 0.25);
                            generateRandomBidWithIssueUtilityBand(utilFloor, utilCeil,a_issue_id);
                            break;
                        case 3:
                            utilFloor = 0.34*(0.5);
                            utilCeil = 0.67*(0.5);
                            generateRandomBidWithIssueUtilityBand(utilFloor, utilCeil,a_issue_id);
                            break;
                    }
                    break;

                case 3:
                    switch (a_important_label) {
                        case 1:
                            utilFloor = LeastAcceptableBidUtility + 0.67*(1 - LeastAcceptableBidUtility);
                            utilCeil =  1  ;
                            generateRandomBidWithIssueUtilityBand(utilFloor, utilCeil,a_issue_id);
                            break;
                        case 2:
                            utilFloor = 0.25 + 0.67*(1 - 0.25);
                            utilCeil = 1 ;
                            generateRandomBidWithIssueUtilityBand(utilFloor, utilCeil,a_issue_id);
                            break;
                        case 3:
                            utilFloor =0.67*(0.5);
                            utilCeil = 0.5;
                            generateRandomBidWithIssueUtilityBand(utilFloor, utilCeil,a_issue_id);
                            break;
                    }
                    break;

                default:
                    switch (a_important_label) {
                        case 1:
                            utilFloor = LeastAcceptableBidUtility;
                            utilCeil =  1  ;
                            generateRandomBidWithIssueUtilityBand(utilFloor, utilCeil,a_issue_id);
                            break;
                        case 2:
                            utilFloor = 0.25 ;
                            utilCeil = 1 ;
                            generateRandomBidWithIssueUtilityBand(utilFloor, utilCeil,a_issue_id);
                            break;
                        case 3:
                            utilFloor = 0;
                            utilCeil = 0.5;
                            generateRandomBidWithIssueUtilityBand(utilFloor, utilCeil,a_issue_id);
                            break;
                    }
                    break;
            }
        }

        newBid = putvalueBid;
    }

    //Method for generate a Bid that has utility value of the Bid within range or interval between input values.
    public Bid generateRandomBidWithUtilityBand(double utilityFloor,double utilityCeil) {
        Bid randomBid;
        double utility;
        int count_loop = 0;
        int max_count_loop = 1000;

        double max_utility = utilitySpace.getUtility(maxBid);
        if(utilityFloor > max_utility)
        {utilityFloor = max_utility;}

        double min_utility = utilitySpace.getUtility(minBid);
        if(utilityCeil < min_utility)
        {utilityCeil = min_utility;}

        if(utilityCeil < min_utility)
        {utilityCeil = utilityFloor;}

        do {
            count_loop++;
            randomBid = generateRandomBid();
            try {
                utility = utilitySpace.getUtility(randomBid);
            } catch (Exception e)
            {
                utility = 0.0;
            }
        }
        while  ( ((utility < utilityFloor) || (utility > utilityCeil)) && (count_loop < max_count_loop) );

        if (count_loop >= max_count_loop)
        {
            randomBid = maxBid;
        }

        return randomBid;
    }

    //Method for generate a Bid that has utility value of the Issue within range or interval between input values.
    public void generateRandomBidWithIssueUtilityBand(double utilityFloor,double utilityCeil, int IssueId) {
        Bid genBid;
        double utility;
        int count_loop = 0;
        int max_count_loop = 1000;

        // create an Evaluator for the Issue
        Evaluator eval_obj =  adtUtilSpace.getEvaluator(IssueId);

        double max_utility = eval_obj.getEvaluation(adtUtilSpace, maxBid, IssueId);
        if(utilityFloor > max_utility)
        {utilityFloor = max_utility;}

        double min_utility = eval_obj.getEvaluation(adtUtilSpace, minBid, IssueId);
        if(utilityCeil < min_utility)
        {utilityCeil = min_utility;}

        if(utilityCeil < min_utility)
        {utilityCeil = utilityFloor;}

        do {
            count_loop++;
            genBid = generateRandomBid();
            try {
                // Read a utility score of an issue for a particular bid in a particular Utility Space.
                utility = eval_obj.getEvaluation(adtUtilSpace, genBid, IssueId);
            } catch (Exception e)
            {
                utility = 0.0;
            }
        }
        while ( ((utility < utilityFloor) || (utility > utilityCeil)) && (count_loop < max_count_loop) );

        if (count_loop >= max_count_loop)
        {
            double utilFloor = LeastAcceptableBidUtility;
            double utilCeil = 1;
            genBid = generateRandomBidWithUtilityBand(utilFloor, utilCeil);
        }

        // Construct a new bid by PutValue
        putvalueBid = putvalueBid.putValue(IssueId,genBid.getValue(IssueId));
    }

    //**** Initial part of the Agent ****//
        @Override
        public void init(NegotiationInfo info) {
            super.init(info);
            System.out.println("--------------------");
            System.out.println("My code printing starts here: ");
            System.out.println("--------------------");

            //get UtilitySpace for getting issue weights in the next step
            AbstractUtilitySpace utilitySpace = info.getUtilitySpace();
            AdditiveUtilitySpace additiveUtilitySpace = (AdditiveUtilitySpace) utilitySpace;

            //get max utility Bid
            try {
                maxBid = additiveUtilitySpace.getMaxUtilityBid();
            } catch (Exception e) {
                e.printStackTrace();
            }

            //get min utility Bid
            try {
                minBid = additiveUtilitySpace.getMinUtilityBid();
            } catch (Exception e) {
                e.printStackTrace();
            }
            //Generate a random Bid, just in case it is needed.
            randBid = generateRandomBid();

            //get Issues as List of Issue
            issues = additiveUtilitySpace.getDomain().getIssues();
            System.out.println(" domain name: "+additiveUtilitySpace.getFileName());
            System.out.println("--------------------");

            sizeIssues = issues.size();
            adtUtilSpace = additiveUtilitySpace;

            // Initialize sumIssueWeight variable.
            double sumIssueWeight = 0;
            // Create Object Array for issues class
            myIssueArray = new my_issue[sizeIssues];

            for (int i = 0; i < sizeIssues; i++){
                Issue issue = (Issue) issues.get(i);
                //System.out.println(i+" issue number: "+" "+issue.getNumber());
                //System.out.println(i+" issue name: "+" "+issue.getName());
                //System.out.println(i+" issue weight: "+" "+additiveUtilitySpace.getWeight(issue.getNumber()));

                //Adding value into Object Array of my issue
                myIssueArray[i] = new my_issue();
                myIssueArray[i].issue_id = issue.getNumber();
                myIssueArray[i].issue_name = issue.getName();
                myIssueArray[i].issue_weight = additiveUtilitySpace.getWeight(issue.getNumber());

                //System.out.println("   Printing value in Issue Object Array as follows: ");
                //System.out.println(myIssueArray[i].issue_id + ": "+myIssueArray[i].issue_name + " - weight : " + myIssueArray[i].issue_weight);
               // System.out.println("--------------------");

                sumIssueWeight = sumIssueWeight + myIssueArray[i].issue_weight;
            }
            //System.out.println("Sum of Issue Weight: "+ sumIssueWeight);
            //System.out.println("--------------------");

            //labelling the important label in each my_issue object.
            for (int i = 0; i < sizeIssues; i++) {
                myIssueArray[i].prioritize_issue(sizeIssues, sumIssueWeight);
                //System.out.println(i+" issue_name: "+myIssueArray[i].issue_name+", important_label: "+myIssueArray[i].important_label);
            }
            //System.out.println("--------------------");

            // Initialize the Play Mode : play mode 1 is a normal mode.
            play_mode = 3;

            // Initialize the score to determine Tough Mode during negotiation
            GoodFriend_score = 0;
            BadFriend_score = 0;

            // Initialize the Least Acceptable Bid Utility
            Calculate_LeastAcceptableBidUtility(2);
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
            double midsize = 2 ;

            if( play_mode == 1) {
                // change least acceptable bid utility over time
                midsize = 2 + (sizeIssues * time);
                Calculate_LeastAcceptableBidUtility(midsize);
            }
            else if (play_mode == 2)
            {
                // change least acceptable bid utility to be tougher over time
                midsize = 2 + (sizeIssues * (1-time));
                // use Tough function
                Calculate_Tough_LeastAcceptableBidUtility(midsize);
            }
            else if (play_mode == 3)
            {
                // change least acceptable bid utility to be tougher over time
                midsize = 2 + (sizeIssues * (time) * 0.7);
                // use Tough function
                Calculate_LeastAcceptableBidUtility_2(midsize);
            }
            else{
                // use midsize as the initialized value
                midsize = 2 ;
                Calculate_LeastAcceptableBidUtility(midsize);
            }

            // Accepts the bid on the table in this phase,
            // if the utility of the bid is higher than Agent's new generated bid.
            do {
                create_new_bid_01();
            } while (this.utilitySpace.getUtility(newBid) < LeastAcceptableBidUtility);

            if (lastReceivedOffer != null
                    && myLastOffer != null
                    && this.utilitySpace.getUtility(lastReceivedOffer) >= LeastAcceptableBidUtility) {

                //Collect latest offer from other as a previous offer.
                prevReceivedOffer = lastReceivedOffer;

                //Accept offered bid.
                return new Accept(this.getPartyId(), lastReceivedOffer);
            } else {
                // Generating new Bid.
                myLastOffer = newBid;

                //Collect latest offer from other as a previous offer.
                prevReceivedOffer = lastReceivedOffer;

                //Offer or counter offer.
                return new Offer(this.getPartyId(), myLastOffer);
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

            //double time = getTimeLine().getTime();

            if (act instanceof Offer) { // sender is making an offer
                Offer offer = (Offer) act;

                // storing last received offer
                lastReceivedOffer = offer.getBid();
            }

            if(prevReceivedOffer != null) {
                if (this.utilitySpace.getUtility(lastReceivedOffer) <= this.utilitySpace.getUtility(prevReceivedOffer)) {
                    // Increase the Bad Friend score for un-improved received offer
                    if (play_mode == 1) {
                        BadFriend_score++;
                    }
                    else if(play_mode == 3) { BadFriend_score++;}
                }
                else { // Increase the Good Friend score for improved received offer
                    if (play_mode == 2) {
                        GoodFriend_score++;
                    }
                    else if(play_mode == 3) { GoodFriend_score++;}
                }
            }

            if (BestReceivedOffer != null){

                // Increase the Bad Friend score for un-improved received offer
                if (this.utilitySpace.getUtility(lastReceivedOffer) <= this.utilitySpace.getUtility(BestReceivedOffer) )
                {
                    if(play_mode == 1) { BadFriend_score++;}
                    else if(play_mode == 3) { BadFriend_score++;}
                }
                else {
                    // If now is in tough mode (play mode = 2) then add the Good Friend Score for improved received offer
                    if(play_mode == 2) { GoodFriend_score++;}
                    else if(play_mode == 3) { GoodFriend_score++;}
                }

                // Get new received offer and keep its information as Best bid
                if (this.utilitySpace.getUtility(lastReceivedOffer) > this.utilitySpace.getUtility(BestReceivedOffer) )
                {
                    BestReceivedOffer = lastReceivedOffer;
                }
            }
            else {
                BestReceivedOffer = lastReceivedOffer;
            }

            if (WorstReceivedOffer != null){

                // Increase the Bad Friend score a lot for even worst received offer
                if (this.utilitySpace.getUtility(lastReceivedOffer) < this.utilitySpace.getUtility(WorstReceivedOffer) )
                {
                    if(play_mode == 1) { BadFriend_score = BadFriend_score + 3;}
                    else if(play_mode == 3) { BadFriend_score = BadFriend_score + 3;}
                }

                // Get new received offer and keep its information as Worst bid
                if (this.utilitySpace.getUtility(lastReceivedOffer) < this.utilitySpace.getUtility(WorstReceivedOffer) )
                {
                    WorstReceivedOffer = lastReceivedOffer;
                }
            }
            else {
                WorstReceivedOffer = lastReceivedOffer;
            }

            if(BadFriend_score > 10)
            {
                // Change play mode to the tough mode (play mode = 2)
                play_mode = 2;
                // Reset the Friend score
                GoodFriend_score = 0;
                BadFriend_score = 0;
            }

            if(GoodFriend_score > 5 )
            {
                // Change play mode to the normal mode (play mode = 1)
                if(play_mode == 2) {play_mode = 3;}
                else if(play_mode == 3) {play_mode = 1;}
                // Reset the Friend score
                GoodFriend_score = 0;
                BadFriend_score = 0;
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

}