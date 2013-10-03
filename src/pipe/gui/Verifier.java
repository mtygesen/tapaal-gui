package pipe.gui;

import javax.swing.JOptionPane;
import javax.swing.JSpinner;

import pipe.dataLayer.TAPNQuery;
import pipe.gui.widgets.RunningVerificationDialog;
import dk.aau.cs.TCTL.TCTLAbstractProperty;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import dk.aau.cs.translations.ReductionOption;
import dk.aau.cs.verification.ModelChecker;
import dk.aau.cs.verification.UPPAAL.UppaalIconSelector;
import dk.aau.cs.verification.UPPAAL.Verifyta;
import dk.aau.cs.verification.UPPAAL.VerifytaOptions;
import dk.aau.cs.verification.VerifyTAPN.VerifyPN;
import dk.aau.cs.verification.VerifyTAPN.VerifyPNOptions;
import dk.aau.cs.verification.VerifyTAPN.VerifyTAPN;
import dk.aau.cs.verification.VerifyTAPN.VerifyTAPNDiscreteVerification;
import dk.aau.cs.verification.VerifyTAPN.VerifyTAPNIconSelector;
import dk.aau.cs.verification.VerifyTAPN.VerifyTAPNOptions;
import dk.aau.cs.verification.VerifyTAPN.VerifyDTAPNOptions;

/**
 * Implementes af class for handling integrated Uppaal Verification
 * 
 * Copyright 2009 Author Kenneth Yrke Joergensen <kenneth@yrke.dk>
 * 
 * Licensed under the Open Software License version 3.0
 */

public class Verifier {
	private static Verifyta getVerifyta() {
		Verifyta verifyta = new Verifyta(new FileFinderImpl(), new MessengerImpl());
		verifyta.setup();
		return verifyta;
	}
	
	private static VerifyTAPN getVerifyTAPN() {
		VerifyTAPN verifytapn = new VerifyTAPN(new FileFinderImpl(), new MessengerImpl());
		verifytapn.setup();
		return verifytapn;
	}
	
	private static VerifyTAPNDiscreteVerification getVerifydTAPN() {
		VerifyTAPNDiscreteVerification verifydtapn = new VerifyTAPNDiscreteVerification(new FileFinderImpl(), new MessengerImpl());
		verifydtapn.setup();
		return verifydtapn;
	}
	
	private static VerifyPN getVerifyPN() {
		VerifyPN verifypn = new VerifyPN(new FileFinderImpl(), new MessengerImpl());
		verifypn.setup();
		return verifypn;
	}
	
	private static ModelChecker getModelChecker(TAPNQuery query) {
		if(query.getReductionOption() == ReductionOption.VerifyTAPN){
			return getVerifyTAPN();
		} else if(query.getReductionOption() == ReductionOption.VerifyTAPNdiscreteVerification){
			return getVerifydTAPN();
		} else if(query.getReductionOption() == ReductionOption.VerifyPN){
			return getVerifyPN();
		} else{
			throw new RuntimeException("Verification method: " + query.getReductionOption() + ", should not be send here");
		}
	}

	public static void analyzeKBound(
			TimedArcPetriNetNetwork tapnNetwork, int k, JSpinner tokensControl) {
		ModelChecker modelChecker;
		if(tapnNetwork.hasWeights() || tapnNetwork.hasUrgentTransitions()){
			modelChecker = getVerifydTAPN();
		} else {
			modelChecker = getVerifyTAPN();
		}

		if (!modelChecker.isCorrectVersion()) {
			System.err.println("The model checker not found, or you are running an old version of it.\n"
							+ "Update to the latest development version.");
			return;
		}
		KBoundAnalyzer optimizer = new KBoundAnalyzer(tapnNetwork, k, modelChecker, new MessengerImpl(), tokensControl);
		optimizer.analyze();
	}


	public static void runUppaalVerification(TimedArcPetriNetNetwork timedArcPetriNetNetwork, TAPNQuery input) {
		runUppaalVerification(timedArcPetriNetNetwork, input, false);
	}

	private static void runUppaalVerification(TimedArcPetriNetNetwork timedArcPetriNetNetwork, TAPNQuery input,	boolean untimedTrace) {
		Verifyta verifyta = getVerifyta();
		if (!verifyta.isCorrectVersion()) {
			System.err.println("Verifyta not found, or you are running an old version of Verifyta.\n"
							+ "Update to the latest development version.");
			return;
		}

		TCTLAbstractProperty inputQuery = input.getProperty();

		VerifytaOptions verifytaOptions = new VerifytaOptions(input.getTraceOption(), input.getSearchOption(), untimedTrace, input.getReductionOption(), input.useSymmetry(), input.useOverApproximation());

		if (inputQuery == null) {
			return;
		}

		if (timedArcPetriNetNetwork != null) {
			RunVerificationBase thread = new RunVerification(verifyta, new UppaalIconSelector(), new MessengerImpl());
			RunningVerificationDialog dialog = new RunningVerificationDialog(CreateGui.getApp());
			dialog.setupListeners(thread);
			thread.execute(verifytaOptions, timedArcPetriNetNetwork, new dk.aau.cs.model.tapn.TAPNQuery(input.getProperty(), input.getCapacity()));
			dialog.setVisible(true);
		} else {
			JOptionPane.showMessageDialog(CreateGui.getApp(),
					"There was an error converting the model.",
					"Conversion error", JOptionPane.ERROR_MESSAGE);
		}

		return;
	}
	
	

	public static void runVerifyTAPNVerification(TimedArcPetriNetNetwork tapnNetwork, TAPNQuery query) {
		ModelChecker verifytapn = getModelChecker(query);

		if (!verifytapn.isCorrectVersion()) {
			new MessengerImpl().displayErrorMessage(
					"No "+verifytapn+" specified: The verification is cancelled",
					"Verification Error");
			return;
		}
		
		TCTLAbstractProperty inputQuery = query.getProperty();

		int bound = query.getCapacity();
		
		VerifyTAPNOptions verifytapnOptions;
		if(query.getReductionOption() == ReductionOption.VerifyTAPNdiscreteVerification){
			verifytapnOptions = new VerifyDTAPNOptions(bound, query.getTraceOption(), query.getSearchOption(), query.useSymmetry(), query.useTimeDarts(), query.usePTrie(), query.useOverApproximation(), query.discreteInclusion(), query.inclusionPlaces());
		} else if(query.getReductionOption() == ReductionOption.VerifyPN){
			verifytapnOptions = new VerifyPNOptions(bound, query.getTraceOption(), query.getSearchOption(), query.useOverApproximation());
		} else {
			verifytapnOptions = new VerifyTAPNOptions(bound, query.getTraceOption(), query.getSearchOption(), query.useSymmetry(), query.useOverApproximation(), query.discreteInclusion(), query.inclusionPlaces());
		}

		if (inputQuery == null) {
			return;
		}

		if (tapnNetwork != null) {
			RunVerificationBase thread = new RunVerification(verifytapn, new VerifyTAPNIconSelector(), new MessengerImpl());
			RunningVerificationDialog dialog = new RunningVerificationDialog(CreateGui.getApp());
			dialog.setupListeners(thread);
			thread.execute(verifytapnOptions, tapnNetwork, new dk.aau.cs.model.tapn.TAPNQuery(query.getProperty(), bound));
			dialog.setVisible(true);
		} else {
			JOptionPane.showMessageDialog(CreateGui.getApp(),
					"There was an error converting the model.",
					"Conversion error", JOptionPane.ERROR_MESSAGE);
		}

		return;
		
	}
}
