package pipe.gui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;

import javax.swing.JOptionPane;

import pipe.dataLayer.DataLayer;
import pipe.dataLayer.PetriNetObject;
import pipe.gui.Verifier.RunUppaalVerification;
import pipe.gui.widgets.RunningVerificationPanel;
import dk.aau.cs.TA.NTA;
import dk.aau.cs.TA.UPPAALQuery;
import dk.aau.cs.TAPN.ModelTransformer;
import dk.aau.cs.TAPN.TAPNToNTASymmetryTransformer;
import dk.aau.cs.TAPN.colorTranslations.ColoredDegree2BroadcastTransformer;
import dk.aau.cs.petrinet.PipeTapnToAauTapnTransformer;
import dk.aau.cs.petrinet.TAPN;
import dk.aau.cs.petrinet.TimedArcPetriNet;
import dk.aau.cs.petrinet.colors.ColoredPipeTapnToColoredAauTapnTransformer;

public class KBoundAnalyzer 
{
	protected DataLayer appModel;
	protected int k;
	private boolean notBounded = true;
	
	private boolean error=true;
	private boolean readingPropertyOneResult = false;
	
	private ModelChecker<NTA, UPPAALQuery> modelChecker;
	
	
	public boolean isBounded() {
		return !notBounded;
	}
	
	public KBoundAnalyzer(ModelChecker<NTA, UPPAALQuery> modelChecker, DataLayer appModel, int k)
	{
		this.k = k;
		this.appModel = appModel;
		this.modelChecker = modelChecker;
	}
	
	public void analyze()
	{
		modelChecker.setup();
		
		if (!modelChecker.isCorrectVersion()){
			System.err.println("Verifyta not found, or you are running an old version of verifyta.\n" +
					"Update to the latest development version.");
			return;
		}
		String verifyta = modelChecker.getPath();
		
		
		//Tmp files
		File xmlfile=null, qfile=null;
		try {
			xmlfile = File.createTempFile("verifyta", ".xml");
			qfile = File.createTempFile("verifyta", ".q");
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		xmlfile.deleteOnExit();qfile.deleteOnExit();
		
//		Create transformer and create uppaal model
		PipeTapnToAauTapnTransformer transformer = null;
		if(!appModel.isUsingColors()){
			transformer = new PipeTapnToAauTapnTransformer(appModel, 0);
		}else{
			transformer = new ColoredPipeTapnToColoredAauTapnTransformer(appModel, 0);
		}
		
		TAPN model=null;
		try {
			model = transformer.getAAUTAPN();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		ModelTransformer<TimedArcPetriNet, NTA> te = getReductionStrategy();
		try {
			NTA nta = null;
			try {
				nta = te.transformModel(model);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			nta.outputToUPPAALXML(new PrintStream(xmlfile));
			//te.transformToUppaal(model2, new PrintStream(xmlfile), k+1);
			
			//We can not auto transform as query is not having lock==1
			//te.autoTransform(model, new PrintStream(xmlfile), new PrintStream(qfile), inputQuery, k+1);
			
			PrintStream stream = new PrintStream(qfile);
			printQuery(stream);
					
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
//		 Do verifta 
		PetriNetObject.ignoreSelection(false);
		CreateGui.getApp().repaint();

		
		RunningVerificationPanel t = new RunningVerificationPanel();
		t.createDialog();
		
		//Run the verifucation thread 	
		RunUppaalVerification a = (new Verifier()).new RunUppaalVerification(verifyta, "-o0", xmlfile, qfile, t); //Wtf?
		a.start();
		
		t.show();
		
		if (t.isInterrupted()){
			a.verifyStop();
			a.interrupt();
			a.stop();
			a.destroy();
			
			try {
				a.wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			a=null;
			//Stop ther verification!
			JOptionPane.showMessageDialog(CreateGui.getApp(),
					"Verification was interupted by the user. No result found!",
					"Verification Result",
					JOptionPane.INFORMATION_MESSAGE);
			return;
			
		}
			
		try
		{
			notBounded = isNetBounded(a.bufferedReaderStderr, a.bufferedReaderStdout);
		}
		catch(Exception e)
		{
			System.err.println("There was an error verifying the model.");
			return;
		}
		
		showResult(a);	
	}

	protected ModelTransformer<TimedArcPetriNet, NTA> getReductionStrategy() {
		if(!appModel.isUsingColors()){
			return new TAPNToNTASymmetryTransformer(k+1);
		}else{
			return new ColoredDegree2BroadcastTransformer(k+1, true);
		}
	}

	protected void showResult(RunUppaalVerification a) {
		String resultmessage = "";
		
		String answerNetIsBounded = getAnswerBoundedString();
		String answerNetIsNotBounded = getAnswerNotBoundedString();
		
		
		//Display Answer
		resultmessage = notBounded ? answerNetIsNotBounded : answerNetIsBounded; 
		resultmessage+= "\nAnalysis time is estimated to: " + (a.verificationtime/1000.0) + "s";
		
		JOptionPane.showMessageDialog(CreateGui.getApp(),
				resultmessage,
				"Boundness Analyses Result",
				JOptionPane.INFORMATION_MESSAGE);
	}
	
	protected boolean isNetBounded(BufferedReader bufferedReaderStderr, BufferedReader bufferedReaderStdout) throws Exception
	{
		String line=null;
		
		try {
			while ( (line = bufferedReaderStdout.readLine()) != null){	
				parseLine(line);
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			error=true;
		}
		
		if (error){
			throw new Exception("error");
		}
		
		return notBounded;
	}

	protected void parseLine(String line) {
		if(line.contains("property 1"))
			readingPropertyOneResult = true;
		
		if (readingPropertyOneResult && line.contains("Property is satisfied")) {
			notBounded = true;
			error=false;
			readingPropertyOneResult = false;
		}else if (readingPropertyOneResult && line.contains("Property is NOT satisfied.")){
			notBounded=false;
			error=false;
			readingPropertyOneResult = false;
		}
	}

	protected void printQuery(PrintStream stream) {
		String inputQuery = "A[] P_capacity >= 1";
		stream.println("// Autogenerated by the TAPAAL (www.tapaal.net)");
		stream.println("");

		stream.println("/*");
		stream.println(" " + inputQuery + " " );
		stream.println("*/");
		
		if(!appModel.isUsingColors()){
			//stream.println("A[]((sum(i:pid_t) P(i).P_capacity)>= 1) and (Control.finish == 1)");
			stream.println("E<>((sum(i:pid_t) Token(i).P_capacity)== 0) and (Control.finish == 1)");
		}else{
			stream.println("E<>((sum(i:pid_t) Token(i).P_capacity) == 0) and (Control.P_lock == 1) and lock == 0");
		}
	}

	protected String getAnswerNotBoundedString() {
		String answerNetIsNotBounded =
			"The net with the speficied extra number of tokens is either unbounded or\n" +
			"more extra tokens have to be added in order to achieve an exact analysis.\n\n" +
			"This means that the analysis using the currently selected number \n" +
			"of extra tokens provides only an underapproximation of the net behaviour.\n" +
			"If you think that the net is bounded, try to add more extra tokens in order\n" +
			"to achieve exact verification analysis.\n";
		return answerNetIsNotBounded;
	}

	protected String getAnswerBoundedString() {
		String answerNetIsBounded =
		//	"The net is "+ k +" bounded.\n\n" + 
			"The net with the specified extra number of tokens is bounded.\n\n" +
			"This means that the analysis using the currently selected number\n" +
			"of extra tokens will be exact and always give the correct answer.\n";
		return answerNetIsBounded;
	}
}
