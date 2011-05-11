import net.sourceforge.jFuzzyLogic.FIS;

/**
 * Test parsing an FCL file
 * @author pcingola@users.sourceforge.net
 */
public class FuzzyExample {
    public static void run() throws Exception {
        // Load from 'FCL' file
        String fileName = "fcl/tipper.fcl";
        FIS fis = FIS.load(fileName,true);
        // Error while loading?
        if( fis == null ) { 
            System.err.println("Can't load file: '" 
                                   + fileName + "'");
            return;
        }

        // Show 
        fis.chart();

        // Set inputs
        fis.setVariable("service", 3);
        fis.setVariable("food", 7);

        // Evaluate
        fis.evaluate();

        // Show output variable's chart 
        fis.getVariable("tip").chartDefuzzifier(true);

        // Print ruleSet
        System.out.println(fis);
    }
}
